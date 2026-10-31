package com.example.challenge_letssign.ui.signer.signing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.repository.PdfStorageRepository
import com.example.challenge_letssign.domain.usecase.signer.SignDocumentUseCase
import com.example.challenge_letssign.utils.AppResult
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

data class SignerDocumentSignUiState(
    val document: Document? = null,
    val signerId: String = "",
    val pdfFile: File? = null,
    val renderedPage: RenderedPdfPage? = null,
    val currentPageIndex: Int = 0,
    val totalPages: Int = 1,
    val isLoadingPage: Boolean = true,
    val myFields: List<SignatureField> = emptyList(),
    val mySignatureStatus: SignatureStatus = SignatureStatus.PENDING,
    val isDrawingModalOpen: Boolean = false,
    val isSubmittingSignature: Boolean = false,
    val snackbarMessage: String? = null,
    val isErrorSnackbar: Boolean = false,
    val isSignedCompleted: Boolean = false
) {
    val canSign: Boolean
        get() = mySignatureStatus == SignatureStatus.PENDING &&
            myFields.isNotEmpty() &&
            !isSubmittingSignature

    val currentPageMyFields: List<SignatureField>
        get() = myFields.filter { it.pageNumber == currentPageIndex }
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

    private var loadedKey: String? = null

    fun loadForSigning(documentId: String, signerId: String) {
        val key = "$documentId:$signerId"
        if (documentId.isBlank() || signerId.isBlank() || loadedKey == key) return
        loadedKey = key
        _uiState.update { it.copy(signerId = signerId) }

        viewModelScope.launch {
            // Busca os campos atribuídos ao usuário logado para focar imediatamente na página correta
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
                val mySigner = list.firstOrNull { it.signer.id == signerId }?.documentSigner
                if (mySigner != null) {
                    _uiState.update { it.copy(mySignatureStatus = mySigner.status) }
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

    fun onOpenDrawingModal() {
        if (!_uiState.value.canSign) return
        _uiState.update { it.copy(isDrawingModalOpen = true) }
    }

    fun onDismissDrawingModal() {
        if (_uiState.value.isSubmittingSignature) return
        _uiState.update { it.copy(isDrawingModalOpen = false) }
    }

    fun onConfirmDrawnSignature(signaturePngBytes: ByteArray) {
        val state = _uiState.value
        val doc = state.document ?: return
        if (state.isSubmittingSignature) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingSignature = true) }

            when (
                val res = signDocumentUseCase(
                    documentId = doc.id,
                    signerId = state.signerId,
                    signaturePngBytes = signaturePngBytes
                )
            ) {
                is AppResult.Success -> {
                    // Re-renderiza a página atual para já mostrar o PDF com a assinatura gravada pelo PDFBox
                    state.pdfFile?.let { renderPageInternal(it, state.currentPageIndex) }
                    _uiState.update {
                        it.copy(
                            isSubmittingSignature = false,
                            isDrawingModalOpen = false,
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
                            snackbarMessage = res.message,
                            isErrorSnackbar = true
                        )
                    }
                }
            }
        }
    }

    fun onDismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
