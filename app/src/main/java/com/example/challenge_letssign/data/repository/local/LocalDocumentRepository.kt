package com.example.challenge_letssign.data.repository.local

import com.example.challenge_letssign.data.di.IoDispatcher
import com.example.challenge_letssign.data.local.dao.DocumentDao
import com.example.challenge_letssign.data.local.dao.SignatureFieldDao
import com.example.challenge_letssign.data.local.entity.toDomain
import com.example.challenge_letssign.data.local.entity.toEntity
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.utils.AppResult
import com.example.challenge_letssign.utils.runCatchingResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDocumentRepository @Inject constructor(
    private val documentDao: DocumentDao,
    private val signatureFieldDao: SignatureFieldDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : DocumentRepository {

    override fun observeDocumentsByAdmin(adminId: String): Flow<List<Document>> =
        documentDao.observeDocumentsByAdmin(adminId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override fun observeDocumentsBySigner(signerId: String): Flow<List<Document>> =
        documentDao.observeDocumentsBySigner(signerId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override fun observeDocumentById(documentId: String): Flow<Document?> =
        documentDao.observeDocumentById(documentId)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)

    override suspend fun getDocumentById(documentId: String): AppResult<Document> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao buscar documento.") {
                val entity = documentDao.getDocumentById(documentId)
                    ?: throw NoSuchElementException("Documento não encontrado.")
                entity.toDomain()
            }
        }

    override suspend fun createDocument(document: Document): AppResult<Document> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao criar documento.") {
                documentDao.insertDocument(document.toEntity())
                document
            }
        }

    override suspend fun updateDocumentStatus(
        documentId: String,
        status: DocumentStatus
    ): AppResult<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Erro ao atualizar status do documento.") {
            documentDao.updateDocumentStatus(documentId, status)
        }
    }

    override suspend fun deleteDocument(documentId: String): AppResult<Unit> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao excluir documento.") {
                documentDao.deleteDocumentById(documentId)
            }
        }

    override fun observeDocumentSigners(documentId: String): Flow<List<DocumentSignerWithUser>> =
        documentDao.observeDocumentSignersWithUsers(documentId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun getDocumentSigners(documentId: String): AppResult<List<DocumentSigner>> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao buscar signatários do documento.") {
                documentDao.getDocumentSigners(documentId).map { it.toDomain() }
            }
        }

    override suspend fun assignSignerToDocument(documentSigner: DocumentSigner): AppResult<Unit> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao vincular signatário ao documento.") {
                documentDao.upsertDocumentSigner(documentSigner.toEntity())
            }
        }

    override suspend fun removeSignerFromDocument(
        documentId: String,
        signerId: String
    ): AppResult<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Erro ao remover signatário do documento.") {
            // Regra 3: Reatividade de Exclusão - Excluir em cascata os SignatureFieldEntity associados
            documentDao.removeSignerWithCascadeFields(documentId, signerId)
        }
    }

    override suspend fun markDocumentAsSignedBySigner(
        documentId: String,
        signerId: String,
        signedAt: Long,
        signatureIp: String
    ): AppResult<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Erro ao registrar assinatura.") {
            // Regra 5: Conclusão Automática - Atualizar para SIGNED e concluir documento se não houver mais PENDING
            documentDao.signAndAutoCompleteIfAllSigned(
                documentId = documentId,
                signerId = signerId,
                signedAt = signedAt,
                signatureIp = signatureIp
            )
        }
    }

    override fun observeSignatureFields(documentId: String): Flow<List<SignatureField>> =
        signatureFieldDao.observeSignatureFieldsByDocument(documentId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun getSignatureFields(documentId: String): AppResult<List<SignatureField>> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao buscar campos de assinatura.") {
                signatureFieldDao.getSignatureFieldsByDocument(documentId).map { it.toDomain() }
            }
        }

    override suspend fun getSignatureFieldsBySigner(
        documentId: String,
        signerId: String
    ): AppResult<List<SignatureField>> = withContext(ioDispatcher) {
        runCatchingResult("Erro ao buscar campos de assinatura do signatário.") {
            signatureFieldDao.getSignatureFieldsBySigner(documentId, signerId).map { it.toDomain() }
        }
    }

    override suspend fun saveSignatureField(field: SignatureField): AppResult<SignatureField> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao salvar campo de assinatura.") {
                signatureFieldDao.upsertSignatureField(field.toEntity())
                field
            }
        }

    override suspend fun deleteSignatureField(fieldId: String): AppResult<Unit> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao remover campo de assinatura.") {
                signatureFieldDao.deleteSignatureFieldById(fieldId)
            }
        }

    override suspend fun deleteSignatureFieldsBySigner(
        documentId: String,
        signerId: String
    ): AppResult<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Erro ao remover campos de assinatura do signatário.") {
            signatureFieldDao.deleteSignatureFieldsBySigner(documentId, signerId)
        }
    }
}
