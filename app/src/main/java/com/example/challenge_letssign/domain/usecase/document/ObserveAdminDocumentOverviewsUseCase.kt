package com.example.challenge_letssign.domain.usecase.document

import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.repository.DocumentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class AdminDocumentOverview(
    val document: Document,
    val totalSigners: Int,
    val signedCount: Int
) {
    val isDraft: Boolean
        get() = document.status == DocumentStatus.DRAFT

    val isSentWaitingAll: Boolean
        get() = document.status == DocumentStatus.PENDING && signedCount == 0

    val isPartiallySigned: Boolean
        get() = document.status == DocumentStatus.PENDING && signedCount > 0 && signedCount < totalSigners

    val isCompleted: Boolean
        get() = document.status == DocumentStatus.COMPLETED ||
            (document.status == DocumentStatus.PENDING && totalSigners > 0 && signedCount == totalSigners)

    val progressFraction: Float
        get() = if (totalSigners <= 0) 0f else (signedCount.toFloat() / totalSigners.toFloat()).coerceIn(0f, 1f)
}

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveAdminDocumentOverviewsUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {
    operator fun invoke(adminId: String): Flow<List<AdminDocumentOverview>> {
        return documentRepository.observeDocumentsByAdmin(adminId)
            .flatMapLatest { documents ->
                if (documents.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val itemFlows = documents.map { doc ->
                        documentRepository.observeDocumentSigners(doc.id).map { signers ->
                            val total = signers.size
                            val signed = signers.count { it.documentSigner.status == SignatureStatus.SIGNED }
                            AdminDocumentOverview(
                                document = doc,
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
