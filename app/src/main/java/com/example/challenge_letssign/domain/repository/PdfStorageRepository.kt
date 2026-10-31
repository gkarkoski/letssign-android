package com.example.challenge_letssign.domain.repository

import com.example.challenge_letssign.domain.model.ImportedPdfFile
import com.example.challenge_letssign.utils.AppResult
import java.io.File

interface PdfStorageRepository {
    suspend fun importPdfFromUri(uriString: String, fallbackName: String? = null): AppResult<ImportedPdfFile>
    suspend fun getPdfFile(internalFileName: String): AppResult<File>
    suspend fun deletePdfFile(internalFileName: String): AppResult<Unit>
}
