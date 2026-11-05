package com.example.challenge_letssign.ui.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.repository.PdfStorageRepository
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

data class DocumentViewerUiState(
    val title: String = "",
    val renderedPage: RenderedPdfPage? = null,
    val currentPageIndex: Int = 0,
    val totalPages: Int = 1,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * Visualização somente leitura do PDF de um documento (VER/LER), sem campos nem ações de edição.
 * Mostra o arquivo como está gravado — inclusive com as assinaturas já aplicadas.
 */
@HiltViewModel
class DocumentViewerViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val pdfStorageRepository: PdfStorageRepository,
    private val pdfPageRenderer: PdfPageRenderer
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentViewerUiState())
    val uiState: StateFlow<DocumentViewerUiState> = _uiState.asStateFlow()

    private var pdfFile: File? = null
    private var loadedDocumentId: String? = null

    fun load(documentId: String) {
        if (documentId.isBlank() || loadedDocumentId == documentId) return
        loadedDocumentId = documentId
        viewModelScope.launch {
            when (val docRes = documentRepository.getDocumentById(documentId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(title = docRes.data.title) }
                    when (val fileRes = pdfStorageRepository.getPdfFile(docRes.data.fileName)) {
                        is AppResult.Success -> {
                            pdfFile = fileRes.data
                            renderPage(0)
                        }
                        is AppResult.Error -> showError(fileRes.message)
                    }
                }
                is AppResult.Error -> showError(docRes.message)
            }
        }
    }

    fun onSelectPage(pageIndex: Int) {
        val state = _uiState.value
        if (pageIndex < 0 || pageIndex >= state.totalPages || pageIndex == state.currentPageIndex) return
        viewModelScope.launch { renderPage(pageIndex) }
    }

    private suspend fun renderPage(pageIndex: Int) {
        val file = pdfFile ?: return
        _uiState.update { it.copy(isLoading = true) }
        when (val res = pdfPageRenderer.renderPage(file, pageIndex)) {
            is AppResult.Success -> _uiState.update {
                it.copy(
                    renderedPage = res.data,
                    currentPageIndex = res.data.pageIndex,
                    totalPages = res.data.pageCount,
                    isLoading = false,
                    errorMessage = null
                )
            }
            is AppResult.Error -> showError(res.message)
        }
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = message.ifBlank { "Não foi possível abrir o documento." }
            )
        }
    }
}
