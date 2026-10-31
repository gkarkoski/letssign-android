package com.example.challenge_letssign.ui.admin.positioning

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureField
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class SignerColorStyle(
    val borderColor: Color,
    val backgroundColor: Color,
    val textColor: Color
)

object SignerPalette {
    // Cores s1, s2, s3 baseadas no components2.png ("Campos de assinatura - Uma cor por signatário")
    private val styles = listOf(
        // s1: Violeta Primary
        SignerColorStyle(
            borderColor = LetsSignColors.Primary,
            backgroundColor = LetsSignColors.VioletSoft,
            textColor = LetsSignColors.Primary
        ),
        // s2: Ink / Escuro
        SignerColorStyle(
            borderColor = LetsSignColors.Ink,
            backgroundColor = Color(0xFFECEBF2),
            textColor = LetsSignColors.Ink
        ),
        // s3: Ciano / Teal
        SignerColorStyle(
            borderColor = Color(0xFF0A6B78),
            backgroundColor = Color(0xFFE3F5F8),
            textColor = Color(0xFF0A6B78)
        )
    )

    fun styleForIndex(index: Int): SignerColorStyle =
        styles[index.coerceAtLeast(0) % styles.size]
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
    val allFields: List<SignatureField> = emptyList(),
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

    private var loadedDocumentId: String? = null

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
                        selectedSignerId = nextSelected
                    )
                }
            }
        }

        viewModelScope.launch {
            documentRepository.observeSignatureFields(documentId).collect { fields ->
                _uiState.update { it.copy(allFields = fields) }
            }
        }
    }

    private suspend fun loadPdfFileAndRenderFirstPage(internalFileName: String) {
        _uiState.update { it.copy(isLoadingPage = true) }
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
        _uiState.update { it.copy(selectedSignerId = signerId) }
    }

    fun onAddSignatureFieldForSelectedSigner() {
        val state = _uiState.value
        val doc = state.document ?: return
        if (!state.isEditable) return

        val signerId = state.selectedSignerId ?: state.signers.firstOrNull()?.signer?.id ?: return
        val existingOnPage = state.currentPageFields.size
        // Posiciona inicialmente de forma escalonada dentro dos limites da página
        val offsetStep = (existingOnPage % 5) * 0.06f
        val initialX = (0.32f).coerceIn(0f, 1f - PdfCoordinateConverter.DEFAULT_RELATIVE_WIDTH)
        val initialY = (0.25f + offsetStep).coerceIn(0f, 1f - PdfCoordinateConverter.DEFAULT_RELATIVE_HEIGHT)

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
                is AppResult.Success -> Unit
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

    /**
     * Matemática de Coordenadas (CRÍTICO - Milestone 5):
     * Ao dar drop na caixa, captura dropX, dropY, width, height em pixels da tela,
     * converte para relativo (relativeX = dropX / viewWidth) e salva no SignatureFieldEntity.
     * Se sair dos limites (Regra 2), bloqueia o drop e exibe "Campo fora dos limites da página".
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
                    snackbarMessage = "Campo fora dos limites da página",
                    isErrorSnackbar = true
                )
            }
            return false
        }

        viewModelScope.launch {
            val saveRes = saveSignatureFieldUseCase(
                fieldId = field.id,
                documentId = field.documentId,
                signerId = field.signerId,
                pageNumber = field.pageNumber,
                relativeX = relativeCoords.relativeX,
                relativeY = relativeCoords.relativeY,
                relativeWidth = relativeCoords.relativeWidth,
                relativeHeight = relativeCoords.relativeHeight
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
            }
        }
    }

    fun onContinueToReview() {
        val docId = _uiState.value.document?.id ?: return
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
}
