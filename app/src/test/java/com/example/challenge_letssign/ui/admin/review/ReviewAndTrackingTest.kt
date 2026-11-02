package com.example.challenge_letssign.ui.admin.review

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
import com.example.challenge_letssign.domain.usecase.document.AdminDocumentOverview
import com.example.challenge_letssign.domain.usecase.document.ObserveAdminDocumentOverviewsUseCase
import com.example.challenge_letssign.domain.usecase.document.SendDocumentForSignatureUseCase
import com.example.challenge_letssign.domain.usecase.document.ValidateDocumentPositioningUseCase
import com.example.challenge_letssign.ui.admin.AdminDashboardUiState
import com.example.challenge_letssign.ui.admin.AdminDocumentTab
import com.example.challenge_letssign.ui.admin.details.DocumentDetailsUiState
import com.example.challenge_letssign.utils.AppResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewAndTrackingTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeDocRepo: FakeReviewDocumentRepository
    private lateinit var sendUseCase: SendDocumentForSignatureUseCase
    private lateinit var observeOverviewsUseCase: ObserveAdminDocumentOverviewsUseCase

    @Before
    fun setUp() {
        fakeDocRepo = FakeReviewDocumentRepository()
        val validateUseCase = ValidateDocumentPositioningUseCase(fakeDocRepo)
        sendUseCase = SendDocumentForSignatureUseCase(fakeDocRepo, validateUseCase)
        observeOverviewsUseCase = ObserveAdminDocumentOverviewsUseCase(fakeDocRepo)
    }

    @Test
    fun `Regra 1 - SendDocumentForSignatureUseCase transitions DRAFT to PENDING only when all signers have fields`() = runTest(testDispatcher) {
        val docId = "doc-1"
        fakeDocRepo.createDocument(
            Document(docId, "Contrato de serviços", "c.pdf", DocumentStatus.DRAFT, 1000L, "admin-1")
        )
        fakeDocRepo.assignSignerToDocument(
            DocumentSigner(docId, "s-1", SignerRoleClass.CONTRATANTE, SignatureStatus.PENDING)
        )
        fakeDocRepo.assignSignerToDocument(
            DocumentSigner(docId, "s-2", SignerRoleClass.CONTRATADO, SignatureStatus.PENDING)
        )

        // Only s-1 has field -> Send must fail
        fakeDocRepo.saveSignatureField(
            SignatureField("f-1", docId, "s-1", 0, 0.2f, 0.2f, 0.3f, 0.08f)
        )
        val failSend = sendUseCase(docId)
        assertFalse(failSend.isSuccess)
        assertEquals(DocumentStatus.DRAFT, fakeDocRepo.getDocumentById(docId).getOrNull()?.status)

        // Add field for s-2 -> Send must succeed, update status to PENDING, and save sentAt & inviteMessage
        fakeDocRepo.saveSignatureField(
            SignatureField("f-2", docId, "s-2", 0, 0.2f, 0.5f, 0.3f, 0.08f)
        )
        val okSend = sendUseCase(
            documentId = docId,
            inviteMessage = "Por favor assinem até sexta-feira.",
            sentAtMillis = 2500L
        )
        assertTrue(okSend.isSuccess)
        val dispatchedDoc = fakeDocRepo.getDocumentById(docId).getOrNull()
        assertEquals(DocumentStatus.PENDING, dispatchedDoc?.status)
        assertEquals(2500L, dispatchedDoc?.sentAt)
        assertEquals("Por favor assinem até sexta-feira.", dispatchedDoc?.inviteMessage)
    }

    @Test
    fun `ObserveAdminDocumentOverviewsUseCase and AdminDashboardUiState categorize documents into Sent, Partial, Completed and Draft`() = runTest(testDispatcher) {
        val adminId = "admin-1"
        // Doc 1: DRAFT
        fakeDocRepo.createDocument(Document("d-draft", "Rascunho", "1.pdf", DocumentStatus.DRAFT, 1000L, adminId))
        // Doc 2: PENDING with 0/2 signed -> Enviados
        fakeDocRepo.createDocument(Document("d-sent", "Proposta", "2.pdf", DocumentStatus.PENDING, 2000L, adminId))
        fakeDocRepo.assignSignerToDocument(DocumentSigner("d-sent", "s-1", SignerRoleClass.CONTRATANTE, SignatureStatus.PENDING))
        fakeDocRepo.assignSignerToDocument(DocumentSigner("d-sent", "s-2", SignerRoleClass.CONTRATADO, SignatureStatus.PENDING))
        // Doc 3: PENDING with 1/2 signed -> Parcialmente Assinados
        fakeDocRepo.createDocument(Document("d-partial", "Aditivo", "3.pdf", DocumentStatus.PENDING, 3000L, adminId))
        fakeDocRepo.assignSignerToDocument(DocumentSigner("d-partial", "s-1", SignerRoleClass.CONTRATANTE, SignatureStatus.SIGNED, 3500L, "127.0.0.1"))
        fakeDocRepo.assignSignerToDocument(DocumentSigner("d-partial", "s-2", SignerRoleClass.CONTRATADO, SignatureStatus.PENDING))
        // Doc 4: COMPLETED with 2/2 signed -> Concluídos
        fakeDocRepo.createDocument(Document("d-done", "Termo", "4.pdf", DocumentStatus.COMPLETED, 4000L, adminId))
        fakeDocRepo.assignSignerToDocument(DocumentSigner("d-done", "s-1", SignerRoleClass.CONTRATANTE, SignatureStatus.SIGNED, 4100L, "127.0.0.1"))
        fakeDocRepo.assignSignerToDocument(DocumentSigner("d-done", "s-2", SignerRoleClass.CONTRATADO, SignatureStatus.SIGNED, 4200L, "127.0.0.2"))

        val overviews = observeOverviewsUseCase(adminId).first()
        val uiState = AdminDashboardUiState(
            selectedTab = AdminDocumentTab.SENT,
            allDocuments = overviews
        )

        assertEquals(1, uiState.draftDocuments.size)
        assertEquals("d-draft", uiState.draftDocuments.first().document.id)

        assertEquals(1, uiState.sentDocuments.size)
        assertEquals("d-sent", uiState.sentDocuments.first().document.id)
        assertEquals(0f, uiState.sentDocuments.first().progressFraction, 0.001f)

        assertEquals(1, uiState.partiallySignedDocuments.size)
        assertEquals("d-partial", uiState.partiallySignedDocuments.first().document.id)
        assertEquals(0.5f, uiState.partiallySignedDocuments.first().progressFraction, 0.001f)

        assertEquals(1, uiState.completedDocuments.size)
        assertEquals("d-done", uiState.completedDocuments.first().document.id)
        assertEquals(1.0f, uiState.completedDocuments.first().progressFraction, 0.001f)
    }

    @Test
    fun `DocumentDetailsUiState builds chronological audit history for signed and completed documents`() {
        val doc = Document(
            id = "d-1",
            title = "Contrato",
            fileName = "c.pdf",
            status = DocumentStatus.COMPLETED,
            createdAt = 1000L,
            ownerAdminId = "admin-1",
            sentAt = 2200L,
            inviteMessage = "Segue contrato para assinatura."
        )
        val signerUser = User("s-1", "Carlos Silva", "carlos@letssign.com", "11999", "123", UserRole.SIGNER)
        val crossRef = DocumentSigner(
            documentId = "d-1",
            signerId = "s-1",
            roleClass = SignerRoleClass.CONTRATANTE,
            status = SignatureStatus.SIGNED,
            signedAt = 5000L,
            signatureIp = "192.168.0.10"
        )

        val detailsState = DocumentDetailsUiState(
            document = doc,
            signers = listOf(DocumentSignerWithUser(crossRef, signerUser)),
            fields = emptyList()
        )

        val events = detailsState.auditEvents
        // Events: Criado (1000), Enviado (2200), Carlos assinou (5000), Documento concluído (5500) -> 4 events
        assertEquals(4, events.size)
        assertEquals("Documento concluído", events.first().title)
        assertTrue(events.any { it.timestampMillis == 2200L && it.description.contains("Segue contrato para assinatura.") })
        assertTrue(events.any { it.title.contains("Carlos Silva assinou") && it.description.contains("192.168.0.10") })
    }

    @Test
    fun `DocumentReviewUiState detects missing signer fields and DocumentDetailsUiState formats Beatriz reminder and timeline`() {
        val doc = Document("d-2", "Aditivo contratual", "aditivo.pdf", DocumentStatus.PENDING, 1000L, "admin-1")
        val marina = User("s-1", "Marina Albuquerque", "marina@exemplo.com.br", "", "123", UserRole.SIGNER)
        val beatriz = User("s-2", "Beatriz Nogueira", "beatriz@exemplo.com.br", "", "123", UserRole.SIGNER)

        val marinaRef = DocumentSigner("d-2", "s-1", SignerRoleClass.CONTRATANTE, SignatureStatus.SIGNED, 2000L, "10.0.0.1")
        val beatrizRef = DocumentSigner("d-2", "s-2", SignerRoleClass.TESTEMUNHA, SignatureStatus.PENDING)
        val signersList = listOf(
            DocumentSignerWithUser(marinaRef, marina),
            DocumentSignerWithUser(beatrizRef, beatriz)
        )

        // Review state with only Marina having a field -> Beatriz is flagged in signersWithoutFields
        val reviewPendingState = DocumentReviewUiState(
            document = doc.copy(status = DocumentStatus.DRAFT),
            signers = signersList,
            fields = listOf(SignatureField("f-1", "d-2", "s-1", 1, 0.1f, 0.6f, 0.3f, 0.08f))
        )
        assertTrue(reviewPendingState.hasPendingFieldError)
        assertFalse(reviewPendingState.canSend)
        assertEquals("Beatriz Nogueira", reviewPendingState.signersWithoutFields.first().signer.name)

        // Details state for partially signed document (Marina signed, Beatriz pending)
        val partialDetails = DocumentDetailsUiState(
            document = doc,
            signers = signersList
        )
        assertEquals("Reenviar lembrete para Beatriz", partialDetails.reminderButtonLabel)
        assertEquals(4, partialDetails.timelineSteps.size)
        assertEquals("Aguardando Beatriz Nogueira", partialDetails.timelineSteps[2].title)
    }

    private class FakeReviewDocumentRepository : DocumentRepository {
        private val docsFlow = MutableStateFlow<List<Document>>(emptyList())
        private val signersFlow = MutableStateFlow<List<DocumentSigner>>(emptyList())
        private val fieldsFlow = MutableStateFlow<List<SignatureField>>(emptyList())

        private val mockUsers = mapOf(
            "s-1" to User("s-1", "Carlos", "c@letssign.com", "11", "123", UserRole.SIGNER),
            "s-2" to User("s-2", "Mariana", "m@letssign.com", "11", "123", UserRole.SIGNER)
        )

        override fun observeDocumentsByAdmin(adminId: String): Flow<List<Document>> =
            docsFlow.map { list -> list.filter { it.ownerAdminId == adminId } }

        override fun observeDocumentsBySigner(signerId: String): Flow<List<Document>> =
            docsFlow

        override fun observeDocumentById(documentId: String): Flow<Document?> =
            docsFlow.map { list -> list.firstOrNull { it.id == documentId } }

        override suspend fun getDocumentById(documentId: String): AppResult<Document> =
            docsFlow.value.firstOrNull { it.id == documentId }?.let { AppResult.Success(it) }
                ?: AppResult.Error("Not found")

        override suspend fun createDocument(document: Document): AppResult<Document> {
            docsFlow.value = docsFlow.value.filterNot { it.id == document.id } + document
            return AppResult.Success(document)
        }

        override suspend fun updateDocumentStatus(documentId: String, status: DocumentStatus): AppResult<Unit> {
            docsFlow.value = docsFlow.value.map { if (it.id == documentId) it.copy(status = status) else it }
            return AppResult.Success(Unit)
        }

        override suspend fun dispatchDocumentForSignature(
            documentId: String,
            sentAt: Long,
            inviteMessage: String
        ): AppResult<Unit> {
            docsFlow.value = docsFlow.value.map {
                if (it.id == documentId) {
                    it.copy(
                        status = DocumentStatus.PENDING,
                        sentAt = sentAt,
                        inviteMessage = inviteMessage
                    )
                } else {
                    it
                }
            }
            return AppResult.Success(Unit)
        }

        override suspend fun deleteDocument(documentId: String): AppResult<Unit> {
            docsFlow.value = docsFlow.value.filterNot { it.id == documentId }
            return AppResult.Success(Unit)
        }

        override fun observeDocumentSigners(documentId: String): Flow<List<DocumentSignerWithUser>> =
            signersFlow.map { list ->
                list.filter { it.documentId == documentId }.map { ref ->
                    val user = mockUsers[ref.signerId]
                        ?: User(ref.signerId, "Signer", "s@letssign.com", "", "", UserRole.SIGNER)
                    DocumentSignerWithUser(ref, user)
                }
            }

        override suspend fun getDocumentSigners(documentId: String): AppResult<List<DocumentSigner>> =
            AppResult.Success(signersFlow.value.filter { it.documentId == documentId })

        override suspend fun assignSignerToDocument(documentSigner: DocumentSigner): AppResult<Unit> {
            signersFlow.value = signersFlow.value.filterNot {
                it.documentId == documentSigner.documentId && it.signerId == documentSigner.signerId
            } + documentSigner
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

        override fun observeSignatureFields(documentId: String): Flow<List<SignatureField>> =
            fieldsFlow.map { list -> list.filter { it.documentId == documentId } }

        override suspend fun getSignatureFields(documentId: String): AppResult<List<SignatureField>> =
            AppResult.Success(fieldsFlow.value.filter { it.documentId == documentId })

        override suspend fun getSignatureFieldsBySigner(documentId: String, signerId: String): AppResult<List<SignatureField>> =
            AppResult.Success(fieldsFlow.value.filter { it.documentId == documentId && it.signerId == signerId })

        override suspend fun saveSignatureField(field: SignatureField): AppResult<SignatureField> {
            fieldsFlow.value = fieldsFlow.value.filterNot { it.id == field.id } + field
            return AppResult.Success(field)
        }

        override suspend fun deleteSignatureField(fieldId: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun deleteSignatureFieldsBySigner(documentId: String, signerId: String): AppResult<Unit> =
            AppResult.Success(Unit)
    }
}
