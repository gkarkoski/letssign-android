package com.example.challenge_letssign.ui.admin.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentAuditEvent(
    val timestampMillis: Long,
    val title: String,
    val description: String,
    val isSuccess: Boolean = false
)

data class DocumentDetailsUiState(
    val document: Document? = null,
    val signers: List<DocumentSignerWithUser> = emptyList(),
    val fields: List<SignatureField> = emptyList(),
    val isDeleted: Boolean = false
) {
    val isDraft: Boolean
        get() = document?.status == DocumentStatus.DRAFT

    val signedCount: Int
        get() = signers.count { it.documentSigner.status == SignatureStatus.SIGNED }

    val totalSigners: Int
        get() = signers.size

    val auditEvents: List<DocumentAuditEvent>
        get() {
            val doc = document ?: return emptyList()
            val events = mutableListOf<DocumentAuditEvent>()

            events.add(
                DocumentAuditEvent(
                    timestampMillis = doc.createdAt,
                    title = "Documento criado",
                    description = "Arquivo PDF importado pelo administrador."
                )
            )

            if (doc.status != DocumentStatus.DRAFT) {
                events.add(
                    DocumentAuditEvent(
                        timestampMillis = doc.createdAt + 1000L,
                        title = "Enviado para assinatura",
                        description = "Aguardando assinatura de ${signers.size} signatário(s)."
                    )
                )
            }

            signers
                .filter { it.documentSigner.status == SignatureStatus.SIGNED }
                .sortedBy { it.documentSigner.signedAt ?: doc.createdAt }
                .forEach { item ->
                    val signedTime = item.documentSigner.signedAt ?: doc.createdAt
                    val ipLabel = item.documentSigner.signatureIp?.let { " · IP: $it" }.orEmpty()
                    events.add(
                        DocumentAuditEvent(
                            timestampMillis = signedTime,
                            title = "${item.signer.name} assinou o documento",
                            description = "Papel: ${item.documentSigner.roleClass.name}$ipLabel",
                            isSuccess = true
                        )
                    )
                }

            if (doc.status == DocumentStatus.COMPLETED) {
                val lastSignedAt = signers.mapNotNull { it.documentSigner.signedAt }.maxOrNull() ?: doc.createdAt
                events.add(
                    DocumentAuditEvent(
                        timestampMillis = lastSignedAt + 500L,
                        title = "Documento concluído",
                        description = "Todas as assinaturas obrigatórias foram coletadas com validade jurídica.",
                        isSuccess = true
                    )
                )
            }

            return events.sortedByDescending { it.timestampMillis }
        }
}

@HiltViewModel
class DocumentDetailsViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentDetailsUiState())
    val uiState: StateFlow<DocumentDetailsUiState> = _uiState.asStateFlow()

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

    fun onDeleteDraftDocument() {
        val doc = _uiState.value.document ?: return
        // Regra 4: Só permite excluir se estiver em DRAFT
        if (doc.status != DocumentStatus.DRAFT) return

        viewModelScope.launch {
            documentRepository.deleteDocument(doc.id)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }
}
