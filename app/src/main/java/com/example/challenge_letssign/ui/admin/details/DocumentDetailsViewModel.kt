package com.example.challenge_letssign.ui.admin.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.utils.DateTimeFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

enum class TimelineStepVisualState {
    COMPLETED_CHECK,
    WAITING_CLOCK,
    FUTURE_DASHED
}

data class DocumentTimelineStep(
    val title: String,
    val subtitle: String,
    val visualState: TimelineStepVisualState
)

data class DocumentDetailsUiState(
    val document: Document? = null,
    val signers: List<DocumentSignerWithUser> = emptyList(),
    val fields: List<SignatureField> = emptyList(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadedFileName: String? = null,
    val snackbarMessage: String? = null,
    val snackbarActionLabel: String? = null,
    val isDeleted: Boolean = false
) {
    val isDraft: Boolean
        get() = document?.status == DocumentStatus.DRAFT

    val signedCount: Int
        get() = signers.count { it.documentSigner.status == SignatureStatus.SIGNED }

    val totalSigners: Int
        get() = signers.size

    val pendingSigners: List<DocumentSignerWithUser>
        get() = signers.filter { it.documentSigner.status == SignatureStatus.PENDING }

    val reminderButtonLabel: String
        get() = if (pendingSigners.size == 1) {
            "Reenviar lembrete para ${pendingSigners.first().signer.name.substringBefore(" ")}"
        } else {
            "Reenviar lembrete"
        }

    /**
     * Linha do tempo "Acompanhamento" exata dos frames Detalhe Enviado, Detalhe Parcial, Detalhe Concluído e Detalhe Baixado.
     */
    val timelineSteps: List<DocumentTimelineStep>
        get() {
            val doc = document ?: return emptyList()
            val steps = mutableListOf<DocumentTimelineStep>()

            val sentTime = doc.sentAt ?: doc.createdAt
            steps.add(
                DocumentTimelineStep(
                    title = if (doc.status == DocumentStatus.DRAFT) "Documento criado" else "Documento enviado",
                    subtitle = DateTimeFormatters.formatTimestampWithAs(sentTime),
                    visualState = TimelineStepVisualState.COMPLETED_CHECK
                )
            )

            val signedList = signers
                .filter { it.documentSigner.status == SignatureStatus.SIGNED }
                .sortedBy { it.documentSigner.signedAt ?: sentTime }

            signedList.forEach { item ->
                val signedAt = item.documentSigner.signedAt ?: sentTime
                steps.add(
                    DocumentTimelineStep(
                        title = "${item.signer.name} assinou",
                        subtitle = DateTimeFormatters.formatTimestampWithAs(signedAt),
                        visualState = TimelineStepVisualState.COMPLETED_CHECK
                    )
                )
            }

            val isDocCompleted = doc.status == DocumentStatus.COMPLETED ||
                (totalSigners > 0 && signedCount == totalSigners)

            if (isDocCompleted) {
                steps.add(
                    DocumentTimelineStep(
                        title = "Concluído",
                        subtitle = "Todas as assinaturas foram coletadas",
                        visualState = TimelineStepVisualState.COMPLETED_CHECK
                    )
                )
            } else {
                val pending = pendingSigners
                if (pending.size == 1) {
                    steps.add(
                        DocumentTimelineStep(
                            title = "Aguardando ${pending.first().signer.name}",
                            subtitle = "1 assinatura pendente",
                            visualState = TimelineStepVisualState.WAITING_CLOCK
                        )
                    )
                } else {
                    steps.add(
                        DocumentTimelineStep(
                            title = "Aguardando assinaturas",
                            subtitle = "$signedCount de $totalSigners concluídas",
                            visualState = TimelineStepVisualState.WAITING_CLOCK
                        )
                    )
                }

                steps.add(
                    DocumentTimelineStep(
                        title = "Concluído",
                        subtitle = "Quando todos assinarem",
                        visualState = TimelineStepVisualState.FUTURE_DASHED
                    )
                )
            }

            return steps
        }

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
                val sentTimestamp = doc.sentAt ?: (doc.createdAt + 1000L)
                val msgPreview = doc.inviteMessage?.takeIf { it.isNotBlank() }?.let { " · Convite: \"$it\"" }.orEmpty()
                events.add(
                    DocumentAuditEvent(
                        timestampMillis = sentTimestamp,
                        title = "Enviado para assinatura",
                        description = "Solicitação disparada para ${signers.size} signatário(s)$msgPreview"
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

    var reloadDelayMillis: Long = 1500L

    private var loadedDocumentId: String? = null

    fun loadDocument(documentId: String) {
        if (documentId.isBlank() || loadedDocumentId == documentId) return
        loadedDocumentId = documentId

        viewModelScope.launch {
            documentRepository.observeDocumentById(documentId).collect { doc ->
                _uiState.update {
                    it.copy(
                        document = doc,
                        isLoading = false,
                        hasError = false
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

    fun onResendReminder() {
        val pending = _uiState.value.pendingSigners
        val msg = if (pending.size == 1) {
            "Lembrete enviado para ${pending.first().signer.name.substringBefore(" ")}."
        } else {
            "Lembrete reenviado para os signatários pendentes."
        }
        _uiState.update {
            it.copy(
                snackbarMessage = msg,
                snackbarActionLabel = null
            )
        }
    }

    fun onDownloadSignedDocument() {
        val doc = _uiState.value.document ?: return
        val baseName = doc.effectiveDisplayFileName
            .removeSuffix(".pdf")
            .removeSuffix(".PDF")
        val signedName = "$baseName-assinado.pdf"
        _uiState.update {
            it.copy(
                isDownloaded = true,
                downloadedFileName = signedName,
                snackbarMessage = "Download concluído",
                snackbarActionLabel = "Abrir"
            )
        }
    }

    fun onRetryLoad() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    hasError = false
                )
            }
            if (reloadDelayMillis > 0L) {
                delay(reloadDelayMillis)
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    hasError = false
                )
            }
        }
    }

    fun onDismissSnackbar() {
        _uiState.update {
            it.copy(
                snackbarMessage = null,
                snackbarActionLabel = null
            )
        }
    }

    fun onDeleteDraftDocument() {
        val doc = _uiState.value.document ?: return
        if (doc.status != DocumentStatus.DRAFT) return

        viewModelScope.launch {
            documentRepository.deleteDocument(doc.id)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }
}
