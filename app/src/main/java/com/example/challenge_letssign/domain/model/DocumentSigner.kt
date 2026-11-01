package com.example.challenge_letssign.domain.model

enum class SignerRoleClass(val displayLabel: String) {
    CONTRATANTE("Contratante"),
    CONTRATADO("Contratado"),
    TESTEMUNHA("Testemunha")
}

enum class SignatureStatus {
    PENDING,
    SIGNED
}

enum class SignerDeliveryChannel(
    val displayLabel: String,
    val description: String
) {
    EMAIL("E-mail", "Link de assinatura enviado por e-mail."),
    SMS("SMS", "Link de assinatura enviado por SMS."),
    WHATSAPP("WhatsApp", "Link de assinatura enviado por WhatsApp."),
    NONE("Não enviar", "Não envia o link de assinatura automaticamente.")
}

enum class SignerAuthMethod(
    val displayLabel: String,
    val description: String
) {
    EMAIL("E-mail", "Código de verificação enviado por e-mail."),
    SMS("SMS", "Código de verificação enviado por SMS."),
    WHATSAPP("WhatsApp", "Código de verificação enviado por WhatsApp."),
    DIGITAL_CERTIFICATE("Certificado Digital", "Assinatura com certificado digital (e-CPF/e-CNPJ)."),
    NO_AUTH("Sem Autenticação", "Assina sem código de verificação, menos seguro.")
}

enum class AdditionalAuthOption(
    val displayLabel: String,
    val description: String
) {
    SELFIE_NO_BIOMETRICS(
        "Selfie (sem biometria facial)",
        "Selfie do signatário, sem verificação biométrica."
    ),
    PHOTO_ID_DOCUMENT(
        "Documento com foto",
        "Foto do documento de identificação com foto."
    ),
    SELFIE_WITH_BIOMETRICS(
        "Selfie (com biometria facial)",
        "Selfie com verificação de biometria facial."
    ),
    SELFIE_BIOMETRICS_LIVENESS(
        "Selfie (com biometria facial e prova de vida)",
        "Selfie com biometria facial e prova de vida."
    ),
    SERVICE_PROOF_PHOTO(
        "Evidência de prestação do serviço",
        "Foto que comprova a prestação do serviço."
    )
}

data class DocumentSigner(
    val documentId: String,
    val signerId: String,
    val roleClass: SignerRoleClass,
    val status: SignatureStatus,
    val signedAt: Long? = null,
    val signatureIp: String? = null,
    val deliveryChannel: SignerDeliveryChannel = SignerDeliveryChannel.EMAIL,
    val authMethod: SignerAuthMethod = SignerAuthMethod.EMAIL,
    val additionalAuths: List<AdditionalAuthOption> = emptyList(),
    val language: String = "Português",
    val colorIndex: Int = 0
)

data class DocumentSignerWithUser(
    val documentSigner: DocumentSigner,
    val signer: User
)
