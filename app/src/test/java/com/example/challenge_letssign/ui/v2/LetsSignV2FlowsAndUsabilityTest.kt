package com.example.challenge_letssign.ui.v2

import com.example.challenge_letssign.data.local.seed.DatabaseSeeder
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.ImportedPdfFile
import com.example.challenge_letssign.domain.model.PersonType
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureFieldType
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.repository.PdfStorageRepository
import com.example.challenge_letssign.domain.repository.UserRepository
import com.example.challenge_letssign.domain.usecase.document.ObserveAdminDocumentOverviewsUseCase
import com.example.challenge_letssign.domain.usecase.signer.DeleteSignerUseCase
import com.example.challenge_letssign.domain.usecase.signer.ObserveSignerDocumentsUseCase
import com.example.challenge_letssign.domain.usecase.signer.ObserveSignersUseCase
import com.example.challenge_letssign.domain.usecase.signer.SaveSignerUseCase
import com.example.challenge_letssign.ui.admin.AdminDashboardViewModel
import com.example.challenge_letssign.ui.admin.MySignaturesTab
import com.example.challenge_letssign.ui.admin.details.DocumentDetailsViewModel
import com.example.challenge_letssign.ui.admin.signers.SignerManagementViewModel
import com.example.challenge_letssign.ui.components.LetsSignBottomNavTab
import com.example.challenge_letssign.ui.profile.LetsSignProfileViewModel
import com.example.challenge_letssign.ui.signer.SignerDashboardViewModel
import com.example.challenge_letssign.ui.signer.SignerDocumentTab
import com.example.challenge_letssign.utils.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class LetsSignV2FlowsAndUsabilityTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeUserRepo: InMemoryV2UserRepository
    private lateinit var fakeDocRepo: InMemoryV2DocumentRepository
    private lateinit var fakePdfRepo: InMemoryV2PdfStorageRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeUserRepo = InMemoryV2UserRepository()
        fakeDocRepo = InMemoryV2DocumentRepository(fakeUserRepo)
        fakePdfRepo = InMemoryV2PdfStorageRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `BottomNavTabs - Admin has 4 tabs with Perfil and Signer has 2 tabs (Minhas Assinaturas and Perfil)`() {
        assertEquals(
            listOf(
                LetsSignBottomNavTab.DOCUMENTS,
                LetsSignBottomNavTab.MY_SIGNATURES,
                LetsSignBottomNavTab.CONTACTS,
                LetsSignBottomNavTab.PROFILE
            ),
            LetsSignBottomNavTab.figmaPrimaryTabs
        )
        assertEquals(
            listOf(
                LetsSignBottomNavTab.MY_SIGNATURES,
                LetsSignBottomNavTab.PROFILE
            ),
            LetsSignBottomNavTab.signerPrimaryTabs
        )
    }

    @Test
    fun `ProfileViewModel - loads Fisica and Juridica profiles, updates fields and persists in UserRepository`() = runTest(testDispatcher) {
        val profileVm = LetsSignProfileViewModel(fakeUserRepo)

        // Load Marina Albuquerque (Física)
        profileVm.loadUserProfile("s-marina")
        val loadedState = profileVm.uiState.value
        assertEquals(PersonType.FISICA, loadedState.personType)
        assertEquals("123.456.789-09", loadedState.cpfCnpj)
        assertEquals("Marina Albuquerque", loadedState.name)
        assertEquals("marina.albuquerque@exemplo.com.br", loadedState.email)
        assertEquals("14/03/1988", loadedState.birthDate)

        // Edit and save profile
        profileVm.onNameChanged("Marina Albuquerque Silva")
        profileVm.onCpfCnpjChanged("999.888.777-66")
        profileVm.onBirthDateChanged("15/04/1989")
        profileVm.onSaveProfile()

        val savedState = profileVm.uiState.value
        assertFalse(savedState.isSaving)
        assertEquals("Perfil atualizado com sucesso.", savedState.snackbarMessage)

        val persistedUser = fakeUserRepo.getUserById("s-marina").getOrNull()
        assertEquals("Marina Albuquerque Silva", persistedUser?.name)
        assertEquals("999.888.777-66", persistedUser?.cpfCnpj)
        assertEquals("15/04/1989", persistedUser?.birthDate)

        profileVm.onDismissSnackbar()
        assertNull(profileVm.uiState.value.snackbarMessage)
    }

    @Test
    fun `ProfileViewModel - falls back gracefully when userId is not found in repository`() = runTest(testDispatcher) {
        val profileVm = LetsSignProfileViewModel(fakeUserRepo)
        profileVm.loadUserProfile("non-existent-id", fallbackName = "Visitante Teste")

        assertEquals("Visitante Teste", profileVm.uiState.value.name)
    }

    @Test
    fun `Novo Grupo - allows creating a new contact from inside Selecionar contatos sheet and auto-selects it in the group`() = runTest(testDispatcher) {
        val vm = SignerManagementViewModel(
            observeSignersUseCase = ObserveSignersUseCase(fakeUserRepo),
            saveSignerUseCase = SaveSignerUseCase(fakeUserRepo),
            deleteSignerUseCase = DeleteSignerUseCase(fakeUserRepo)
        )

        // 1. Open Novo Grupo flow
        vm.onOpenNewGroupFlow()
        assertTrue(vm.uiState.value.isNewGroupVisible)
        assertFalse(vm.uiState.value.canCreateGroup)

        // 2. Open "Selecionar contatos" sheet and select Marina
        vm.onOpenGroupContactPickerSheet()
        assertTrue(vm.uiState.value.isGroupContactPickerSheetOpen)
        vm.onToggleGroupPickerContact("s-marina")

        // 3. Click "Cadastrar novo contato" inside the group contact picker
        vm.onOpenCreateFormFromGroupPicker()
        assertFalse(vm.uiState.value.isGroupContactPickerSheetOpen)
        assertTrue(vm.uiState.value.isFormVisible)
        assertTrue(vm.uiState.value.isCreatingContactFromGroupPicker)

        // 4. Fill new contact form and save
        vm.onCpfCnpjChanged("55544433322")
        vm.onNameChanged("Camila Duarte")
        vm.onEmailChanged("camila.duarte@exemplo.com.br")
        vm.onBirthDateChanged("09/11/1990")
        vm.onSaveSigner()

        // 5. Verify it returns directly to the group contact picker with Camila already checked!
        val afterContactSave = vm.uiState.value
        assertFalse(afterContactSave.isFormVisible)
        assertTrue(afterContactSave.isNewGroupVisible)
        assertTrue(afterContactSave.isGroupContactPickerSheetOpen)
        assertEquals(2, afterContactSave.groupPickerSelectedIds.size)

        // 6. Confirm picker selection -> moves to Novo Grupo Finalizar screen
        vm.onConfirmGroupPickerSelection()
        assertFalse(vm.uiState.value.isGroupContactPickerSheetOpen)
        assertEquals(2, vm.uiState.value.groupConfirmedMembers.size)

        // 7. Enter group name and create group
        vm.onGroupNameChanged("Turma 15 Calculo II AGO - 2027")
        assertTrue(vm.uiState.value.canCreateGroup)
        vm.onCreateGroup()

        val finalState = vm.uiState.value
        assertFalse(finalState.isNewGroupVisible)
        assertEquals("Grupo \"Turma 15 Calculo II AGO - 2027\" criado com sucesso.", finalState.snackbarMessage)

        val camila = finalState.allSigners.first { it.email == "camila.duarte@exemplo.com.br" }
        val marina = finalState.allSigners.first { it.id == "s-marina" }
        assertEquals("Turma 15 Calculo II AGO - 2027", camila.groupName)
        assertEquals("Turma 15 Calculo II AGO - 2027", marina.groupName)
    }

    @Test
    fun `SignerDashboard and AdminDashboard - merges Aguardando Terceiros into Pendentes showing both Aguardando voce and Assinado por voce`() = runTest(testDispatcher) {
        // Doc 1: Pending for Rafael (0/2 signed -> Aguardando você)
        fakeDocRepo.createDocument(
            Document("doc-pending-me", "Contrato de serviços", "c1.pdf", DocumentStatus.PENDING, 1000L, "admin-1")
        )
        fakeDocRepo.assignSignerToDocument(
            DocumentSigner("doc-pending-me", "s-rafael", SignerRoleClass.CONTRATADO, SignatureStatus.PENDING)
        )
        fakeDocRepo.assignSignerToDocument(
            DocumentSigner("doc-pending-me", "s-beatriz", SignerRoleClass.TESTEMUNHA, SignatureStatus.PENDING)
        )

        // Doc 2: Already signed by Rafael, but Beatriz is still pending (1/2 signed, DocumentStatus.PENDING -> Assinado por você)
        fakeDocRepo.createDocument(
            Document("doc-waiting-others", "Aditivo contratual", "c2.pdf", DocumentStatus.PENDING, 2000L, "admin-1")
        )
        fakeDocRepo.assignSignerToDocument(
            DocumentSigner("doc-waiting-others", "s-rafael", SignerRoleClass.CONTRATADO, SignatureStatus.SIGNED, 2500L, "10.0.0.1")
        )
        fakeDocRepo.assignSignerToDocument(
            DocumentSigner("doc-waiting-others", "s-beatriz", SignerRoleClass.TESTEMUNHA, SignatureStatus.PENDING)
        )

        // Doc 3: Fully completed (2/2 signed, DocumentStatus.COMPLETED)
        fakeDocRepo.createDocument(
            Document("doc-completed", "Termo de adesão", "c3.pdf", DocumentStatus.COMPLETED, 3000L, "admin-1")
        )
        fakeDocRepo.assignSignerToDocument(
            DocumentSigner("doc-completed", "s-rafael", SignerRoleClass.CONTRATADO, SignatureStatus.SIGNED, 3100L, "10.0.0.1")
        )
        fakeDocRepo.assignSignerToDocument(
            DocumentSigner("doc-completed", "s-beatriz", SignerRoleClass.TESTEMUNHA, SignatureStatus.SIGNED, 3200L, "10.0.0.2")
        )

        val signerVm = SignerDashboardViewModel(ObserveSignerDocumentsUseCase(fakeDocRepo))
        signerVm.observeDocuments("s-rafael")

        val signerState = signerVm.uiState.value
        // In Pendentes tab: both doc-pending-me (Aguardando você) AND doc-waiting-others (Assinado por você) appear!
        assertEquals(2, signerState.pendingDocuments.size)
        assertTrue(signerState.pendingDocuments.any { it.document.id == "doc-pending-me" && it.isPendingForMe })
        assertTrue(signerState.pendingDocuments.any { it.document.id == "doc-waiting-others" && it.isSignedByMe })

        // Switch to Assinados tab
        signerVm.onSelectTab(SignerDocumentTab.SIGNED)
        assertEquals(2, signerVm.uiState.value.visibleDocuments.size)

        // Verify AdminDashboardViewModel MySignatures Pendentes also includes both Sent and Partially Signed
        val adminVm = AdminDashboardViewModel(ObserveAdminDocumentOverviewsUseCase(fakeDocRepo))
        adminVm.observeDocumentsForAdmin("admin-1")
        assertEquals(2, adminVm.uiState.value.myPendingSignatures.size)
        adminVm.onSelectMySignaturesTab(MySignaturesTab.SIGNED)
        assertEquals(1, adminVm.uiState.value.mySignedDocuments.size)
    }

    @Test
    fun `DocumentDetailsViewModel - deletes incomplete and completed documents with confirmation and cleans up non-seed PDF file`() = runTest(testDispatcher) {
        // 1. Incomplete (PENDING) document with custom imported PDF
        fakeDocRepo.createDocument(
            Document("doc-incomplete", "Aditivo Parcial", "doc_custom_99.pdf", DocumentStatus.PENDING, 1000L, "admin-1")
        )
        val detailsVm = DocumentDetailsViewModel(fakeDocRepo, fakePdfRepo)
        detailsVm.loadDocument("doc-incomplete")

        // Open delete confirmation dialog and cancel first
        detailsVm.onRequestDeleteDocument()
        assertTrue(detailsVm.uiState.value.isConfirmDeleteDialogOpen)
        detailsVm.onCancelDeleteDocument()
        assertFalse(detailsVm.uiState.value.isConfirmDeleteDialogOpen)
        assertFalse(detailsVm.uiState.value.isDeleted)

        // Confirm deletion of incomplete document -> deletes from repository and deletes doc_custom_99.pdf
        detailsVm.onRequestDeleteDocument()
        detailsVm.onConfirmDeleteDocument()
        assertTrue(detailsVm.uiState.value.isDeleted)
        assertTrue(fakePdfRepo.deletedFiles.contains("doc_custom_99.pdf"))
        assertFalse(fakeDocRepo.getDocumentById("doc-incomplete").isSuccess)

        // 2. Completed document using shared SEED_PDF_FILE_NAME -> removes document from DB but preserves seed PDF
        fakeDocRepo.createDocument(
            Document("doc-done-seed", "Contrato Padrão", DatabaseSeeder.SEED_PDF_FILE_NAME, DocumentStatus.COMPLETED, 2000L, "admin-1")
        )
        val detailsVm2 = DocumentDetailsViewModel(fakeDocRepo, fakePdfRepo)
        detailsVm2.loadDocument("doc-done-seed")
        detailsVm2.onRequestDeleteDocument()
        detailsVm2.onConfirmDeleteDocument()

        assertTrue(detailsVm2.uiState.value.isDeleted)
        assertFalse(fakePdfRepo.deletedFiles.contains(DatabaseSeeder.SEED_PDF_FILE_NAME))
    }

    @Test
    fun `DatabaseSeeder - places Rubrica (INITIALS) at the bottom-right corner of the contract page`() {
        val rubricaField = DatabaseSeeder.initialSignatureFields.first {
            it.fieldType == SignatureFieldType.INITIALS
        }
        assertEquals(0.72f, rubricaField.relativeX, 0.001f)
        assertEquals(0.85f, rubricaField.relativeY, 0.001f)
        assertTrue(rubricaField.relativeX + rubricaField.relativeWidth <= 1.0f)
        assertTrue(rubricaField.relativeY + rubricaField.relativeHeight <= 1.0f)
    }

    // --- Fakes ---

    private class InMemoryV2UserRepository : UserRepository {
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
                ),
                User(
                    id = "s-beatriz",
                    name = "Beatriz Nogueira",
                    email = "beatriz.nogueira@exemplo.com.br",
                    role = UserRole.SIGNER,
                    personType = PersonType.FISICA,
                    cpfCnpj = "456.789.123-44",
                    birthDate = "22/07/1993",
                    groupName = "Jurídico"
                )
            )
        )

        override suspend fun login(email: String, password: String): AppResult<User> = AppResult.Error("")
        override suspend fun requestLoginCode(email: String): AppResult<User> = AppResult.Error("")
        override suspend fun verifyLoginCode(email: String, code: String): AppResult<User> = AppResult.Error("")

        override suspend fun getUserById(userId: String): AppResult<User> =
            usersFlow.value.firstOrNull { it.id == userId }?.let { AppResult.Success(it) }
                ?: AppResult.Error("Usuário não encontrado.")

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

    private class InMemoryV2DocumentRepository(
        private val userRepo: InMemoryV2UserRepository
    ) : DocumentRepository {
        private val docsFlow = MutableStateFlow<List<Document>>(emptyList())
        private val signersFlow = MutableStateFlow<List<DocumentSigner>>(emptyList())
        private val fieldsFlow = MutableStateFlow<List<SignatureField>>(emptyList())

        override fun observeDocumentsByAdmin(adminId: String): Flow<List<Document>> =
            docsFlow.map { list -> list.filter { it.ownerAdminId == adminId } }

        override fun observeDocumentsBySigner(signerId: String): Flow<List<Document>> =
            docsFlow.map { list ->
                val docIdsForSigner = signersFlow.value.filter { it.signerId == signerId }.map { it.documentId }.toSet()
                list.filter { it.id in docIdsForSigner && it.status != DocumentStatus.DRAFT }
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

        override suspend fun deleteSignatureField(fieldId: String): AppResult<Unit> {
            fieldsFlow.value = fieldsFlow.value.filterNot { it.id == fieldId }
            return AppResult.Success(Unit)
        }

        override suspend fun deleteSignatureFieldsBySigner(documentId: String, signerId: String): AppResult<Unit> {
            fieldsFlow.value = fieldsFlow.value.filterNot { it.documentId == documentId && it.signerId == signerId }
            return AppResult.Success(Unit)
        }
    }

    private class InMemoryV2PdfStorageRepository : PdfStorageRepository {
        val deletedFiles = mutableListOf<String>()

        override suspend fun importPdfFromUri(uriString: String, fallbackName: String?): AppResult<ImportedPdfFile> =
            AppResult.Error("Not used")

        override suspend fun getPdfFile(internalFileName: String): AppResult<File> =
            AppResult.Error("Not used")

        override suspend fun deletePdfFile(internalFileName: String): AppResult<Unit> {
            deletedFiles.add(internalFileName)
            return AppResult.Success(Unit)
        }

        override suspend fun embedSignaturesIntoPdf(
            internalFileName: String,
            fields: List<SignatureField>,
            signaturePngBytes: ByteArray
        ): AppResult<Unit> = AppResult.Success(Unit)
    }
}
