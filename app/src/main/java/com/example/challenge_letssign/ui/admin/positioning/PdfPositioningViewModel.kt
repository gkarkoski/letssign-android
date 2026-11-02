package com.example.challenge_letssign.ui.admin.positioning

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureFieldType
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.repository.PdfStorageRepository
import com.example.challenge_letssign.domain.usecase.document.DeleteSignatureFieldUseCase
import com.example.challenge_letssign.domain.usecase.document.SaveSignatureFieldUseCase
import com.example.challenge_letssign.domain.usecase.document.ValidateDocumentPositioningUseCase
import com.example.challenge_letssign.ui.theme.LetsSignColors
import com.example.challenge_letssign.utils.AppResult
import com.example.challenge_letssign.utils.PdfCoordinateConverter
import com.example.challenge_letssign.utils.PdfPageRenderer
import com.example.challenge_letssign.utils.RenderedPdfPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class SignerColorStyle(
    val borderColor: Color,
    val backgroundColor: Color,
    val textColor: Color
)

object SignerPalette {
    // Cores s1, s2, s3 baseadas na Biblioteca LetsSign (color/signer-1, signer-2, signer-3)
    private val styles = listOf(
        // s1: Violeta Primary (#6d3bf5)
        SignerColorStyle(
            borderColor = LetsSignColors.Signer1,
            backgroundColor = LetsSignColors.Signer1Soft,
            textColor = LetsSignColors.Signer1
        ),
        // s2: Ink / Escuro (#17123a)
        SignerColorStyle(
            borderColor = LetsSignColors.Signer2,
            backgroundColor = LetsSignColors.Signer2Soft,
            textColor = LetsSignColors.Signer2
        ),
        // s3: Ciano / Teal (#0a7886)
        SignerColorStyle(
            borderColor = LetsSignColors.Signer3,
            backgroundColor = LetsSignColors.Signer3Soft,
            textColor = LetsSignColors.Signer3
        )
    )

    fun styleForIndex(index: Int): SignerColorStyle =
        styles[index.coerceAtLeast(0) % styles.size]
}

data class AiFieldSuggestionItem(
    val id: String,
    val signerId: String,
    val pageNumber: Int,
    val relativeX: Float,
    val relativeY: Float,
    val relativeWidth: Float = PdfCoordinateConverter.DEFAULT_RELATIVE_WIDTH,
    val relativeHeight: Float = PdfCoordinateConverter.DEFAULT_RELATIVE_HEIGHT,
    val fieldType: SignatureFieldType = SignatureFieldType.SIGNATURE,
    val contextHint: String = "perto de \"CONTRATANTE\""
)

enum class AiFieldsModalState {
    NONE,
    ANALYZING,
    SUGGESTIONS,
    REVIEWING_SINGLE,
    NOTHING_FOUND,
    ERROR
}

data class PdfPositioningUiState(
    val document: Document? = null,
    val pdfFile: File? = null,
    val renderedPage: RenderedPdfPage? = null,
    val currentPageIndex: Int = 0,
    val totalPages: Int = 1,
    val isLoadingPage: Boolean = true,
    val signers: List<DocumentSignerWithUser> = emptyList(),
    val selectedSignerId: String? = null,
    val selectedFieldId: String? = null,
    val isDraggingOutOfBounds: Boolean = false,
    val allFields: List<SignatureField> = emptyList(),
    // Modal Bottom Sheet: Novo Doc Adicionar Campo
    val isAddFieldSheetOpen: Boolean = false,
    val addFieldPickerSignerId: String? = null,
    // Fluxo de IA nos Campos (Etapa 4)
    val aiModalState: AiFieldsModalState = AiFieldsModalState.NONE,
    val aiSuggestedFields: List<AiFieldSuggestionItem> = emptyList(),
    val reviewingAiFieldId: String? = null,
    val reviewingSignerId: String? = null,
    val reviewingFieldType: SignatureFieldType = SignatureFieldType.SIGNATURE,
    val snackbarMessage: String? = null,
    val isErrorSnackbar: Boolean = false,
    val isReadyForReview: Boolean = false
) {
    val isEditable: Boolean
        get() = document?.status == DocumentStatus.DRAFT

    val currentPageFields: List<SignatureField>
        get() = allFields.filter { it.pageNumber == currentPageIndex }

    val canContinueToReview: Boolean
        get() {
            if (signers.isEmpty()) return false
            val signersWithFields = allFields.map { it.signerId }.toSet()
            return signers.all { it.signer.id in signersWithFields }
        }

    val distinctAiSuggestedPagesCount: Int
        get() = aiSuggestedFields.map { it.pageNumber }.distinct().size.coerceAtLeast(1)

    val reviewingAiSuggestion: AiFieldSuggestionItem?
        get() = aiSuggestedFields.firstOrNull { it.id == reviewingAiFieldId }
}

@HiltViewModel
class PdfPositioningViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val pdfStorageRepository: PdfStorageRepository,
    private val pdfPageRenderer: PdfPageRenderer,
    private val saveSignatureFieldUseCase: SaveSignatureFieldUseCase,
    private val deleteSignatureFieldUseCase: DeleteSignatureFieldUseCase,
    private val validateDocumentPositioningUseCase: ValidateDocumentPositioningUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PdfPositioningUiState())
    val uiState: StateFlow<PdfPositioningUiState> = _uiState.asStateFlow()

    var pageLoadingDelayMillis: Long = 0L
    var aiAnalysisDelayMillis: Long = 2500L

    private var loadedDocumentId: String? = null
    private var aiAnalysisJob: Job? = null

    fun loadDocument(documentId: String) {
        if (documentId.isBlank() || loadedDocumentId == documentId) return
        loadedDocumentId = documentId

        viewModelScope.launch {
            documentRepository.observeDocumentById(documentId).collect { doc ->
                _uiState.update { it.copy(document = doc) }
                if (doc != null && _uiState.value.pdfFile == null) {
                    loadPdfFileAndRenderFirstPage(doc.fileName)
                }
            }
        }

        viewModelScope.launch {
            documentRepository.observeDocumentSigners(documentId).collect { signerList ->
                _uiState.update { state ->
                    val currentSelected = state.selectedSignerId
                    val nextSelected = if (currentSelected != null && signerList.any { it.signer.id == currentSelected }) {
                        currentSelected
                    } else {
                        signerList.firstOrNull()?.signer?.id
                    }
                    state.copy(
                        signers = signerList,
                        selectedSignerId = nextSelected,
                        addFieldPickerSignerId = state.addFieldPickerSignerId ?: nextSelected
                    )
                }
            }
        }

        viewModelScope.launch {
            documentRepository.observeSignatureFields(documentId).collect { fields ->
                _uiState.update { state ->
                    val nextSelectedField = state.selectedFieldId?.takeIf { id -> fields.any { it.id == id } }
                    state.copy(
                        allFields = fields,
                        selectedFieldId = nextSelectedField
                    )
                }
            }
        }
    }

    private suspend fun loadPdfFileAndRenderFirstPage(internalFileName: String) {
        _uiState.update { it.copy(isLoadingPage = true) }
        if (pageLoadingDelayMillis > 0L) {
            delay(pageLoadingDelayMillis)
        }
        when (val fileResult = pdfStorageRepository.getPdfFile(internalFileName)) {
            is AppResult.Success -> {
                _uiState.update { it.copy(pdfFile = fileResult.data) }
                renderPageInternal(fileResult.data, 0)
            }
            is AppResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoadingPage = false,
                        snackbarMessage = fileResult.message,
                        isErrorSnackbar = true
                    )
                }
            }
        }
    }

    private suspend fun renderPageInternal(file: File, pageIndex: Int) {
        _uiState.update { it.copy(isLoadingPage = true) }
        when (val renderResult = pdfPageRenderer.renderPage(file, pageIndex)) {
            is AppResult.Success -> {
                val rendered = renderResult.data
                _uiState.update {
                    it.copy(
                        renderedPage = rendered,
                        currentPageIndex = rendered.pageIndex,
                        totalPages = rendered.pageCount,
                        isLoadingPage = false
                    )
                }
            }
            is AppResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoadingPage = false,
                        snackbarMessage = renderResult.message,
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

    fun onSelectSigner(signerId: String) {
        _uiState.update {
            it.copy(
                selectedSignerId = signerId,
                addFieldPickerSignerId = signerId
            )
        }
    }

    fun onSelectField(fieldId: String?) {
        _uiState.update { state ->
            val field = state.allFields.firstOrNull { it.id == fieldId }
            state.copy(
                selectedFieldId = fieldId,
                selectedSignerId = field?.signerId ?: state.selectedSignerId
            )
        }
    }

    // --- Modal Bottom Sheet: Novo Doc Adicionar Campo ---

    fun onOpenAddFieldSheet() {
        if (!_uiState.value.isEditable) return
        _uiState.update { state ->
            state.copy(
                isAddFieldSheetOpen = true,
                aiModalState = AiFieldsModalState.NONE,
                addFieldPickerSignerId = state.selectedSignerId ?: state.signers.firstOrNull()?.signer?.id
            )
        }
    }

    fun onDismissAddFieldSheet() {
        _uiState.update { it.copy(isAddFieldSheetOpen = false) }
    }

    fun onSelectAddFieldPickerSigner(signerId: String) {
        _uiState.update {
            it.copy(
                addFieldPickerSignerId = signerId,
                selectedSignerId = signerId
            )
        }
    }

    fun onConfirmAddFieldFromSheet() {
        val targetSignerId = _uiState.value.addFieldPickerSignerId
            ?: _uiState.value.selectedSignerId
            ?: _uiState.value.signers.firstOrNull()?.signer?.id
            ?: return
        _uiState.update {
            it.copy(
                selectedSignerId = targetSignerId,
                isAddFieldSheetOpen = false
            )
        }
        onAddSignatureFieldForSelectedSigner()
    }

    fun onAddSignatureFieldForSelectedSigner() {
        val state = _uiState.value
        val doc = state.document ?: return
        if (!state.isEditable) return

        val signerId = state.selectedSignerId ?: state.signers.firstOrNull()?.signer?.id ?: return
        val existingOnPage = state.currentPageFields.size
        val offsetStep = (existingOnPage % 5) * 0.08f
        val initialX = 0.30f.coerceIn(0f, 1f - PdfCoordinateConverter.DEFAULT_RELATIVE_WIDTH)
        val initialY = (0.48f + offsetStep).coerceIn(0f, 1f - PdfCoordinateConverter.DEFAULT_RELATIVE_HEIGHT)

        viewModelScope.launch {
            when (
                val result = saveSignatureFieldUseCase(
                    documentId = doc.id,
                    signerId = signerId,
                    pageNumber = state.currentPageIndex,
                    relativeX = initialX,
                    relativeY = initialY
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(selectedFieldId = result.data.id)
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            snackbarMessage = result.message,
                            isErrorSnackbar = true
                        )
                    }
                }
            }
        }
    }

    // --- Fluxo de IA nos Campos (Novo Doc IA Analisando -> Sugestões -> Revisar Campo / Nada Encontrado / Falha) ---

    fun onSuggestFieldsWithAiClick(
        simulateNothingFound: Boolean = false,
        simulateError: Boolean = false
    ) {
        if (!_uiState.value.isEditable) return
        aiAnalysisJob?.cancel()
        aiAnalysisJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    aiModalState = AiFieldsModalState.ANALYZING,
                    isAddFieldSheetOpen = false
                )
            }

            if (aiAnalysisDelayMillis > 0L) {
                delay(aiAnalysisDelayMillis)
            }

            if (simulateError) {
                _uiState.update { it.copy(aiModalState = AiFieldsModalState.ERROR) }
                return@launch
            }

            val currentSigners = _uiState.value.signers
            if (simulateNothingFound || currentSigners.isEmpty()) {
                _uiState.update { it.copy(aiModalState = AiFieldsModalState.NOTHING_FOUND) }
                return@launch
            }

            val totalPages = _uiState.value.totalPages.coerceAtLeast(1)
            val mainPage = if (totalPages >= 2) 1 else 0
            val lastPage = (totalPages - 1).coerceAtLeast(mainPage)

            val generatedSuggestions = mutableListOf<AiFieldSuggestionItem>()
            currentSigners.forEachIndexed { idx, signerItem ->
                val hint = when (idx) {
                    0 -> "perto de \"CONTRATANTE\""
                    1 -> "perto de \"CONTRATADO\""
                    else -> "perto de \"TESTEMUNHA\""
                }
                val relX = when (idx % 3) {
                    0 -> 0.08f
                    1 -> 0.54f
                    else -> 0.31f
                }
                val relY = when (idx % 3) {
                    0, 1 -> 0.68f
                    else -> 0.82f
                }
                generatedSuggestions.add(
                    AiFieldSuggestionItem(
                        id = UUID.randomUUID().toString(),
                        signerId = signerItem.signer.id,
                        pageNumber = mainPage,
                        relativeX = relX,
                        relativeY = relY,
                        fieldType = SignatureFieldType.SIGNATURE,
                        contextHint = hint
                    )
                )
                // Adiciona rubrica adicional nas páginas para os 2 primeiros signatários (totalizando até 5 campos sugeridos como no Figma)
                if (idx < 2 && totalPages > 1) {
                    generatedSuggestions.add(
                        AiFieldSuggestionItem(
                            id = UUID.randomUUID().toString(),
                            signerId = signerItem.signer.id,
                            pageNumber = if (idx == 0) lastPage else mainPage,
                            relativeX = if (idx == 0) 0.08f else 0.54f,
                            relativeY = 0.56f,
                            relativeWidth = 0.24f,
                            relativeHeight = 0.07f,
                            fieldType = SignatureFieldType.INITIALS,
                            contextHint = hint
                        )
                    )
                }
            }

            _uiState.update {
                it.copy(
                    aiModalState = AiFieldsModalState.SUGGESTIONS,
                    aiSuggestedFields = generatedSuggestions
                )
            }
        }
    }

    fun onCancelAiAnalysis() {
        aiAnalysisJob?.cancel()
        _uiState.update {
            it.copy(
                aiModalState = AiFieldsModalState.NONE,
                reviewingAiFieldId = null
            )
        }
    }

    fun onOpenReviewSingleAiSuggestion(suggestionId: String) {
        _uiState.update { state ->
            val target = state.aiSuggestedFields.firstOrNull { it.id == suggestionId }
                ?: return@update state
            state.copy(
                aiModalState = AiFieldsModalState.REVIEWING_SINGLE,
                reviewingAiFieldId = target.id,
                reviewingSignerId = target.signerId,
                reviewingFieldType = target.fieldType
            )
        }
    }

    fun onChangeReviewingSigner(signerId: String) {
        _uiState.update { it.copy(reviewingSignerId = signerId) }
    }

    fun onChangeReviewingFieldType(fieldType: SignatureFieldType) {
        _uiState.update { it.copy(reviewingFieldType = fieldType) }
    }

    fun onAcceptSingleReviewedAiSuggestion() {
        _uiState.update { state ->
            val targetId = state.reviewingAiFieldId ?: return@update state
            val updatedList = state.aiSuggestedFields.map { item ->
                if (item.id == targetId) {
                    item.copy(
                        signerId = state.reviewingSignerId ?: item.signerId,
                        fieldType = state.reviewingFieldType
                    )
                } else {
                    item
                }
            }
            state.copy(
                aiSuggestedFields = updatedList,
                aiModalState = AiFieldsModalState.SUGGESTIONS,
                reviewingAiFieldId = null
            )
        }
    }

    fun onDiscardSingleReviewedAiSuggestion() {
        _uiState.update { state ->
            val targetId = state.reviewingAiFieldId ?: return@update state
            val remaining = state.aiSuggestedFields.filterNot { it.id == targetId }
            state.copy(
                aiSuggestedFields = remaining,
                aiModalState = if (remaining.isEmpty()) AiFieldsModalState.NONE else AiFieldsModalState.SUGGESTIONS,
                reviewingAiFieldId = null
            )
        }
    }

    fun onAcceptAllAiSuggestions() {
        val state = _uiState.value
        val doc = state.document ?: return
        val suggestions = state.aiSuggestedFields
        if (suggestions.isEmpty()) {
            onCancelAiAnalysis()
            return
        }

        viewModelScope.launch {
            for (suggestion in suggestions) {
                saveSignatureFieldUseCase(
                    documentId = doc.id,
                    signerId = suggestion.signerId,
                    pageNumber = suggestion.pageNumber,
                    relativeX = suggestion.relativeX,
                    relativeY = suggestion.relativeY,
                    relativeWidth = suggestion.relativeWidth,
                    relativeHeight = suggestion.relativeHeight,
                    fieldType = suggestion.fieldType,
                    isAiSuggested = true,
                    aiContextHint = suggestion.contextHint
                )
            }
            _uiState.update {
                it.copy(
                    aiModalState = AiFieldsModalState.NONE,
                    aiSuggestedFields = emptyList(),
                    reviewingAiFieldId = null
                )
            }
        }
    }

    fun onUpdateDraggingOutOfBounds(isOutOfBounds: Boolean) {
        _uiState.update { state ->
            if (state.isDraggingOutOfBounds == isOutOfBounds) return@update state
            state.copy(
                isDraggingOutOfBounds = isOutOfBounds,
                snackbarMessage = if (isOutOfBounds) {
                    SaveSignatureFieldUseCase.OUT_OF_BOUNDS_MESSAGE
                } else {
                    state.snackbarMessage
                },
                isErrorSnackbar = if (isOutOfBounds) true else state.isErrorSnackbar
            )
        }
    }

    /**
     * Matemática de Coordenadas (CRÍTICO - Milestone 5):
     * Ao dar drop na caixa, captura dropX, dropY, width, height em pixels da tela,
     * converte para relativo (relativeX = dropX / viewWidth) e salva no SignatureFieldEntity.
     * Se sair dos limites (Regra 2), bloqueia o drop e exibe a snackbar de erro.
     */
    fun onDropSignatureFieldInPixels(
        field: SignatureField,
        dropXPx: Float,
        dropYPx: Float,
        widthPx: Float,
        heightPx: Float,
        viewWidthPx: Float,
        viewHeightPx: Float
    ): Boolean {
        val state = _uiState.value
        if (!state.isEditable) return false
        if (viewWidthPx <= 0f || viewHeightPx <= 0f) return false

        val relativeCoords = PdfCoordinateConverter.pixelsToRelative(
            dropXPx = dropXPx,
            dropYPx = dropYPx,
            widthPx = widthPx,
            heightPx = heightPx,
            viewWidthPx = viewWidthPx,
            viewHeightPx = viewHeightPx
        )

        if (!PdfCoordinateConverter.isWithinPageBounds(relativeCoords)) {
            _uiState.update {
                it.copy(
                    isDraggingOutOfBounds = false,
                    snackbarMessage = SaveSignatureFieldUseCase.OUT_OF_BOUNDS_MESSAGE,
                    isErrorSnackbar = true
                )
            }
            return false
        }

        _uiState.update { it.copy(isDraggingOutOfBounds = false) }

        viewModelScope.launch {
            val saveRes = saveSignatureFieldUseCase(
                fieldId = field.id,
                documentId = field.documentId,
                signerId = field.signerId,
                pageNumber = field.pageNumber,
                relativeX = relativeCoords.relativeX,
                relativeY = relativeCoords.relativeY,
                relativeWidth = relativeCoords.relativeWidth,
                relativeHeight = relativeCoords.relativeHeight,
                fieldType = field.fieldType,
                isAiSuggested = field.isAiSuggested,
                aiContextHint = field.aiContextHint
            )
            if (saveRes is AppResult.Error) {
                _uiState.update {
                    it.copy(
                        snackbarMessage = saveRes.message,
                        isErrorSnackbar = true
                    )
                }
            }
        }
        return true
    }

    fun onDeleteSignatureField(fieldId: String) {
        val docId = _uiState.value.document?.id ?: return
        if (!_uiState.value.isEditable) return

        viewModelScope.launch {
            val result = deleteSignatureFieldUseCase(docId, fieldId)
            if (result is AppResult.Error) {
                _uiState.update {
                    it.copy(
                        snackbarMessage = result.message,
                        isErrorSnackbar = true
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        selectedFieldId = if (state.selectedFieldId == fieldId) null else state.selectedFieldId
                    )
                }
            }
        }
    }

    fun onContinueToReview(allowWithPendingFields: Boolean = false) {
        val docId = _uiState.value.document?.id ?: return
        if (allowWithPendingFields) {
            _uiState.update { it.copy(isReadyForReview = true) }
            return
        }
        viewModelScope.launch {
            when (val validation = validateDocumentPositioningUseCase(docId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isReadyForReview = true) }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            snackbarMessage = validation.message,
                            isErrorSnackbar = true
                        )
                    }
                }
            }
        }
    }

    fun onReviewNavigationHandled() {
        _uiState.update { it.copy(isReadyForReview = false) }
    }

    fun onDismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    override fun onCleared() {
        aiAnalysisJob?.cancel()
        super.onCleared()
    }
}
