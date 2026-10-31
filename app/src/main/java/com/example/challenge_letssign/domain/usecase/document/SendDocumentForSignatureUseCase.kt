package com.example.challenge_letssign.domain.usecase.document

import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.utils.AppResult
import javax.inject.Inject

/**
 * Regra 1 (Validação de Envio) & Slide 19 (Revisão e Disparo):
 * O documento DRAFT só pode virar PENDING se possuir ao menos 1 Signatário associado.
 * Além disso, TODO signatário associado deve ter pelo menos um SignatureField posicionado no PDF.
 * Registra o envio com timestamp real (sentAt) e a mensagem do convite (inviteMessage).
 */
class SendDocumentForSignatureUseCase @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val validateDocumentPositioningUseCase: ValidateDocumentPositioningUseCase
) {
    suspend operator fun invoke(
        documentId: String,
        inviteMessage: String = Document.DEFAULT_INVITE_MESSAGE,
        sentAtMillis: Long = System.currentTimeMillis()
    ): AppResult<Unit> {
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

        val effectiveMessage = inviteMessage.trim().ifBlank { Document.DEFAULT_INVITE_MESSAGE }
        return documentRepository.dispatchDocumentForSignature(
            documentId = documentId,
            sentAt = sentAtMillis,
            inviteMessage = effectiveMessage
        )
    }
}
