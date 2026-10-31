package com.example.challenge_letssign.ui.signer.signing

import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.ImportedPdfFile
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.repository.PdfStorageRepository
import com.example.challenge_letssign.domain.usecase.signer.SignDocumentUseCase
import com.example.challenge_letssign.utils.AppResult
import com.example.challenge_letssign.utils.PdfCoordinateConverter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SignerWorkflowAndPdfBoxTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeDocRepo: FakeSignerDocumentRepository
    private lateinit var fakePdfRepo: FakeSignerPdfStorageRepository
    private lateinit var signDocumentUseCase: SignDocumentUseCase

    @Before
    fun setUp() {
        fakeDocRepo = FakeSignerDocumentRepository()
        fakePdfRepo = FakeSignerPdfStorageRepository()
        signDocumentUseCase = SignDocumentUseCase(fakeDocRepo, fakePdfRepo)
    }

    @Test
    fun `PdfCoordinateConverter relativeToPdfPageCoordinates calculates exact PDFBox bottom-left absolute coordinates`() {
        // Standard A4 cropBox: 595 x 842
        val placement = PdfCoordinateConverter.relativeToPdfPageCoordinates(
            relativeX = 0.20f,
            relativeY = 0.30f,
            relativeWidth = 0.36f,
            relativeHeight = 0.10f,
            cropBoxWidth = 595f,
            cropBoxHeight = 842f
        )

        // absX = 0.20 * 595 = 119.0
        assertEquals(119.0f, placement.absX, 0.01f)
        // absWidth = 0.36 * 595 = 214.2
        assertEquals(214.2f, placement.absWidth, 0.01f)
        // absHeight = 0.10 * 842 = 84.2
        assertEquals(84.2f, placement.absHeight, 0.01f)
        // absY = 842 - (0.30 * 842) - 84.2 = 842 - 252.6 - 84.2 = 505.2
        assertEquals(505.2f, placement.absY, 0.01f)
    }

    @Test
    fun `SignDocumentUseCase embeds signature via PDFBox, logs timestamp + IP, and auto-completes document when last signer signs`() = runTest(testDispatcher) {
        val docId = "doc-sign-1"
        val signer1 = "s-1"
        val signer2 = "s-2"

        fakeDocRepo.createDocument(
            Document(docId, "Contrato de Locação", "locacao.pdf", DocumentStatus.PENDING, 1000L, "admin-1")
        )
        fakeDocRepo.assignSignerToDocument(
            DocumentSigner(docId, signer1, SignerRoleClass.CONTRATANTE, SignatureStatus.PENDING)
        )
        fakeDocRepo.assignSignerToDocument(
            DocumentSigner(docId, signer2, SignerRoleClass.CONTRATADO, SignatureStatus.PENDING)
        )

        fakeDocRepo.saveSignatureField(
            SignatureField("f-1", docId, signer1, 0, 0.2f, 0.7f, 0.36f, 0.08f)
        )
        fakeDocRepo.saveSignatureField(
            SignatureField("f-2", docId, signer2, 1, 0.4f, 0.6f, 0.36f, 0.08f)
        )

        val dummyPng = byteArrayOf(1, 2, 3, 4)

        // Signer 1 signs -> Document stays PENDING
        val res1 = signDocumentUseCase(
            documentId = docId,
            signerId = signer1,
            signaturePngBytes = dummyPng,
            signedAtMillis = 5000L,
            signatureIp = "192.168.1.50"
        )
        assertTrue(res1.isSuccess)
        assertEquals(1, fakePdfRepo.embedCallCount)
        assertEquals(DocumentStatus.PENDING, fakeDocRepo.getDocumentById(docId).getOrNull()?.status)

        // Signer 2 signs -> Regra 5 triggers -> Document becomes COMPLETED
        val res2 = signDocumentUseCase(
            documentId = docId,
            signerId = signer2,
            signaturePngBytes = dummyPng,
            signedAtMillis = 6000L,
            signatureIp = "192.168.1.51"
        )
        assertTrue(res2.isSuccess)
        assertEquals(2, fakePdfRepo.embedCallCount)
        assertEquals(DocumentStatus.COMPLETED, fakeDocRepo.getDocumentById(docId).getOrNull()?.status)

        val signersAfter = fakeDocRepo.getDocumentSigners(docId).getOrNull().orEmpty()
        assertTrue(signersAfter.all { it.status == SignatureStatus.SIGNED })
        assertEquals("192.168.1.50", signersAfter.first { it.signerId == signer1 }.signatureIp)
        assertEquals("192.168.1.51", signersAfter.first { it.signerId == signer2 }.signatureIp)
    }

    @Test
    fun `SignDocumentUseCase rejects empty signature bytes`() = runTest(testDispatcher) {
        val result = signDocumentUseCase(
            documentId = "doc-1",
            signerId = "s-1",
            signaturePngBytes = ByteArray(0)
        )
        assertFalse(result.isSuccess)
        assertEquals("Desenhe sua assinatura antes de confirmar.", (result as AppResult.Error).message)
    }

    private class FakeSignerPdfStorageRepository : PdfStorageRepository {
        var embedCallCount = 0

        override suspend fun importPdfFromUri(
            uriString: String,
            fallbackName: String?
        ): AppResult<ImportedPdfFile> = AppResult.Error("Not used")

        override suspend fun getPdfFile(internalFileName: String): AppResult<File> =
            AppResult.Error("Not used")

        override suspend fun deletePdfFile(internalFileName: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun embedSignaturesIntoPdf(
            internalFileName: String,
            fields: List<SignatureField>,
            signaturePngBytes: ByteArray
        ): AppResult<Unit> {
            embedCallCount++
            return AppResult.Success(Unit)
        }
    }

    private class FakeSignerDocumentRepository : DocumentRepository {
        private val docs = mutableMapOf<String, Document>()
        private val signers = mutableListOf<DocumentSigner>()
        private val fields = mutableListOf<SignatureField>()

        override fun observeDocumentsByAdmin(adminId: String): Flow<List<Document>> = emptyFlow()
        override fun observeDocumentsBySigner(signerId: String): Flow<List<Document>> = emptyFlow()
        override fun observeDocumentById(documentId: String): Flow<Document?> = emptyFlow()

        override suspend fun getDocumentById(documentId: String): AppResult<Document> =
            docs[documentId]?.let { AppResult.Success(it) } ?: AppResult.Error("Not found")

        override suspend fun createDocument(document: Document): AppResult<Document> {
            docs[document.id] = document
            return AppResult.Success(document)
        }

        override suspend fun updateDocumentStatus(documentId: String, status: DocumentStatus): AppResult<Unit> {
            docs[documentId]?.let { docs[documentId] = it.copy(status = status) }
            return AppResult.Success(Unit)
        }

        override suspend fun dispatchDocumentForSignature(
            documentId: String,
            sentAt: Long,
            inviteMessage: String
        ): AppResult<Unit> {
            docs[documentId]?.let {
                docs[documentId] = it.copy(
                    status = DocumentStatus.PENDING,
                    sentAt = sentAt,
                    inviteMessage = inviteMessage
                )
            }
            return AppResult.Success(Unit)
        }

        override suspend fun deleteDocument(documentId: String): AppResult<Unit> = AppResult.Success(Unit)
        override fun observeDocumentSigners(documentId: String): Flow<List<DocumentSignerWithUser>> = emptyFlow()

        override suspend fun getDocumentSigners(documentId: String): AppResult<List<DocumentSigner>> =
            AppResult.Success(signers.filter { it.documentId == documentId })

        override suspend fun assignSignerToDocument(documentSigner: DocumentSigner): AppResult<Unit> {
            signers.add(documentSigner)
            return AppResult.Success(Unit)
        }

        override suspend fun removeSignerFromDocument(documentId: String, signerId: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun markDocumentAsSignedBySigner(
            documentId: String,
            signerId: String,
            signedAt: Long,
            signatureIp: String
        ): AppResult<Unit> {
            val idx = signers.indexOfFirst { it.documentId == documentId && it.signerId == signerId }
            if (idx >= 0) {
                signers[idx] = signers[idx].copy(
                    status = SignatureStatus.SIGNED,
                    signedAt = signedAt,
                    signatureIp = signatureIp
                )
            }
            val pendingLeft = signers.count { it.documentId == documentId && it.status == SignatureStatus.PENDING }
            if (pendingLeft == 0) {
                docs[documentId]?.let { docs[documentId] = it.copy(status = DocumentStatus.COMPLETED) }
            }
            return AppResult.Success(Unit)
        }

        override fun observeSignatureFields(documentId: String): Flow<List<SignatureField>> = emptyFlow()

        override suspend fun getSignatureFields(documentId: String): AppResult<List<SignatureField>> =
            AppResult.Success(fields.filter { it.documentId == documentId })

        override suspend fun getSignatureFieldsBySigner(documentId: String, signerId: String): AppResult<List<SignatureField>> =
            AppResult.Success(fields.filter { it.documentId == documentId && it.signerId == signerId })

        override suspend fun saveSignatureField(field: SignatureField): AppResult<SignatureField> {
            fields.add(field)
            return AppResult.Success(field)
        }

        override suspend fun deleteSignatureField(fieldId: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun deleteSignatureFieldsBySigner(documentId: String, signerId: String): AppResult<Unit> = AppResult.Success(Unit)
    }
}
