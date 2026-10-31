package com.example.challenge_letssign.ui.admin.positioning

import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.usecase.document.DeleteSignatureFieldUseCase
import com.example.challenge_letssign.domain.usecase.document.SaveSignatureFieldUseCase
import com.example.challenge_letssign.domain.usecase.document.ValidateDocumentPositioningUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class PdfPositioningAndCoordinatesTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeDocRepo: FakePositioningDocumentRepository
    private lateinit var saveFieldUseCase: SaveSignatureFieldUseCase
    private lateinit var deleteFieldUseCase: DeleteSignatureFieldUseCase
    private lateinit var validatePositioningUseCase: ValidateDocumentPositioningUseCase

    @Before
    fun setUp() {
        fakeDocRepo = FakePositioningDocumentRepository()
        saveFieldUseCase = SaveSignatureFieldUseCase(fakeDocRepo)
        deleteFieldUseCase = DeleteSignatureFieldUseCase(fakeDocRepo)
        validatePositioningUseCase = ValidateDocumentPositioningUseCase(fakeDocRepo)
    }

    @Test
    fun `PdfCoordinateConverter converts screen pixels to relative coordinates and back accurately`() {
        val viewWidth = 1000f
        val viewHeight = 1500f

        val rel = PdfCoordinateConverter.pixelsToRelative(
            dropXPx = 250f,
            dropYPx = 600f,
            widthPx = 360f,
            heightPx = 120f,
            viewWidthPx = viewWidth,
            viewHeightPx = viewHeight
        )

        assertEquals(0.25f, rel.relativeX, 0.0001f)
        assertEquals(0.40f, rel.relativeY, 0.0001f)
        assertEquals(0.36f, rel.relativeWidth, 0.0001f)
        assertEquals(0.08f, rel.relativeHeight, 0.0001f)
        assertTrue(PdfCoordinateConverter.isWithinPageBounds(rel))

        val px = PdfCoordinateConverter.relativeToPixels(
            relativeX = rel.relativeX,
            relativeY = rel.relativeY,
            relativeWidth = rel.relativeWidth,
            relativeHeight = rel.relativeHeight,
            viewWidthPx = viewWidth,
            viewHeightPx = viewHeight
        )
        assertEquals(250f, px.xPx, 0.01f)
        assertEquals(600f, px.yPx, 0.01f)
        assertEquals(360f, px.widthPx, 0.01f)
        assertEquals(120f, px.heightPx, 0.01f)
    }

    @Test
    fun `Regra 2 - SaveSignatureFieldUseCase blocks drop outside page boundaries`() = runTest(testDispatcher) {
        fakeDocRepo.setDocument(
            Document("doc-1", "Contrato", "c.pdf", DocumentStatus.DRAFT, 100L, "admin-1")
        )

        // relativeX (0.80) + relativeWidth (0.36) = 1.16 > 1.0 -> Must fail
        val outRight = saveFieldUseCase(
            documentId = "doc-1",
            signerId = "s-1",
            pageNumber = 0,
            relativeX = 0.80f,
            relativeY = 0.50f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f
        )
        assertTrue(outRight is AppResult.Error)
        assertEquals("Campo fora dos limites da página", (outRight as AppResult.Error).message)

        // relativeY (-0.05) < 0.0 -> Must fail
        val outTop = saveFieldUseCase(
            documentId = "doc-1",
            signerId = "s-1",
            pageNumber = 0,
            relativeX = 0.20f,
            relativeY = -0.05f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f
        )
        assertTrue(outTop is AppResult.Error)

        // Valid coordinates -> Must succeed
        val valid = saveFieldUseCase(
            documentId = "doc-1",
            signerId = "s-1",
            pageNumber = 0,
            relativeX = 0.60f,
            relativeY = 0.90f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f
        )
        assertTrue(valid is AppResult.Success)
    }

    @Test
    fun `Regra 4 - SaveSignatureFieldUseCase and DeleteSignatureFieldUseCase reject changes when document is PENDING or COMPLETED`() = runTest(testDispatcher) {
        fakeDocRepo.setDocument(
            Document("doc-pending", "Enviado", "c.pdf", DocumentStatus.PENDING, 100L, "admin-1")
        )

        val saveRes = saveFieldUseCase(
            documentId = "doc-pending",
            signerId = "s-1",
            pageNumber = 0,
            relativeX = 0.2f,
            relativeY = 0.2f
        )
        assertTrue(saveRes is AppResult.Error)
        assertEquals(
            "Este documento já foi enviado e não pode ser alterado.",
            (saveRes as AppResult.Error).message
        )

        val delRes = deleteFieldUseCase("doc-pending", "field-1")
        assertTrue(delRes is AppResult.Error)
    }

    @Test
    fun `Regra 1 - ValidateDocumentPositioningUseCase requires every assigned signer to have at least 1 SignatureField`() = runTest(testDispatcher) {
        val docId = "doc-1"
        fakeDocRepo.setDocument(
            Document(docId, "Contrato", "c.pdf", DocumentStatus.DRAFT, 100L, "admin-1")
        )
        fakeDocRepo.assignSignerToDocument(
            DocumentSigner(docId, "s-1", SignerRoleClass.CONTRATANTE, SignatureStatus.PENDING)
        )
        fakeDocRepo.assignSignerToDocument(
            DocumentSigner(docId, "s-2", SignerRoleClass.TESTEMUNHA, SignatureStatus.PENDING)
        )

        // Only s-1 has a positioned field
        saveFieldUseCase(
            documentId = docId,
            signerId = "s-1",
            pageNumber = 0,
            relativeX = 0.2f,
            relativeY = 0.2f
        )

        val incompleteValidation = validatePositioningUseCase(docId)
        assertFalse(incompleteValidation.isSuccess)
        assertEquals(
            "Posicione ao menos 1 campo de assinatura para cada signatário selecionado.",
            (incompleteValidation as AppResult.Error).message
        )

        // Now s-2 also gets a field
        saveFieldUseCase(
            documentId = docId,
            signerId = "s-2",
            pageNumber = 0,
            relativeX = 0.2f,
            relativeY = 0.5f
        )

        val completeValidation = validatePositioningUseCase(docId)
        assertTrue(completeValidation.isSuccess)
    }

    private class FakePositioningDocumentRepository : DocumentRepository {
        private val docs = mutableMapOf<String, Document>()
        private val signers = mutableListOf<DocumentSigner>()
        private val fields = mutableListOf<SignatureField>()

        fun setDocument(document: Document) {
            docs[document.id] = document
        }

        override fun observeDocumentsByAdmin(adminId: String): Flow<List<Document>> = emptyFlow()
        override fun observeDocumentsBySigner(signerId: String): Flow<List<Document>> = emptyFlow()
        override fun observeDocumentById(documentId: String): Flow<Document?> = emptyFlow()

        override suspend fun getDocumentById(documentId: String): AppResult<Document> =
            docs[documentId]?.let { AppResult.Success(it) } ?: AppResult.Error("Not found")

        override suspend fun createDocument(document: Document): AppResult<Document> {
            docs[document.id] = document
            return AppResult.Success(document)
        }

        override suspend fun updateDocumentStatus(documentId: String, status: DocumentStatus): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun dispatchDocumentForSignature(
            documentId: String,
            sentAt: Long,
            inviteMessage: String
        ): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun deleteDocument(documentId: String): AppResult<Unit> =
            AppResult.Success(Unit)

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
        ): AppResult<Unit> = AppResult.Success(Unit)

        override fun observeSignatureFields(documentId: String): Flow<List<SignatureField>> = emptyFlow()

        override suspend fun getSignatureFields(documentId: String): AppResult<List<SignatureField>> =
            AppResult.Success(fields.filter { it.documentId == documentId })

        override suspend fun getSignatureFieldsBySigner(documentId: String, signerId: String): AppResult<List<SignatureField>> =
            AppResult.Success(fields.filter { it.documentId == documentId && it.signerId == signerId })

        override suspend fun saveSignatureField(field: SignatureField): AppResult<SignatureField> {
            fields.removeAll { it.id == field.id }
            fields.add(field)
            return AppResult.Success(field)
        }

        override suspend fun deleteSignatureField(fieldId: String): AppResult<Unit> {
            fields.removeAll { it.id == fieldId }
            return AppResult.Success(Unit)
        }

        override suspend fun deleteSignatureFieldsBySigner(documentId: String, signerId: String): AppResult<Unit> =
            AppResult.Success(Unit)
    }
}
