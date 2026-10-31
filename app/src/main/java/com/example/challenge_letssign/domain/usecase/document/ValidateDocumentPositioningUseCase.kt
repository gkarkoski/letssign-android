package com.example.challenge_letssign.domain.usecase.document

import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.utils.AppResult
import javax.inject.Inject

/**
 * Regra 1 (Validação de Envio):
 * O documento DRAFT só pode avançar/virar PENDING se possuir ao menos 1 Signatário associado.
 * Além disso, TODO signatário associado deve ter pelo menos um SignatureField posicionado no PDF.
 */
class ValidateDocumentPositioningUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(documentId: String): AppResult<Unit> {
        val signersResult = documentRepository.getDocumentSigners(documentId)
        val signers = when (signersResult) {
            is AppResult.Success -> signersResult.data
            is AppResult.Error -> return signersResult
        }

        if (signers.isEmpty()) {
            return AppResult.Error("O documento deve possuir ao menos 1 signatário associado.")
        }

        val fieldsResult = documentRepository.getSignatureFields(documentId)
        val fields = when (fieldsResult) {
            is AppResult.Success -> fieldsResult.data
            is AppResult.Error -> return fieldsResult
        }

        val signersWithFields = fields.map { it.signerId }.toSet()
        val missingSigners = signers.filter { it.signerId !in signersWithFields }

        if (missingSigners.isNotEmpty()) {
            return AppResult.Error(
                "Posicione ao menos 1 campo de assinatura para cada signatário selecionado."
            )
        }

        return AppResult.Success(Unit)
    }
}
