package com.example.challenge_letssign.domain.usecase.signer

import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.repository.DocumentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class SignerAssignedDocument(
    val document: Document,
    val myRoleClass: SignerRoleClass,
    val mySignatureStatus: SignatureStatus,
    val mySignedAt: Long?,
    val mySignatureIp: String?,
    val totalSigners: Int,
    val signedCount: Int
) {
    val isPendingForMe: Boolean
        get() = mySignatureStatus == SignatureStatus.PENDING

    val isSignedByMe: Boolean
        get() = mySignatureStatus == SignatureStatus.SIGNED

    val progressFraction: Float
        get() = if (totalSigners <= 0) 0f else (signedCount.toFloat() / totalSigners.toFloat()).coerceIn(0f, 1f)
}

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveSignerDocumentsUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    operator fun invoke(signerId: String): Flow<List<SignerAssignedDocument>> {
        return documentRepository.observeDocumentsBySigner(signerId)
            .flatMapLatest { documents ->
                if (documents.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val itemFlows = documents.map { doc ->
                        documentRepository.observeDocumentSigners(doc.id).map { signers ->
                            val myEntry = signers.firstOrNull { it.signer.id == signerId }?.documentSigner
                            val total = signers.size
                            val signed = signers.count { it.documentSigner.status == SignatureStatus.SIGNED }
                            SignerAssignedDocument(
                                document = doc,
                                myRoleClass = myEntry?.roleClass ?: SignerRoleClass.CONTRATANTE,
                                mySignatureStatus = myEntry?.status ?: SignatureStatus.PENDING,
                                mySignedAt = myEntry?.signedAt,
                                mySignatureIp = myEntry?.signatureIp,
                                totalSigners = total,
                                signedCount = signed
                            )
                        }
                    }
                    combine(itemFlows) { array -> array.toList() }
                }
            }
    }
}
