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
import kotlinx.coroutines.delay
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
    val isSentSuccess: Boolean = false,
    val sentTimestampMillis: Long = System.currentTimeMillis()
) {
    val signersWithoutFields: List<DocumentSignerWithUser>
        get() {
            val signersWithFields = fields.map { it.signerId }.toSet()
            return signers.filterNot { it.signer.id in signersWithFields }
        }

    val hasPendingFieldError: Boolean
        get() = signers.isNotEmpty() && signersWithoutFields.isNotEmpty()

    val canSend: Boolean
        get() = !isSending && signers.isNotEmpty() && signersWithoutFields.isEmpty()

    val distinctPagesCount: Int
        get() = fields.map { it.pageNumber }.distinct().size
}

@HiltViewModel
class DocumentReviewViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val sendDocumentForSignatureUseCase: SendDocumentForSignatureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentReviewUiState())
    val uiState: StateFlow<DocumentReviewUiState> = _uiState.asStateFlow()

    var sendingDelayMillis: Long = 2000L

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
            val nowMillis = System.currentTimeMillis()
            _uiState.update {
                it.copy(
                    isSending = true,
                    errorMessage = null,
                    sentTimestampMillis = nowMillis
                )
            }

            if (sendingDelayMillis > 0L) {
                delay(sendingDelayMillis)
            }

            when (
                val result = sendDocumentForSignatureUseCase(
                    documentId = docId,
                    inviteMessage = currentState.inviteMessage,
                    sentAtMillis = nowMillis
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

    fun onReturnToReviewFromError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onSentNavigationHandled() {
        _uiState.update { it.copy(isSentSuccess = false) }
    }
}
