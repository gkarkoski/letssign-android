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
    val inviteMessage: String = Document.DEFAULT_INVITE_MESSAGE,
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
                _uiState.update { state ->
                    state.copy(
                        document = doc,
                        inviteMessage = doc?.inviteMessage?.takeIf { it.isNotBlank() }
                            ?: state.inviteMessage
                    )
                }
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

    fun onInviteMessageChanged(newMessage: String) {
        _uiState.update {
            it.copy(
                inviteMessage = newMessage,
                errorMessage = null
            )
        }
    }

    fun onSendDocument() {
        val currentState = _uiState.value
        val docId = currentState.document?.id ?: return
        if (currentState.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }

            when (
                val result = sendDocumentForSignatureUseCase(
                    documentId = docId,
                    inviteMessage = currentState.inviteMessage
                )
            ) {
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
