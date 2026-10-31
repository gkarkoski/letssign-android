package com.example.challenge_letssign.domain.model

data class SignatureField(
    val id: String,
    val documentId: String,
    val signerId: String,
    val pageNumber: Int,
    val relativeX: Float,
    val relativeY: Float,
    val relativeWidth: Float,
    val relativeHeight: Float
)
