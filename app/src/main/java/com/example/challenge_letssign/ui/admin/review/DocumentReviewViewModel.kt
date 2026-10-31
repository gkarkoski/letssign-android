package com.example.challenge_letssign.ui.admin.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.usecase.document.SendDocumentForSignatureUseCase
import com.example.challenge_letssign.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentReviewUiState(
    val document: Document? = null,
    val signers: List<DocumentSignerWithUser> = emptyList(),
    val fields: List<SignatureField> = emptyList(),
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val isSentSuccess: Boolean = false
) {
    val canSend: Boolean
        get() {
            if (isSending || signers.isEmpty()) return false
            val signersWithFields = fields.map { it.signerId }.toSet()
            return signers.all { it.signer.id in signersWithFields }
        }
}

@HiltViewModel
class DocumentReviewViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val sendDocumentForSignatureUseCase: SendDocumentForSignatureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentReviewUiState())
    val uiState: StateFlow<DocumentReviewUiState> = _uiState.asStateFlow()

    private var loadedDocumentId: String? = null

    fun loadDocument(documentId: String) {
        if (documentId.isBlank() || loadedDocumentId == documentId) return
        loadedDocumentId = documentId

        viewModelScope.launch {
            documentRepository.observeDocumentById(documentId).collect { doc ->
                _uiState.update { it.copy(document = doc) }
            }
        }

        viewModelScope.launch {
            documentRepository.observeDocumentSigners(documentId).collect { signers ->
                _uiState.update { it.copy(signers = signers) }
            }
        }

        viewModelScope.launch {
            documentRepository.observeSignatureFields(documentId).collect { fields ->
                _uiState.update { it.copy(fields = fields) }
            }
        }
    }

    fun onSendDocument() {
        val docId = _uiState.value.document?.id ?: return
        if (_uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }

            when (val result = sendDocumentForSignatureUseCase(docId)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            isSentSuccess = true
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun onSentNavigationHandled() {
        _uiState.update { it.copy(isSentSuccess = false) }
    }
}
