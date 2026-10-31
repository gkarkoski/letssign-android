package com.example.challenge_letssign.domain.model

enum class SignerRoleClass {
    CONTRATANTE,
    CONTRATADO,
    TESTEMUNHA
}

enum class SignatureStatus {
    PENDING,
    SIGNED
}

data class DocumentSigner(
    val documentId: String,
    val signerId: String,
    val roleClass: SignerRoleClass,
    val status: SignatureStatus,
    val signedAt: Long? = null,
    val signatureIp: String? = null
)

data class DocumentSignerWithUser(
    val documentSigner: DocumentSigner,
    val signer: User
)
