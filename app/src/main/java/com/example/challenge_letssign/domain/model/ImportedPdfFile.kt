package com.example.challenge_letssign.domain.model

data class ImportedPdfFile(
    val internalFileName: String,
    val originalDisplayName: String,
    val pageCount: Int
)
