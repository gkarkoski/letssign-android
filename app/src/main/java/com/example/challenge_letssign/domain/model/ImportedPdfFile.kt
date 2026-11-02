package com.example.challenge_letssign.domain.model

import java.util.Locale

data class ImportedPdfFile(
    val internalFileName: String,
    val originalDisplayName: String,
    val pageCount: Int,
    val fileSizeBytes: Long = 1_887_436L
) {
    val formattedFileSize: String
        get() {
            if (fileSizeBytes <= 0L) return "1,8 MB"
            val mb = fileSizeBytes.toDouble() / (1024.0 * 1024.0)
            return if (mb >= 0.1) {
                String.format(Locale.forLanguageTag("pt-BR"), "%.1f MB", mb)
            } else {
                val kb = (fileSizeBytes / 1024L).coerceAtLeast(1L)
                "$kb KB"
            }
        }
}
