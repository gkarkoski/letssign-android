package com.example.challenge_letssign.domain.usecase.document

import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.utils.AppResult
import javax.inject.Inject

class DeleteSignatureFieldUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(documentId: String, fieldId: String): AppResult<Unit> {
        if (fieldId.isBlank()) {
            return AppResult.Error("Campo de assinatura inválido.")
        }

        // Regra 4 (Imutabilidade)
        if (documentId.isNotBlank()) {
            val docResult = documentRepository.getDocumentById(documentId)
            if (docResult is AppResult.Success && docResult.data.status != DocumentStatus.DRAFT) {
                return AppResult.Error("Este documento já foi enviado e não pode ser alterado.")
            }
        }

        return documentRepository.deleteSignatureField(fieldId)
    }
}
