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
    val inviteMessage: String? = null
) {
    companion object {
        const val DEFAULT_INVITE_MESSAGE =
            "Olá! Segue o documento para sua assinatura eletrônica. Qualquer dúvida, é só responder este e-mail."
    }
}
