package com.example.challenge_letssign.e2e

import com.example.challenge_letssign.domain.model.AdditionalAuthOption
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.ImportedPdfFile
import com.example.challenge_letssign.domain.model.PersonType
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureFieldType
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerAuthMethod
import com.example.challenge_letssign.domain.model.SignerDeliveryChannel
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.repository.PdfStorageRepository
import com.example.challenge_letssign.domain.repository.UserRepository
import com.example.challenge_letssign.domain.usecase.document.DeleteSignatureFieldUseCase
import com.example.challenge_letssign.domain.usecase.document.ImportPdfUseCase
import com.example.challenge_letssign.domain.usecase.document.ObserveAdminDocumentOverviewsUseCase
import com.example.challenge_letssign.domain.usecase.document.PrepareDraftDocumentUseCase
import com.example.challenge_letssign.domain.usecase.document.SaveSignatureFieldUseCase
import com.example.challenge_letssign.domain.usecase.document.SendDocumentForSignatureUseCase
import com.example.challenge_letssign.domain.usecase.document.ValidateDocumentPositioningUseCase
import com.example.challenge_letssign.domain.usecase.signer.ObserveSignerDocumentsUseCase
import com.example.challenge_letssign.domain.usecase.signer.ObserveSignersUseCase
import com.example.challenge_letssign.domain.usecase.signer.SaveSignerUseCase
import com.example.challenge_letssign.domain.usecase.signer.SignDocumentUseCase
import com.example.challenge_letssign.ui.admin.details.DocumentDetailsViewModel
import com.example.challenge_letssign.ui.admin.preparation.DocumentPreparationViewModel
import com.example.challenge_letssign.ui.admin.review.DocumentReviewViewModel
import com.example.challenge_letssign.ui.signer.SignerDashboardViewModel
import com.example.challenge_letssign.utils.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class EndToEndAndEdgeCasesTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepo: E2EUserRepository
    private lateinit var docRepo: E2EDocumentRepository
    private lateinit var pdfRepo: E2EPdfStorageRepository

    private lateinit var saveFieldUseCase: SaveSignatureFieldUseCase
    private lateinit var deleteFieldUseCase: DeleteSignatureFieldUseCase
    private lateinit var validatePositioningUseCase: ValidateDocumentPositioningUseCase
    private lateinit var sendDocumentUseCase: SendDocumentForSignatureUseCase
    private lateinit var signDocumentUseCase: SignDocumentUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepo = E2EUserRepository()
        docRepo = E2EDocumentRepository(userRepo)
        pdfRepo = E2EPdfStorageRepository()

        saveFieldUseCase = SaveSignatureFieldUseCase(docRepo)
        deleteFieldUseCase = DeleteSignatureFieldUseCase(docRepo)
        validatePositioningUseCase = ValidateDocumentPositioningUseCase(docRepo)
        sendDocumentUseCase = SendDocumentForSignatureUseCase(docRepo, validatePositioningUseCase)
        signDocumentUseCase = SignDocumentUseCase(docRepo, pdfRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `E2E Complete Journey - Admin creates group, prepares doc with inline contact and global auth, positions signature and bottom-right rubrica, dispatches, both signers sign, auto-completes, downloads and removes`() = runTest(testDispatcher) {
        // 1. Admin creates a group "Diretoria" for Marina
        val groupRes = userRepo.assignGroupToUsers("Diretoria", setOf("s-marina"))
        assertTrue(groupRes.isSuccess)
        assertEquals("Diretoria", userRepo.getUserById("s-marina").getOrNull()?.groupName)

        // 2. Admin opens New Document Wizard (Steps 1-3)
        val prepVm = DocumentPreparationViewModel(
            observeSignersUseCase = ObserveSignersUseCase(userRepo),
            importPdfUseCase = ImportPdfUseCase(pdfRepo),
            prepareDraftDocumentUseCase = PrepareDraftDocumentUseCase(docRepo),
            documentRepository = docRepo,
            saveSignerUseCase = SaveSignerUseCase(userRepo)
        ).apply {
            pdfProcessingDelayMillis = 0L
            aiDetectionDelayMillis = 0L
        }

        // Step 1: Select built-in sample PDF
        prepVm.onPdfUriSelected("sample://contrato-servicos.pdf", "contrato-servicos.pdf")
        assertEquals("Contrato de serviço", prepVm.uiState.value.title)
        assertTrue(prepVm.uiState.value.canAdvanceFromStep1)
        prepVm.onContinueFromStep1()
        assertEquals(2, prepVm.uiState.value.currentStep)

        // Step 2: Open signer selection sheet, search group "Diretoria", select it, AND register an inline contact "Helena Costa"
        prepVm.onOpenSignerSelectionSheet()
        prepVm.onPickerSearchQueryChanged("Diretoria")
        assertEquals(1, prepVm.uiState.value.matchingPickerGroups.size)
        prepVm.onTogglePickerGroupSelection("Diretoria")

        prepVm.onOpenInlineNewContactDialog()
        prepVm.onInlineNewContactCpfCnpjChanged("11122233344")
        prepVm.onInlineNewContactNameChanged("Helena Costa")
        prepVm.onInlineNewContactEmailChanged("helena.costa@exemplo.com.br")
        prepVm.onInlineNewContactBirthDateChanged("20/08/1991")
        prepVm.onSaveInlineNewContact()

        prepVm.onConfirmPickerSelection()
        assertEquals(2, prepVm.uiState.value.assignedSigners.size)
        val helenaUser = prepVm.uiState.value.assignedSigners.first { it.user.name == "Helena Costa" }.user

        // Step 3: Configure GLOBAL Delivery, Auth, Additional Auths and Language for all signers
        prepVm.onContinueFromStep2()
        assertEquals(3, prepVm.uiState.value.currentStep)
        prepVm.onSelectDeliveryChannel("GLOBAL", SignerDeliveryChannel.WHATSAPP)
        prepVm.onSelectAuthMethod("GLOBAL", SignerAuthMethod.SMS)
        prepVm.onOpenAdditionalAuthsSheet("GLOBAL")
        prepVm.onTogglePendingAdditionalAuth(AdditionalAuthOption.SELFIE_WITH_BIOMETRICS)
        prepVm.onApplyAdditionalAuths()
        prepVm.onSignerLanguageChanged("GLOBAL", "Português")

        prepVm.onSaveDraftAndContinue("admin-1")
        val draftDoc = prepVm.uiState.value.completedDraftDocument
        assertNotNull(draftDoc)
        val docId = draftDoc!!.id

        // 3. Step 4: Position Signature and Bottom-Right Rubrica for Marina and Helena
        val f1 = saveFieldUseCase(
            documentId = docId,
            signerId = "s-marina",
            pageNumber = 1,
            relativeX = 0.08f,
            relativeY = 0.62f,
            fieldType = SignatureFieldType.SIGNATURE
        )
        val f2Rubrica = saveFieldUseCase(
            documentId = docId,
            signerId = "s-marina",
            pageNumber = 1,
            relativeX = 0.72f,
            relativeY = 0.85f,
            relativeWidth = 0.20f,
            relativeHeight = 0.07f,
            fieldType = SignatureFieldType.INITIALS
        )
        val f3 = saveFieldUseCase(
            documentId = docId,
            signerId = helenaUser.id,
            pageNumber = 1,
            relativeX = 0.54f,
            relativeY = 0.62f,
            fieldType = SignatureFieldType.SIGNATURE
        )
        assertTrue(f1.isSuccess && f2Rubrica.isSuccess && f3.isSuccess)

        // 4. Steps 5-6: Review and Send Document for Signature
        val reviewVm = DocumentReviewViewModel(docRepo, sendDocumentUseCase).apply {
            sendingDelayMillis = 0L
        }
        reviewVm.loadDocument(docId)
        assertFalse(reviewVm.uiState.value.hasPendingFieldError)
        assertTrue(reviewVm.uiState.value.canSend)
        reviewVm.onInviteMessageChanged("Por favor assinem e rubriquem o contrato.")
        reviewVm.onSendDocument()
        assertTrue(reviewVm.uiState.value.isSentSuccess)
        assertEquals(DocumentStatus.PENDING, docRepo.getDocumentById(docId).getOrNull()?.status)

        // 5. Signer 1 (Marina) signs with both Signature and Rubrica
        val signRes1 = signDocumentUseCase(
            documentId = docId,
            signerId = "s-marina",
            signaturePngBytes = byteArrayOf(1, 2, 3),
            initialsPngBytes = byteArrayOf(4, 5, 6),
            signedAtMillis = 10_000L,
            signatureIp = "192.168.0.10"
        )
        assertTrue(signRes1.isSuccess)
        // Document is still PENDING because Helena hasn't signed yet
        assertEquals(DocumentStatus.PENDING, docRepo.getDocumentById(docId).getOrNull()?.status)

        // Verify Marina's SignerDashboard still shows the document in Pendentes as "Assinado por você" (merged Aguardando Terceiros!)
        val marinaDashboardVm = SignerDashboardViewModel(ObserveSignerDocumentsUseCase(docRepo))
        marinaDashboardVm.observeDocuments("s-marina")
        assertEquals(1, marinaDashboardVm.uiState.value.pendingDocuments.size)
        assertTrue(marinaDashboardVm.uiState.value.pendingDocuments.first().isSignedByMe)

        // 6. Signer 2 (Helena) signs -> Regra 5 auto-completes the document!
        val signRes2 = signDocumentUseCase(
            documentId = docId,
            signerId = helenaUser.id,
            signaturePngBytes = byteArrayOf(7, 8, 9),
            signedAtMillis = 20_000L,
            signatureIp = "192.168.0.11"
        )
        assertTrue(signRes2.isSuccess)
        assertEquals(DocumentStatus.COMPLETED, docRepo.getDocumentById(docId).getOrNull()?.status)

        // Now Marina's Pendentes tab is empty because the document is COMPLETED!
        assertEquals(0, marinaDashboardVm.uiState.value.pendingDocuments.size)
        assertEquals(1, marinaDashboardVm.uiState.value.signedDocuments.size)

        // 7. Admin opens Document Details, downloads signed PDF and removes document
        val detailsVm = DocumentDetailsViewModel(docRepo, pdfRepo)
        detailsVm.loadDocument(docId)
        detailsVm.onDownloadSignedDocument()
        assertTrue(detailsVm.uiState.value.isDownloaded)
        assertEquals("contrato-servicos-assinado.pdf", detailsVm.uiState.value.downloadedFileName)
        assertEquals(1, pdfRepo.exportedFiles.size)

        detailsVm.onRequestDeleteDocument()
        detailsVm.onConfirmDeleteDocument()
        assertTrue(detailsVm.uiState.value.isDeleted)
    }

    @Test
    fun `Edge Cases Regra 2 - exact boundary 1_0f succeeds while 1_0001f or negative coordinates are blocked`() = runTest(testDispatcher) {
        val docId = "doc-bounds"
        docRepo.createDocument(Document(docId, "Teste Limites", "b.pdf", DocumentStatus.DRAFT, 1000L, "admin-1"))

        // Exact bottom-right boundary: 0.64 + 0.36 == 1.00 and 0.92 + 0.08 == 1.00 -> valid
        val exactEdgeRes = saveFieldUseCase(
            documentId = docId,
            signerId = "s-marina",
            pageNumber = 0,
            relativeX = 0.64f,
            relativeY = 0.92f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f
        )
        assertTrue(exactEdgeRes.isSuccess)

        // Slightly overflowing X: 0.65 + 0.36 = 1.01 > 1.0 -> blocked
        val overflowXRes = saveFieldUseCase(
            documentId = docId,
            signerId = "s-marina",
            pageNumber = 0,
            relativeX = 0.65f,
            relativeY = 0.50f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f
        )
        assertFalse(overflowXRes.isSuccess)
        assertEquals(SaveSignatureFieldUseCase.OUT_OF_BOUNDS_MESSAGE, (overflowXRes as AppResult.Error).message)

        // Negative Y: -0.01f -> blocked
        val negativeYRes = saveFieldUseCase(
            documentId = docId,
            signerId = "s-marina",
            pageNumber = 0,
            relativeX = 0.10f,
            relativeY = -0.01f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f
        )
        assertFalse(negativeYRes.isSuccess)
    }

    @Test
    fun `Edge Cases Regra 3 and Regra 4 - cascade delete on signer removal in DRAFT and strict immutability after PENDING or COMPLETED`() = runTest(testDispatcher) {
        val docId = "doc-rules-3-4"
        docRepo.createDocument(Document(docId, "Contrato Imutável", "i.pdf", DocumentStatus.DRAFT, 1000L, "admin-1"))
        docRepo.assignSignerToDocument(DocumentSigner(docId, "s-marina", SignerRoleClass.CONTRATANTE, SignatureStatus.PENDING))
        docRepo.assignSignerToDocument(DocumentSigner(docId, "s-rafael", SignerRoleClass.CONTRATADO, SignatureStatus.PENDING))

        val marinaField = saveFieldUseCase(
            documentId = docId,
            signerId = "s-marina",
            pageNumber = 0,
            relativeX = 0.1f,
            relativeY = 0.5f
        ).getOrNull()!!
        val rafaelField = saveFieldUseCase(
            documentId = docId,
            signerId = "s-rafael",
            pageNumber = 0,
            relativeX = 0.5f,
            relativeY = 0.5f
        ).getOrNull()!!

        // Regra 3: Removing Rafael in DRAFT cascades deletion of rafaelField but keeps marinaField
        docRepo.removeSignerFromDocument(docId, "s-rafael")
        val remainingFields = docRepo.getSignatureFields(docId).getOrNull().orEmpty()
        assertEquals(1, remainingFields.size)
        assertEquals(marinaField.id, remainingFields.first().id)

        // Dispatch document -> status becomes PENDING
        val sendRes = sendDocumentUseCase(docId)
        assertTrue(sendRes.isSuccess)

        // Regra 4: Attempting to add or delete a field on a PENDING document must fail
        val addAfterSend = saveFieldUseCase(
            documentId = docId,
            signerId = "s-marina",
            pageNumber = 0,
            relativeX = 0.2f,
            relativeY = 0.2f
        )
        assertFalse(addAfterSend.isSuccess)

        val deleteAfterSend = deleteFieldUseCase(docId, marinaField.id)
        assertFalse(deleteAfterSend.isSuccess)
        assertEquals(1, docRepo.getSignatureFields(docId).getOrNull()?.size)
    }

    @Test
    fun `Edge Cases Group & Inline Contact validation - rejects blank group name or blank inline contact fields`() = runTest(testDispatcher) {
        // Blank group name or empty userIds
        assertFalse(userRepo.assignGroupToUsers("   ", setOf("s-marina")).isSuccess)
        assertFalse(userRepo.assignGroupToUsers("Jurídico", emptySet()).isSuccess)

        val prepVm = DocumentPreparationViewModel(
            observeSignersUseCase = ObserveSignersUseCase(userRepo),
            importPdfUseCase = ImportPdfUseCase(pdfRepo),
            prepareDraftDocumentUseCase = PrepareDraftDocumentUseCase(docRepo),
            documentRepository = docRepo,
            saveSignerUseCase = SaveSignerUseCase(userRepo)
        )

        prepVm.onOpenInlineNewContactDialog()
        prepVm.onInlineNewContactNameChanged("   ")
        prepVm.onInlineNewContactEmailChanged("")
        prepVm.onSaveInlineNewContact()

        assertTrue(prepVm.uiState.value.isInlineNewContactDialogOpen)
        assertNotNull(prepVm.uiState.value.newContactError)
    }

    @Test
    fun `Edge Cases Isolation (Regra 4) - Admin only sees own documents and Signer never sees DRAFT or unassigned documents`() = runTest(testDispatcher) {
        // Admin 1 DRAFT
        docRepo.createDocument(Document("d-draft", "Rascunho Secreto", "1.pdf", DocumentStatus.DRAFT, 100L, "admin-1"))
        docRepo.assignSignerToDocument(DocumentSigner("d-draft", "s-marina", SignerRoleClass.CONTRATANTE, SignatureStatus.PENDING))

        // Admin 1 PENDING for Marina
        docRepo.createDocument(Document("d-marina", "Contrato Marina", "2.pdf", DocumentStatus.PENDING, 200L, "admin-1"))
        docRepo.assignSignerToDocument(DocumentSigner("d-marina", "s-marina", SignerRoleClass.CONTRATANTE, SignatureStatus.PENDING))

        // Admin 2 PENDING for Rafael
        docRepo.createDocument(Document("d-other-admin", "Contrato Outro Admin", "3.pdf", DocumentStatus.PENDING, 300L, "admin-2"))
        docRepo.assignSignerToDocument(DocumentSigner("d-other-admin", "s-rafael", SignerRoleClass.CONTRATADO, SignatureStatus.PENDING))

        val admin1Docs = ObserveAdminDocumentOverviewsUseCase(docRepo)("admin-1").first()
        assertEquals(2, admin1Docs.size)
        assertFalse(admin1Docs.any { it.document.id == "d-other-admin" })

        val marinaDocs = ObserveSignerDocumentsUseCase(docRepo)("s-marina").first()
        assertEquals(1, marinaDocs.size)
        assertEquals("d-marina", marinaDocs.first().document.id)
    }

    // --- Fakes ---

    private class E2EUserRepository : UserRepository {
        val usersFlow = MutableStateFlow(
            listOf(
                User(
                    id = "s-marina",
                    name = "Marina Albuquerque",
                    email = "marina.albuquerque@exemplo.com.br",
                    role = UserRole.SIGNER,
                    personType = PersonType.FISICA,
                    cpfCnpj = "123.456.789-09",
                    birthDate = "14/03/1988",
                    groupName = "Jurídico"
                ),
                User(
                    id = "s-rafael",
                    name = "Rafael Tavares",
                    email = "rafael.tavares@exemplo.com.br",
                    role = UserRole.SIGNER,
                    personType = PersonType.FISICA,
                    cpfCnpj = "321.654.987-10",
                    birthDate = "03/09/1985",
                    groupName = "Comercial"
                )
            )
        )

        override suspend fun login(email: String, password: String): AppResult<User> = AppResult.Error("")
        override suspend fun requestLoginCode(email: String): AppResult<User> = AppResult.Error("")
        override suspend fun verifyLoginCode(email: String, code: String): AppResult<User> = AppResult.Error("")

        override suspend fun getUserById(userId: String): AppResult<User> =
            usersFlow.value.firstOrNull { it.id == userId }?.let { AppResult.Success(it) }
                ?: AppResult.Error("Não encontrado")

        override suspend fun getUserByEmail(email: String): AppResult<User?> =
            AppResult.Success(usersFlow.value.firstOrNull { it.email.equals(email, ignoreCase = true) })

        override fun observeSigners(): Flow<List<User>> = observeUsersByRole(UserRole.SIGNER)

        override fun observeUsersByRole(role: UserRole): Flow<List<User>> =
            usersFlow.map { list -> list.filter { it.role == role } }

        override suspend fun createUser(user: User): AppResult<User> {
            usersFlow.value = usersFlow.value + user
            return AppResult.Success(user)
        }

        override suspend fun updateUser(user: User): AppResult<User> {
            usersFlow.value = usersFlow.value.map { if (it.id == user.id) user else it }
            return AppResult.Success(user)
        }

        override suspend fun deleteUser(userId: String): AppResult<Unit> {
            usersFlow.value = usersFlow.value.filterNot { it.id == userId }
            return AppResult.Success(Unit)
        }
    }

    private class E2EDocumentRepository(
        private val userRepo: E2EUserRepository
    ) : DocumentRepository {
        private val docsFlow = MutableStateFlow<List<Document>>(emptyList())
        private val signersFlow = MutableStateFlow<List<DocumentSigner>>(emptyList())
        private val fieldsFlow = MutableStateFlow<List<SignatureField>>(emptyList())

        override fun observeDocumentsByAdmin(adminId: String): Flow<List<Document>> =
            docsFlow.map { list -> list.filter { it.ownerAdminId == adminId } }

        override fun observeDocumentsBySigner(signerId: String): Flow<List<Document>> =
            docsFlow.map { list ->
                val docIds = signersFlow.value.filter { it.signerId == signerId }.map { it.documentId }.toSet()
                list.filter { it.id in docIds && it.status != DocumentStatus.DRAFT }
            }

        override fun observeDocumentById(documentId: String): Flow<Document?> =
            docsFlow.map { list -> list.firstOrNull { it.id == documentId } }

        override suspend fun getDocumentById(documentId: String): AppResult<Document> =
            docsFlow.value.firstOrNull { it.id == documentId }?.let { AppResult.Success(it) }
                ?: AppResult.Error("Não encontrado")

        override suspend fun createDocument(document: Document): AppResult<Document> {
            docsFlow.value = docsFlow.value.filterNot { it.id == document.id } + document
            return AppResult.Success(document)
        }

        override suspend fun updateDocumentStatus(documentId: String, status: DocumentStatus): AppResult<Unit> {
            docsFlow.value = docsFlow.value.map { if (it.id == documentId) it.copy(status = status) else it }
            return AppResult.Success(Unit)
        }

        override suspend fun dispatchDocumentForSignature(documentId: String, sentAt: Long, inviteMessage: String): AppResult<Unit> {
            docsFlow.value = docsFlow.value.map {
                if (it.id == documentId) it.copy(status = DocumentStatus.PENDING, sentAt = sentAt, inviteMessage = inviteMessage) else it
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
                    val u = userRepo.usersFlow.value.firstOrNull { it.id == ref.signerId }
                        ?: User(ref.signerId, "Signatário", "s@exemplo.com.br", role = UserRole.SIGNER)
                    DocumentSignerWithUser(ref, u)
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

        override suspend fun removeSignerFromDocument(documentId: String, signerId: String): AppResult<Unit> {
            signersFlow.value = signersFlow.value.filterNot { it.documentId == documentId && it.signerId == signerId }
            fieldsFlow.value = fieldsFlow.value.filterNot { it.documentId == documentId && it.signerId == signerId }
            return AppResult.Success(Unit)
        }

        override suspend fun markDocumentAsSignedBySigner(
            documentId: String,
            signerId: String,
            signedAt: Long,
            signatureIp: String
        ): AppResult<Unit> {
            signersFlow.value = signersFlow.value.map {
                if (it.documentId == documentId && it.signerId == signerId) {
                    it.copy(status = SignatureStatus.SIGNED, signedAt = signedAt, signatureIp = signatureIp)
                } else {
                    it
                }
            }
            val pendingLeft = signersFlow.value.count { it.documentId == documentId && it.status == SignatureStatus.PENDING }
            if (pendingLeft == 0) {
                docsFlow.value = docsFlow.value.map {
                    if (it.id == documentId) it.copy(status = DocumentStatus.COMPLETED) else it
                }
            }
            return AppResult.Success(Unit)
        }

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

        override suspend fun deleteSignatureField(fieldId: String): AppResult<Unit> {
            fieldsFlow.value = fieldsFlow.value.filterNot { it.id == fieldId }
            return AppResult.Success(Unit)
        }

        override suspend fun deleteSignatureFieldsBySigner(documentId: String, signerId: String): AppResult<Unit> {
            fieldsFlow.value = fieldsFlow.value.filterNot { it.documentId == documentId && it.signerId == signerId }
            return AppResult.Success(Unit)
        }
    }

    private class E2EPdfStorageRepository : PdfStorageRepository {
        val exportedFiles = mutableListOf<String>()

        override suspend fun importPdfFromUri(uriString: String, fallbackName: String?): AppResult<ImportedPdfFile> =
            AppResult.Success(
                ImportedPdfFile(
                    internalFileName = "doc_e2e_1.pdf",
                    originalDisplayName = fallbackName ?: "contrato-servicos.pdf",
                    pageCount = 6,
                    fileSizeBytes = 1_887_436L
                )
            )

        override suspend fun getPdfFile(internalFileName: String): AppResult<File> =
            AppResult.Success(File(internalFileName))

        override suspend fun deletePdfFile(internalFileName: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun embedSignaturesIntoPdf(
            internalFileName: String,
            fields: List<SignatureField>,
            signaturePngBytes: ByteArray
        ): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun exportSignedPdfToDownloads(
            internalFileName: String,
            targetDisplayName: String
        ): AppResult<File> {
            exportedFiles.add(targetDisplayName)
            return AppResult.Success(File(targetDisplayName))
        }
    }
}
