package com.example.challenge_letssign.data.repository.local

import com.example.challenge_letssign.data.local.dao.DocumentDao
import com.example.challenge_letssign.data.local.dao.SignatureFieldDao
import com.example.challenge_letssign.data.local.entity.DocumentEntity
import com.example.challenge_letssign.data.local.entity.DocumentSignerCrossRef
import com.example.challenge_letssign.data.local.entity.DocumentSignerWithUserEntity
import com.example.challenge_letssign.data.local.entity.SignatureFieldEntity
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerRoleClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocalDocumentRepositoryTest {

    private lateinit var fakeFieldDao: FakeSignatureFieldDao
    private lateinit var fakeDocumentDao: FakeDocumentDao
    private lateinit var repository: LocalDocumentRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        fakeFieldDao = FakeSignatureFieldDao()
        fakeDocumentDao = FakeDocumentDao(fakeFieldDao)
        repository = LocalDocumentRepository(
            documentDao = fakeDocumentDao,
            signatureFieldDao = fakeFieldDao,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `Regra 3 - removing signer from document should cascade delete their signature fields`() = runTest(testDispatcher) {
        val docId = "doc-1"
        val signer1 = "signer-1"
        val signer2 = "signer-2"

        repository.createDocument(
            Document(
                id = docId,
                title = "Contrato",
                fileName = "contrato.pdf",
                status = DocumentStatus.DRAFT,
                createdAt = 1000L,
                ownerAdminId = "admin-1"
            )
        )

        repository.assignSignerToDocument(
            DocumentSigner(docId, signer1, SignerRoleClass.CONTRATANTE, SignatureStatus.PENDING)
        )
        repository.assignSignerToDocument(
            DocumentSigner(docId, signer2, SignerRoleClass.TESTEMUNHA, SignatureStatus.PENDING)
        )

        repository.saveSignatureField(
            SignatureField("f-1", docId, signer1, 0, 0.1f, 0.1f, 0.2f, 0.1f)
        )
        repository.saveSignatureField(
            SignatureField("f-2", docId, signer1, 1, 0.3f, 0.3f, 0.2f, 0.1f)
        )
        repository.saveSignatureField(
            SignatureField("f-3", docId, signer2, 0, 0.5f, 0.5f, 0.2f, 0.1f)
        )

        // Act: remove signer1
        val removeResult = repository.removeSignerFromDocument(docId, signer1)
        assertTrue(removeResult.isSuccess)

        // Assert: signer1's fields are removed, signer2's field remains
        val remainingFields = repository.getSignatureFields(docId).getOrNull().orEmpty()
        assertEquals(1, remainingFields.size)
        assertEquals("f-3", remainingFields.first().id)

        val remainingSigners = repository.getDocumentSigners(docId).getOrNull().orEmpty()
        assertEquals(1, remainingSigners.size)
        assertEquals(signer2, remainingSigners.first().signerId)
    }

    @Test
    fun `Regra 5 - signing document updates status to COMPLETED only when all signers have signed`() = runTest(testDispatcher) {
        val docId = "doc-2"
        val signer1 = "signer-1"
        val signer2 = "signer-2"

        repository.createDocument(
            Document(
                id = docId,
                title = "Acordo NDA",
                fileName = "nda.pdf",
                status = DocumentStatus.PENDING,
                createdAt = 2000L,
                ownerAdminId = "admin-1"
            )
        )

        repository.assignSignerToDocument(
            DocumentSigner(docId, signer1, SignerRoleClass.CONTRATANTE, SignatureStatus.PENDING)
        )
        repository.assignSignerToDocument(
            DocumentSigner(docId, signer2, SignerRoleClass.CONTRATADO, SignatureStatus.PENDING)
        )

        // First signer signs -> Document must remain PENDING
        repository.markDocumentAsSignedBySigner(
            documentId = docId,
            signerId = signer1,
            signedAt = 3000L,
            signatureIp = "10.0.2.15"
        )
        val docAfterFirstSign = repository.getDocumentById(docId).getOrNull()!!
        assertEquals(DocumentStatus.PENDING, docAfterFirstSign.status)

        // Second signer signs -> No PENDING signers left -> Document must become COMPLETED
        repository.markDocumentAsSignedBySigner(
            documentId = docId,
            signerId = signer2,
            signedAt = 4000L,
            signatureIp = "10.0.2.16"
        )
        val docAfterSecondSign = repository.getDocumentById(docId).getOrNull()!!
        assertEquals(DocumentStatus.COMPLETED, docAfterSecondSign.status)

        val signers = repository.getDocumentSigners(docId).getOrNull().orEmpty()
        assertTrue(signers.all { it.status == SignatureStatus.SIGNED })
    }

    @Test
    fun `Regra 4 - observeDocumentsBySigner should only emit non-DRAFT documents assigned to that signer`() = runTest(testDispatcher) {
        val signerId = "signer-1"

        repository.createDocument(
            Document("doc-draft", "Rascunho", "d1.pdf", DocumentStatus.DRAFT, 100L, "admin-1")
        )
        repository.createDocument(
            Document("doc-pending", "Pendente", "d2.pdf", DocumentStatus.PENDING, 200L, "admin-1")
        )

        repository.assignSignerToDocument(
            DocumentSigner("doc-draft", signerId, SignerRoleClass.CONTRATANTE, SignatureStatus.PENDING)
        )
        repository.assignSignerToDocument(
            DocumentSigner("doc-pending", signerId, SignerRoleClass.CONTRATANTE, SignatureStatus.PENDING)
        )

        val visibleDocs = repository.observeDocumentsBySigner(signerId).first()
        assertEquals(1, visibleDocs.size)
        assertEquals("doc-pending", visibleDocs.first().id)
    }

    private class FakeSignatureFieldDao : SignatureFieldDao {
        val fieldsFlow = MutableStateFlow<List<SignatureFieldEntity>>(emptyList())

        override fun observeSignatureFieldsByDocument(documentId: String): Flow<List<SignatureFieldEntity>> =
            fieldsFlow.map { list -> list.filter { it.documentId == documentId }.sortedBy { it.pageNumber } }

        override suspend fun getSignatureFieldsByDocument(documentId: String): List<SignatureFieldEntity> =
            fieldsFlow.value.filter { it.documentId == documentId }.sortedBy { it.pageNumber }

        override suspend fun getSignatureFieldsBySigner(
            documentId: String,
            signerId: String
        ): List<SignatureFieldEntity> =
            fieldsFlow.value.filter { it.documentId == documentId && it.signerId == signerId }

        override suspend fun upsertSignatureField(field: SignatureFieldEntity) {
            fieldsFlow.value = fieldsFlow.value.filterNot { it.id == field.id } + field
        }

        override suspend fun deleteSignatureFieldById(fieldId: String) {
            fieldsFlow.value = fieldsFlow.value.filterNot { it.id == fieldId }
        }

        override suspend fun deleteSignatureFieldsBySigner(documentId: String, signerId: String) {
            fieldsFlow.value = fieldsFlow.value.filterNot {
                it.documentId == documentId && it.signerId == signerId
            }
        }
    }

    private class FakeDocumentDao(
        private val signatureFieldDao: FakeSignatureFieldDao
    ) : DocumentDao {
        private val docsFlow = MutableStateFlow<List<DocumentEntity>>(emptyList())
        private val crossRefsFlow = MutableStateFlow<List<DocumentSignerCrossRef>>(emptyList())

        override fun observeDocumentsByAdmin(adminId: String): Flow<List<DocumentEntity>> =
            docsFlow.map { list -> list.filter { it.ownerAdminId == adminId } }

        override fun observeDocumentsBySigner(signerId: String): Flow<List<DocumentEntity>> =
            docsFlow.map { docs ->
                val assignedDocIds = crossRefsFlow.value
                    .filter { it.signerId == signerId }
                    .map { it.documentId }
                    .toSet()
                docs.filter { it.id in assignedDocIds && it.status != DocumentStatus.DRAFT }
            }

        override fun observeDocumentById(documentId: String): Flow<DocumentEntity?> =
            docsFlow.map { list -> list.firstOrNull { it.id == documentId } }

        override suspend fun getDocumentById(documentId: String): DocumentEntity? =
            docsFlow.value.firstOrNull { it.id == documentId }

        override suspend fun insertDocument(document: DocumentEntity) {
            docsFlow.value = docsFlow.value.filterNot { it.id == document.id } + document
        }

        override suspend fun updateDocument(document: DocumentEntity) {
            docsFlow.value = docsFlow.value.map { if (it.id == document.id) document else it }
        }

        override suspend fun updateDocumentStatus(documentId: String, status: DocumentStatus) {
            docsFlow.value = docsFlow.value.map {
                if (it.id == documentId) it.copy(status = status) else it
            }
        }

        override suspend fun markDocumentDispatched(
            documentId: String,
            status: DocumentStatus,
            sentAt: Long,
            inviteMessage: String
        ) {
            docsFlow.value = docsFlow.value.map {
                if (it.id == documentId) {
                    it.copy(status = status, sentAt = sentAt, inviteMessage = inviteMessage)
                } else {
                    it
                }
            }
        }

        override suspend fun deleteDocumentById(documentId: String) {
            docsFlow.value = docsFlow.value.filterNot { it.id == documentId }
        }

        override fun observeDocumentSignersWithUsers(documentId: String): Flow<List<DocumentSignerWithUserEntity>> =
            MutableStateFlow(emptyList())

        override suspend fun getDocumentSigners(documentId: String): List<DocumentSignerCrossRef> =
            crossRefsFlow.value.filter { it.documentId == documentId }

        override suspend fun upsertDocumentSigner(crossRef: DocumentSignerCrossRef) {
            crossRefsFlow.value = crossRefsFlow.value.filterNot {
                it.documentId == crossRef.documentId && it.signerId == crossRef.signerId
            } + crossRef
        }

        override suspend fun deleteDocumentSigner(documentId: String, signerId: String) {
            crossRefsFlow.value = crossRefsFlow.value.filterNot {
                it.documentId == documentId && it.signerId == signerId
            }
        }

        override suspend fun updateSignerSignatureStatus(
            documentId: String,
            signerId: String,
            status: SignatureStatus,
            signedAt: Long,
            signatureIp: String
        ) {
            crossRefsFlow.value = crossRefsFlow.value.map {
                if (it.documentId == documentId && it.signerId == signerId) {
                    it.copy(status = status, signedAt = signedAt, signatureIp = signatureIp)
                } else {
                    it
                }
            }
        }

        override suspend fun countPendingSignersForDocument(documentId: String): Int =
            crossRefsFlow.value.count {
                it.documentId == documentId && it.status == SignatureStatus.PENDING
            }

        override suspend fun deleteSignatureFieldsForSigner(documentId: String, signerId: String) {
            signatureFieldDao.deleteSignatureFieldsBySigner(documentId, signerId)
        }
    }
}
