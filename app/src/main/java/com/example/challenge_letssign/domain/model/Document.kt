package com.example.challenge_letssign.domain.model

enum class DocumentStatus {
    DRAFT,
    PENDING,
    COMPLETED
}

data class Document(
    val id: String,
    val title: String,
    val fileName: String,
    val status: DocumentStatus,
    val createdAt: Long,
    val ownerAdminId: String,
    val sentAt: Long? = null,
    val inviteMessage: String? = null,
    val pageCount: Int = 1,
    val fileSizeBytes: Long = 0L,
    val deadlineAt: Long? = null,
    val originalDisplayName: String = ""
) {
    val effectiveDisplayFileName: String
        get() = originalDisplayName.ifBlank {
            if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"
        }

    companion object {
        const val DEFAULT_INVITE_MESSAGE =
            "Olá! Segue o contrato de serviços para a sua assinatura. Qualquer dúvida, é só responder este e-mail."
    }
}
