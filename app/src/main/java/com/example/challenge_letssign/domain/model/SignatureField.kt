package com.example.challenge_letssign.domain.model

enum class SignatureFieldType(val displayLabel: String) {
    SIGNATURE("Assinatura"),
    INITIALS("Rubrica")
}

data class SignatureField(
    val id: String,
    val documentId: String,
    val signerId: String,
    val pageNumber: Int,
    val relativeX: Float,
    val relativeY: Float,
    val relativeWidth: Float,
    val relativeHeight: Float,
    val fieldType: SignatureFieldType = SignatureFieldType.SIGNATURE,
    val isAiSuggested: Boolean = false,
    val aiContextHint: String? = null
)
