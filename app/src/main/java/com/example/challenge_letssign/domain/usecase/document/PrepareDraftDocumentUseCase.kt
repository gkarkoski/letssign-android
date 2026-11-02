package com.example.challenge_letssign.domain.usecase.document

import com.example.challenge_letssign.domain.model.AdditionalAuthOption
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerAuthMethod
import com.example.challenge_letssign.domain.model.SignerDeliveryChannel
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.utils.AppResult
import java.util.UUID
import javax.inject.Inject

data class SelectedSignerAssignment(
    val signerId: String,
    val roleClass: SignerRoleClass,
    val deliveryChannel: SignerDeliveryChannel = SignerDeliveryChannel.EMAIL,
    val authMethod: SignerAuthMethod = SignerAuthMethod.EMAIL,
    val additionalAuths: List<AdditionalAuthOption> = emptyList(),
    val language: String = "Português",
    val colorIndex: Int = 0
)

class PrepareDraftDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(
        existingDocumentId: String? = null,
        title: String,
        internalFileName: String,
        originalDisplayName: String = "",
        pageCount: Int = 1,
        fileSizeBytes: Long = 0L,
        ownerAdminId: String,
        signerAssignments: List<SelectedSignerAssignment>,
        nowUtcMillis: Long = System.currentTimeMillis()
    ): AppResult<Document> {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) {
            return AppResult.Error("Informe o título do documento.")
        }
        if (internalFileName.isBlank()) {
            return AppResult.Error("Selecione um arquivo PDF válido.")
        }
        if (ownerAdminId.isBlank()) {
            return AppResult.Error("Administrador inválido.")
        }
        if (signerAssignments.isEmpty()) {
            return AppResult.Error("Selecione ao menos 1 signatário para continuar.")
        }

        if (existingDocumentId != null) {
            val existingDoc = documentRepository.getDocumentById(existingDocumentId).getOrNull()
            if (existingDoc != null && existingDoc.ownerAdminId.isNotBlank()) {
                return prepareInternal(
                    documentId = existingDoc.id,
                    title = trimmedTitle,
                    internalFileName = internalFileName,
                    originalDisplayName = originalDisplayName.ifBlank { existingDoc.originalDisplayName },
                    pageCount = pageCount.coerceAtLeast(1),
                    fileSizeBytes = if (fileSizeBytes > 0L) fileSizeBytes else existingDoc.fileSizeBytes,
                    ownerAdminId = existingDoc.ownerAdminId,
                    signerAssignments = signerAssignments,
                    createdAtMillis = existingDoc.createdAt
                )
            }
        }

        val documentId = existingDocumentId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        return prepareInternal(
            documentId = documentId,
            title = trimmedTitle,
            internalFileName = internalFileName,
            originalDisplayName = originalDisplayName.ifBlank { "$trimmedTitle.pdf" },
            pageCount = pageCount.coerceAtLeast(1),
            fileSizeBytes = fileSizeBytes,
            ownerAdminId = ownerAdminId,
            signerAssignments = signerAssignments,
            createdAtMillis = nowUtcMillis
        )
    }

    private suspend fun prepareInternal(
        documentId: String,
        title: String,
        internalFileName: String,
        originalDisplayName: String,
        pageCount: Int,
        fileSizeBytes: Long,
        ownerAdminId: String,
        signerAssignments: List<SelectedSignerAssignment>,
        createdAtMillis: Long
    ): AppResult<Document> {
        val document = Document(
            id = documentId,
            title = title,
            fileName = internalFileName,
            originalDisplayName = originalDisplayName,
            pageCount = pageCount,
            fileSizeBytes = fileSizeBytes,
            status = DocumentStatus.DRAFT,
            createdAt = createdAtMillis,
            ownerAdminId = ownerAdminId
        )

        val saveDocResult = documentRepository.createDocument(document)
        if (saveDocResult is AppResult.Error) {
            return saveDocResult
        }

        // Sincronizar signatários atribuídos (Regra 3: remover quem saiu excluindo seus campos em cascata)
        val currentSignersResult = documentRepository.getDocumentSigners(documentId)
        val currentSignerIds = currentSignersResult.getOrNull()?.map { it.signerId }?.toSet().orEmpty()
        val newSignerIds = signerAssignments.map { it.signerId }.toSet()

        val removedSignerIds = currentSignerIds - newSignerIds
        for (removedId in removedSignerIds) {
            val removeRes = documentRepository.removeSignerFromDocument(documentId, removedId)
            if (removeRes is AppResult.Error) return removeRes
        }

        signerAssignments.forEachIndexed { index, assignment ->
            val crossRef = DocumentSigner(
                documentId = documentId,
                signerId = assignment.signerId,
                roleClass = assignment.roleClass,
                status = SignatureStatus.PENDING,
                signedAt = null,
                signatureIp = null,
                deliveryChannel = assignment.deliveryChannel,
                authMethod = assignment.authMethod,
                additionalAuths = assignment.additionalAuths,
                language = assignment.language,
                colorIndex = index % 3
            )
            val assignRes = documentRepository.assignSignerToDocument(crossRef)
            if (assignRes is AppResult.Error) return assignRes
        }

        return AppResult.Success(document)
    }
}
