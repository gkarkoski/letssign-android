package com.example.challenge_letssign.ui.signer.signing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.data.local.signature.SavedSignatureSet
import com.example.challenge_letssign.data.local.signature.SavedSignatureStore
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureFieldType
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.repository.PdfStorageRepository
import com.example.challenge_letssign.domain.usecase.signer.SignDocumentUseCase
import com.example.challenge_letssign.ui.components.extractInitials
import com.example.challenge_letssign.utils.AppResult
import com.example.challenge_letssign.utils.PdfPageRenderer
import com.example.challenge_letssign.utils.RenderedPdfPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

enum class SignWizardStep {
    STEP_1_READ_DOCUMENT,
    STEP_2_CHOOSE_SAVED,
    STEP_2_CAPTURE_SIGNATURE,
    STEP_2_CAPTURE_INITIALS,
    STEP_3_CONFIRM,
    COMPLETED,
    FAILURE
}

enum class SignatureCaptureMode {
    DRAW,
    TYPE
}

enum class CursiveFontStyle {
    DANCING_SCRIPT,
    CAVEAT,
    HOMEMADE_APPLE
}

data class SignerDocumentSignUiState(
    val document: Document? = null,
    val signerId: String = "",
    val signerName: String = "Rafael Tavares",
    val pdfFile: File? = null,
    val renderedPage: RenderedPdfPage? = null,
    val currentPageIndex: Int = 0,
    val totalPages: Int = 1,
    val isLoadingPage: Boolean = true,
    val myFields: List<SignatureField> = emptyList(),
    val mySignatureStatus: SignatureStatus = SignatureStatus.PENDING,
    val wizardStep: SignWizardStep = SignWizardStep.STEP_1_READ_DOCUMENT,
    val signatureMode: SignatureCaptureMode = SignatureCaptureMode.DRAW,
    val initialsMode: SignatureCaptureMode = SignatureCaptureMode.DRAW,
    val typedSignatureText: String = "Rafael Tavares",
    val typedSignatureFont: CursiveFontStyle = CursiveFontStyle.DANCING_SCRIPT,
    val typedInitialsText: String = "RT",
    val typedInitialsFont: CursiveFontStyle = CursiveFontStyle.DANCING_SCRIPT,
    val capturedSignatureBytes: ByteArray? = null,
    val capturedInitialsBytes: ByteArray? = null,
    // Banco de assinaturas salvas do signatário + opção "Salvar assinatura e rubrica" do cadastro
    val savedSignatureSets: List<SavedSignatureSet> = emptyList(),
    val saveForFutureUse: Boolean = false,
    val isSubmittingSignature: Boolean = false,
    val signedTimestampMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val isErrorSnackbar: Boolean = false,
    val isSignedCompleted: Boolean = false
) {
    /** Assinatura + rubrica já definidas para ESTE documento (cadastradas agora ou escolhidas das salvas). */
    val hasSignatureReadyForThisDocument: Boolean
        get() = capturedSignatureBytes != null && capturedInitialsBytes != null

    val hasSavedSignatures: Boolean
        get() = savedSignatureSets.isNotEmpty()

    val canSign: Boolean
        get() = mySignatureStatus == SignatureStatus.PENDING &&
                myFields.isNotEmpty() &&
                !isSubmittingSignature

    val isDrawingModalOpen: Boolean
        get() = wizardStep == SignWizardStep.STEP_2_CAPTURE_SIGNATURE ||
                wizardStep == SignWizardStep.STEP_2_CAPTURE_INITIALS

    val currentPageMyFields: List<SignatureField>
        get() = myFields.filter { it.pageNumber == currentPageIndex }

    val hasInitialsField: Boolean
        get() = myFields.any { it.fieldType == SignatureFieldType.INITIALS }

    val footerFieldsDescription: String
        get() {
            if (myFields.isEmpty()) return "Nenhum campo para você neste documento"
            val hasSignature = myFields.any { it.fieldType != SignatureFieldType.INITIALS }
            val kinds = when {
                hasSignature && hasInitialsField -> "assinatura e rubrica"
                hasInitialsField -> "rubrica"
                else -> "assinatura"
            }
            val pages = myFields.map { it.pageNumber }.distinct().sorted()
            return if (pages.size == 1) {
                "Você assina na página ${pages.first() + 1}: $kinds"
            } else {
                "Você assina em ${pages.size} páginas: $kinds"
            }
        }

    val topBarStepSubtitle: String
        get() = when (wizardStep) {
            SignWizardStep.STEP_1_READ_DOCUMENT -> "Etapa 1 de 3 · Leia o documento"
            SignWizardStep.STEP_2_CHOOSE_SAVED -> "Etapa 2 de 3 · Escolha sua assinatura"
            SignWizardStep.STEP_2_CAPTURE_SIGNATURE -> "Etapa 2 de 3 · Sua assinatura"
            SignWizardStep.STEP_2_CAPTURE_INITIALS -> "Etapa 2 de 3 · Sua rubrica"
            SignWizardStep.STEP_3_CONFIRM,
            SignWizardStep.COMPLETED,
            SignWizardStep.FAILURE -> "Etapa 3 de 3 · Confirmar"
        }
}

@HiltViewModel
class SignerDocumentSignViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val pdfStorageRepository: PdfStorageRepository,
    private val pdfPageRenderer: PdfPageRenderer,
    private val signDocumentUseCase: SignDocumentUseCase,
    private val savedSignatureStore: SavedSignatureStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignerDocumentSignUiState())
    val uiState: StateFlow<SignerDocumentSignUiState> = _uiState.asStateFlow()

    var savingDelayMillis: Long = 2000L

    private var loadedKey: String? = null

    fun loadForSigning(documentId: String, signerId: String) {
        val key = "$documentId:$signerId"
        if (documentId.isBlank() || signerId.isBlank() || loadedKey == key) return
        loadedKey = key
        _uiState.update { it.copy(signerId = signerId) }

        viewModelScope.launch {
            val savedSets = withContext(Dispatchers.IO) { savedSignatureStore.list(signerId) }
            _uiState.update { it.copy(savedSignatureSets = savedSets) }
        }

        viewModelScope.launch {
            val myFieldsRes = documentRepository.getSignatureFieldsBySigner(documentId, signerId)
            val initialFields = myFieldsRes.getOrNull().orEmpty()
            // O documento sempre abre na página 1 (o signatário lê desde o início).
            val targetPage = 0

            _uiState.update {
                it.copy(
                    myFields = initialFields,
                    currentPageIndex = targetPage
                )
            }

            val docRes = documentRepository.getDocumentById(documentId)
            val doc = docRes.getOrNull()
            if (doc != null) {
                _uiState.update { it.copy(document = doc) }
                when (val fileRes = pdfStorageRepository.getPdfFile(doc.fileName)) {
                    is AppResult.Success -> {
                        _uiState.update { it.copy(pdfFile = fileRes.data) }
                        renderPageInternal(fileRes.data, targetPage)
                    }
                    is AppResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingPage = false,
                                snackbarMessage = fileRes.message,
                                isErrorSnackbar = true
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            documentRepository.observeDocumentSigners(documentId).collect { list ->
                val myEntry = list.firstOrNull { it.signer.id == signerId }
                if (myEntry != null) {
                    val fullName = myEntry.signer.name
                    val initials = extractInitials(fullName)
                    _uiState.update { state ->
                        state.copy(
                            signerName = fullName,
                            mySignatureStatus = myEntry.documentSigner.status,
                            typedSignatureText = if (state.typedSignatureText == "Rafael Tavares") fullName else state.typedSignatureText,
                            typedInitialsText = if (state.typedInitialsText == "RT") initials else state.typedInitialsText
                        )
                    }
                }
            }
        }
    }

    private suspend fun renderPageInternal(file: File, pageIndex: Int) {
        _uiState.update { it.copy(isLoadingPage = true) }
        when (val res = pdfPageRenderer.renderPage(file, pageIndex)) {
            is AppResult.Success -> {
                _uiState.update {
                    it.copy(
                        renderedPage = res.data,
                        currentPageIndex = res.data.pageIndex,
                        totalPages = res.data.pageCount,
                        isLoadingPage = false
                    )
                }
            }
            is AppResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoadingPage = false,
                        snackbarMessage = res.message,
                        isErrorSnackbar = true
                    )
                }
            }
        }
    }

    fun onSelectPage(pageIndex: Int) {
        val state = _uiState.value
        val file = state.pdfFile ?: return
        if (pageIndex < 0 || pageIndex >= state.totalPages || pageIndex == state.currentPageIndex) return

        viewModelScope.launch {
            renderPageInternal(file, pageIndex)
        }
    }

    fun onFocusMyNextField() {
        val state = _uiState.value
        val file = state.pdfFile ?: return
        val firstFieldPage = state.myFields.firstOrNull()?.pageNumber ?: return
        if (firstFieldPage != state.currentPageIndex) {
            viewModelScope.launch {
                renderPageInternal(file, firstFieldPage)
            }
        }
    }

    // --- Etapa 1 -> Etapa 2 (Sua assinatura) ---

    /**
     * Botão principal da Etapa 1. Cadastrar/escolher a assinatura e ASSINAR são atos separados:
     * - Assinatura já definida para este documento -> abre "Assinar o documento?" (toque intencional).
     * - Existem assinaturas salvas -> abre a escolha: usar uma salva ou criar uma nova.
     * - Nenhuma assinatura -> abre o cadastro (assinatura + rubrica).
     */
    fun onOpenDrawingModal() {
        val state = _uiState.value
        if (!state.canSign) return
        val nextStep = when {
            state.hasSignatureReadyForThisDocument -> SignWizardStep.STEP_3_CONFIRM
            state.hasSavedSignatures -> SignWizardStep.STEP_2_CHOOSE_SAVED
            else -> SignWizardStep.STEP_2_CAPTURE_SIGNATURE
        }
        _uiState.update {
            it.copy(
                wizardStep = nextStep,
                saveForFutureUse = if (nextStep == SignWizardStep.STEP_2_CAPTURE_SIGNATURE) false else it.saveForFutureUse,
                errorMessage = null
            )
        }
    }

    /** Escolheu uma assinatura salva: define para este documento e volta para a revisão. */
    fun onUseSavedSignatureSet(set: SavedSignatureSet) {
        _uiState.update {
            it.copy(
                capturedSignatureBytes = set.signaturePng,
                capturedInitialsBytes = set.initialsPng,
                wizardStep = SignWizardStep.STEP_1_READ_DOCUMENT,
                snackbarMessage = SIGNATURE_SELECTED_MESSAGE,
                isErrorSnackbar = false
            )
        }
    }

    /** Preferiu criar uma nova assinatura (que também pode ser salva no fim do cadastro). */
    fun onStartNewSignature() {
        _uiState.update {
            it.copy(
                wizardStep = SignWizardStep.STEP_2_CAPTURE_SIGNATURE,
                saveForFutureUse = false,
                errorMessage = null
            )
        }
    }

    fun onSaveForFutureUseChanged(save: Boolean) {
        _uiState.update { it.copy(saveForFutureUse = save) }
    }

    /** Fecha a confirmação "Assinar o documento?" sem assinar. */
    fun onCancelConfirm() {
        if (_uiState.value.isSubmittingSignature) return
        _uiState.update { it.copy(wizardStep = SignWizardStep.STEP_1_READ_DOCUMENT) }
    }

    fun onDismissDrawingModal() {
        if (_uiState.value.isSubmittingSignature) return
        _uiState.update {
            it.copy(
                wizardStep = SignWizardStep.STEP_1_READ_DOCUMENT,
                errorMessage = null
            )
        }
    }

    fun onSignatureCaptureModeChanged(mode: SignatureCaptureMode) {
        _uiState.update { it.copy(signatureMode = mode) }
    }

    fun onTypedSignatureTextChanged(text: String) {
        _uiState.update { it.copy(typedSignatureText = text) }
    }

    fun onTypedSignatureFontChanged(font: CursiveFontStyle) {
        _uiState.update { it.copy(typedSignatureFont = font) }
    }

    /**
     * Chamado quando o usuário clica em "Usar assinatura" (Assinar Captura, Desenhada ou Digitar):
     * guarda os bytes da assinatura e avança para "Sua rubrica" (Assinar Rubrica).
     */
    fun onSignatureCapturedAdvanceToInitials(signaturePngBytes: ByteArray) {
        _uiState.update {
            it.copy(
                capturedSignatureBytes = signaturePngBytes,
                wizardStep = SignWizardStep.STEP_2_CAPTURE_INITIALS
            )
        }
    }

    // --- Etapa 2 (Sua rubrica) -> Etapa 3 (Confirmar) ---

    fun onInitialsCaptureModeChanged(mode: SignatureCaptureMode) {
        _uiState.update { it.copy(initialsMode = mode) }
    }

    fun onTypedInitialsTextChanged(text: String) {
        _uiState.update { it.copy(typedInitialsText = text) }
    }

    fun onTypedInitialsFontChanged(font: CursiveFontStyle) {
        _uiState.update { it.copy(typedInitialsFont = font) }
    }

    /**
     * Chamado quando o usuário clica em "Usar rubrica" (Assinar Rubrica ou Rubrica Digitar):
     * guarda os bytes da rubrica e avança para "Confirmar assinatura e rubrica?" (Etapa 3 de 3).
     */
    fun onInitialsCapturedAdvanceToConfirm(initialsPngBytes: ByteArray) {
        // Fim do CADASTRO: NÃO assina o documento. Salva no banco de assinaturas (se o usuário optou)
        // e volta para o documento; a assinatura só acontece em "Assinar" -> "Assinar documento".
        val state = _uiState.value
        val signatureBytes = state.capturedSignatureBytes ?: return
        viewModelScope.launch {
            val newSet = if (state.saveForFutureUse) {
                withContext(Dispatchers.IO) {
                    savedSignatureStore.save(state.signerId, signatureBytes, initialsPngBytes)
                }
            } else {
                null
            }
            _uiState.update {
                it.copy(
                    capturedInitialsBytes = initialsPngBytes,
                    savedSignatureSets = if (newSet != null) {
                        (listOf(newSet) + it.savedSignatureSets).take(SavedSignatureStore.MAX_SAVED_SETS)
                    } else {
                        it.savedSignatureSets
                    },
                    wizardStep = SignWizardStep.STEP_1_READ_DOCUMENT,
                    snackbarMessage = SIGNATURE_READY_MESSAGE,
                    isErrorSnackbar = false
                )
            }
        }
    }

    /** "Trocar" na confirmação: volta para a escolha (se houver salvas) ou para um novo cadastro. */
    fun onRedoFromConfirm() {
        val state = _uiState.value
        if (state.isSubmittingSignature) return
        _uiState.update {
            it.copy(
                wizardStep = if (state.hasSavedSignatures) {
                    SignWizardStep.STEP_2_CHOOSE_SAVED
                } else {
                    SignWizardStep.STEP_2_CAPTURE_SIGNATURE
                },
                saveForFutureUse = false
            )
        }
    }

    /**
     * Etapa 3 (Confirmar -> Assinar Salvando -> Assinar Concluída / Assinar Falha):
     */
    fun onConfirmAndSaveSignature() {
        val state = _uiState.value
        val sigBytes = state.capturedSignatureBytes ?: return
        onConfirmDrawnSignature(sigBytes, state.capturedInitialsBytes)
    }

    fun onConfirmDrawnSignature(
        signaturePngBytes: ByteArray,
        initialsPngBytes: ByteArray? = _uiState.value.capturedInitialsBytes
    ) {
        val state = _uiState.value
        val doc = state.document ?: return
        if (state.isSubmittingSignature) return

        viewModelScope.launch {
            val nowMillis = System.currentTimeMillis()
            _uiState.update {
                it.copy(
                    isSubmittingSignature = true,
                    signedTimestampMillis = nowMillis,
                    errorMessage = null
                )
            }

            if (savingDelayMillis > 0L) {
                delay(savingDelayMillis)
            }

            when (
                val res = signDocumentUseCase(
                    documentId = doc.id,
                    signerId = state.signerId,
                    signaturePngBytes = signaturePngBytes,
                    initialsPngBytes = initialsPngBytes,
                    signedAtMillis = nowMillis
                )
            ) {
                is AppResult.Success -> {
                    state.pdfFile?.let { renderPageInternal(it, state.currentPageIndex) }
                    _uiState.update {
                        it.copy(
                            isSubmittingSignature = false,
                            wizardStep = SignWizardStep.COMPLETED,
                            mySignatureStatus = SignatureStatus.SIGNED,
                            snackbarMessage = "Documento assinado com sucesso!",
                            isErrorSnackbar = false,
                            isSignedCompleted = true
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingSignature = false,
                            wizardStep = SignWizardStep.FAILURE,
                            errorMessage = res.message,
                            snackbarMessage = res.message,
                            isErrorSnackbar = true
                        )
                    }
                }
            }
        }
    }

    fun onRetrySaveAfterFailure() {
        _uiState.update { it.copy(wizardStep = SignWizardStep.STEP_3_CONFIRM) }
        onConfirmAndSaveSignature()
    }

    fun onReturnToDocumentFromFailure() {
        _uiState.update {
            it.copy(
                wizardStep = SignWizardStep.STEP_1_READ_DOCUMENT,
                errorMessage = null
            )
        }
    }

    companion object {
        const val SIGNATURE_READY_MESSAGE = "Assinatura cadastrada! Revise o documento e toque em Assinar."
        const val SIGNATURE_SELECTED_MESSAGE = "Assinatura selecionada! Revise o documento e toque em Assinar."
    }

    fun onDismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}