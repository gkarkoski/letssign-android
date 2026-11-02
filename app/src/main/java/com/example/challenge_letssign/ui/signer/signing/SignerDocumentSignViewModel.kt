package com.example.challenge_letssign.ui.signer.signing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class SignWizardStep {
    STEP_1_READ_DOCUMENT,
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
    val isSubmittingSignature: Boolean = false,
    val signedTimestampMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val isErrorSnackbar: Boolean = false,
    val isSignedCompleted: Boolean = false
) {
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
            val count = myFields.size
            return when {
                count <= 0 -> "Nenhum campo pendente"
                count == 1 -> "1 campo seu: assinatura"
                hasInitialsField -> "$count campos seus: assinatura e rubrica"
                else -> "$count campos seus: assinatura"
            }
        }

    val topBarStepSubtitle: String
        get() = when (wizardStep) {
            SignWizardStep.STEP_1_READ_DOCUMENT -> "Etapa 1 de 3 · Leia o documento"
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
    private val signDocumentUseCase: SignDocumentUseCase
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
            val myFieldsRes = documentRepository.getSignatureFieldsBySigner(documentId, signerId)
            val initialFields = myFieldsRes.getOrNull().orEmpty()
            val targetPage = initialFields.minOfOrNull { it.pageNumber } ?: 0

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

    fun onOpenDrawingModal() {
        if (!_uiState.value.canSign) return
        _uiState.update {
            it.copy(
                wizardStep = SignWizardStep.STEP_2_CAPTURE_SIGNATURE,
                errorMessage = null
            )
        }
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
        _uiState.update {
            it.copy(
                capturedInitialsBytes = initialsPngBytes,
                wizardStep = SignWizardStep.STEP_3_CONFIRM
            )
        }
    }

    fun onRedoFromConfirm() {
        if (_uiState.value.isSubmittingSignature) return
        _uiState.update {
            it.copy(wizardStep = SignWizardStep.STEP_2_CAPTURE_SIGNATURE)
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

    fun onDismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
