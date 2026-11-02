package com.example.challenge_letssign.domain.usecase.document

import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureFieldType
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.utils.AppResult
import com.example.challenge_letssign.utils.PdfCoordinateConverter
import java.util.UUID
import javax.inject.Inject

class SaveSignatureFieldUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(
        fieldId: String? = null,
        documentId: String,
        signerId: String,
        pageNumber: Int,
        relativeX: Float,
        relativeY: Float,
        relativeWidth: Float = PdfCoordinateConverter.DEFAULT_RELATIVE_WIDTH,
        relativeHeight: Float = PdfCoordinateConverter.DEFAULT_RELATIVE_HEIGHT,
        fieldType: SignatureFieldType = SignatureFieldType.SIGNATURE,
        isAiSuggested: Boolean = false,
        aiContextHint: String? = null
    ): AppResult<SignatureField> {
        if (documentId.isBlank() || signerId.isBlank()) {
            return AppResult.Error("Documento ou signatário inválido.")
        }
        if (pageNumber < 0) {
            return AppResult.Error("Página inválida.")
        }

        // Regra 4 (Imutabilidade): Se status for PENDING ou COMPLETED, bloquear edição de campos
        val docResult = documentRepository.getDocumentById(documentId)
        if (docResult is AppResult.Success && docResult.data.status != DocumentStatus.DRAFT) {
            return AppResult.Error("Este documento já foi enviado e não pode ser alterado.")
        }

        // Regra 2 (Limites do PDF): relativeX + relativeWidth <= 1.0 e relativeY + relativeHeight <= 1.0
        if (!PdfCoordinateConverter.isWithinPageBounds(relativeX, relativeY, relativeWidth, relativeHeight)) {
            return AppResult.Error(OUT_OF_BOUNDS_MESSAGE)
        }

        val normalizedX = relativeX.coerceIn(0f, 1f - relativeWidth)
        val normalizedY = relativeY.coerceIn(0f, 1f - relativeHeight)

        val field = SignatureField(
            id = fieldId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
            documentId = documentId,
            signerId = signerId,
            pageNumber = pageNumber,
            relativeX = normalizedX,
            relativeY = normalizedY,
            relativeWidth = relativeWidth,
            relativeHeight = relativeHeight,
            fieldType = fieldType,
            isAiSuggested = isAiSuggested,
            aiContextHint = aiContextHint
        )
        return documentRepository.saveSignatureField(field)
    }

    companion object {
        const val OUT_OF_BOUNDS_MESSAGE = "O campo precisa ficar dentro da página."
    }
}
