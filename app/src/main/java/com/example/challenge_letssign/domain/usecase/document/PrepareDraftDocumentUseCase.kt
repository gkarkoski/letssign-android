package com.example.challenge_letssign.domain.usecase.document

import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.utils.AppResult
import java.util.UUID
import javax.inject.Inject

data class SelectedSignerAssignment(
    val signerId: String,
    val roleClass: SignerRoleClass
)

class PrepareDraftDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(
        existingDocumentId: String? = null,
        title: String,
        internalFileName: String,
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

        val documentId = existingDocumentId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val document = Document(
            id = documentId,
            title = trimmedTitle,
            fileName = internalFileName,
            status = DocumentStatus.DRAFT,
            createdAt = nowUtcMillis,
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

        for (assignment in signerAssignments) {
            val crossRef = DocumentSigner(
                documentId = documentId,
                signerId = assignment.signerId,
                roleClass = assignment.roleClass,
                status = SignatureStatus.PENDING,
                signedAt = null,
                signatureIp = null
            )
            val assignRes = documentRepository.assignSignerToDocument(crossRef)
            if (assignRes is AppResult.Error) return assignRes
        }

        return AppResult.Success(document)
    }
}
