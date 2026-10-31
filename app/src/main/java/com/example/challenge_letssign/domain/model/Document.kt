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
    val ownerAdminId: String
)
