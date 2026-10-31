package com.example.challenge_letssign.domain.usecase.document

import com.example.challenge_letssign.domain.model.ImportedPdfFile
import com.example.challenge_letssign.domain.repository.PdfStorageRepository
import com.example.challenge_letssign.utils.AppResult
import javax.inject.Inject

class ImportPdfUseCase @Inject constructor(
    private val pdfStorageRepository: PdfStorageRepository
) {
    suspend operator fun invoke(
        uriString: String,
        fallbackName: String? = null
    ): AppResult<ImportedPdfFile> {
        if (uriString.isBlank()) {
            return AppResult.Error("Selecione um arquivo PDF válido.")
        }
        return pdfStorageRepository.importPdfFromUri(uriString, fallbackName)
    }
}
