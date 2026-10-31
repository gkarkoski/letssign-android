package com.example.challenge_letssign.domain.usecase.document

import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.utils.AppResult
import javax.inject.Inject

/**
 * Regra 1 (Validação de Envio):
 * O documento DRAFT só pode virar PENDING se possuir ao menos 1 Signatário associado.
 * Além disso, TODO signatário associado deve ter pelo menos um SignatureField posicionado no PDF.
 */
class SendDocumentForSignatureUseCase @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val validateDocumentPositioningUseCase: ValidateDocumentPositioningUseCase
) {
    suspend operator fun invoke(documentId: String): AppResult<Unit> {
        if (documentId.isBlank()) {
            return AppResult.Error("Documento inválido.")
        }

        val docResult = documentRepository.getDocumentById(documentId)
        val document = when (docResult) {
            is AppResult.Success -> docResult.data
            is AppResult.Error -> return docResult
        }

        if (document.status != DocumentStatus.DRAFT) {
            return AppResult.Error("Apenas documentos em preparação (rascunho) podem ser enviados.")
        }

        when (val validation = validateDocumentPositioningUseCase(documentId)) {
            is AppResult.Success -> Unit
            is AppResult.Error -> return validation
        }

        return documentRepository.updateDocumentStatus(documentId, DocumentStatus.PENDING)
    }
}
