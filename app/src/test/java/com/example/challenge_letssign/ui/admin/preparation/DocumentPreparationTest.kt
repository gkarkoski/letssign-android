package com.example.challenge_letssign.ui.admin.preparation

import com.example.challenge_letssign.domain.model.AdditionalAuthOption
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.ImportedPdfFile
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignerAuthMethod
import com.example.challenge_letssign.domain.model.SignerDeliveryChannel
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.repository.PdfStorageRepository
import com.example.challenge_letssign.domain.repository.UserRepository
import com.example.challenge_letssign.domain.usecase.document.ImportPdfUseCase
import com.example.challenge_letssign.domain.usecase.document.PrepareDraftDocumentUseCase
import com.example.challenge_letssign.domain.usecase.signer.ObserveSignersUseCase
import com.example.challenge_letssign.utils.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentPreparationTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeUserRepo: FakePrepUserRepository
    private lateinit var fakePdfRepo: FakePdfStorageRepository
    private lateinit var fakeDocRepo: FakePrepDocumentRepository
    private lateinit var viewModel: DocumentPreparationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeUserRepo = FakePrepUserRepository()
        fakePdfRepo = FakePdfStorageRepository()
        fakeDocRepo = FakePrepDocumentRepository()

        viewModel = DocumentPreparationViewModel(
            observeSignersUseCase = ObserveSignersUseCase(fakeUserRepo),
            importPdfUseCase = ImportPdfUseCase(fakePdfRepo),
            prepareDraftDocumentUseCase = PrepareDraftDocumentUseCase(fakeDocRepo),
            documentRepository = fakeDocRepo
        ).apply {
            pdfProcessingDelayMillis = 0L
            aiDetectionDelayMillis = 0L
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Step 1 - canAdvanceFromStep1 is true only when title is filled and PDF is READY`() = runTest(testDispatcher) {
        assertFalse(viewModel.uiState.value.canAdvanceFromStep1)

        viewModel.onTitleChanged("Contrato de serviço")
        assertFalse(viewModel.uiState.value.canAdvanceFromStep1)

        viewModel.onPdfUriSelected("content://valid/contrato-servicos.pdf", "contrato-servicos.pdf")
        val state = viewModel.uiState.value
        assertEquals(PdfImportStatus.READY, state.pdfStatus)
        assertEquals("doc_internal_123.pdf", state.importedPdf?.internalFileName)
        assertEquals(6, state.importedPdf?.pageCount)
        assertEquals("1,8 MB", state.importedPdf?.formattedFileSize)
        assertTrue(state.canAdvanceFromStep1)

        viewModel.onContinueFromStep1()
        assertEquals(2, viewModel.uiState.value.currentStep)
    }

    @Test
    fun `Step 1 - invalid or corrupted PDF sets ERROR status and message`() = runTest(testDispatcher) {
        fakePdfRepo.shouldFail = true
        viewModel.onTitleChanged("Relatório Mensal")
        viewModel.onPdfUriSelected("content://corrupted/relatorio-final.pdf", "relatorio-final.pdf")

        val state = viewModel.uiState.value
        assertEquals(PdfImportStatus.ERROR, state.pdfStatus)
        assertNull(state.importedPdf)
        assertFalse(state.canAdvanceFromStep1)
        assertEquals(
            "Não foi possível processar relatorio-final.pdf. Escolha outro arquivo PDF.",
            state.pdfErrorMessage
        )
    }

    @Test
    fun `Step 2 - Sugerir com IA detects 3 signers and allows confirming selection`() = runTest(testDispatcher) {
        viewModel.onTitleChanged("Contrato de serviço")
        viewModel.onPdfUriSelected("content://valid/contrato.pdf", "contrato.pdf")
        viewModel.onContinueFromStep1()

        viewModel.onSuggestSignersWithAiClick()
        val aiState = viewModel.uiState.value
        assertTrue(aiState.isReviewingAiSigners)
        assertEquals(3, aiState.aiSuggestedSigners.size)
        assertEquals(3, aiState.selectedAiSignersCount)

        // Uncheck one suggested signer and re-check
        viewModel.onToggleAiSuggestedSigner("s-3")
        assertEquals(2, viewModel.uiState.value.selectedAiSignersCount)
        viewModel.onToggleAiSuggestedSigner("s-3")
        assertEquals(3, viewModel.uiState.value.selectedAiSignersCount)

        viewModel.onConfirmAiSuggestedSigners()
        val afterConfirm = viewModel.uiState.value
        assertFalse(afterConfirm.isReviewingAiSigners)
        assertEquals(3, afterConfirm.assignedSigners.size)
        assertEquals(SignerRoleClass.CONTRATANTE, afterConfirm.assignedSigners[0].roleClass)
        assertEquals(SignerRoleClass.CONTRATADO, afterConfirm.assignedSigners[1].roleClass)
        assertEquals(SignerRoleClass.TESTEMUNHA, afterConfirm.assignedSigners[2].roleClass)
    }

    @Test
    fun `Step 2 - group search Juridico shows group header and toggles all group members`() = runTest(testDispatcher) {
        viewModel.onOpenSignerSelectionSheet()
        viewModel.onPickerSearchQueryChanged("Jurídico")

        val groups = viewModel.uiState.value.matchingPickerGroups
        assertEquals(1, groups.size)
        assertEquals("Jurídico", groups.first().groupName)
        assertEquals(2, groups.first().members.size)

        viewModel.onTogglePickerGroupSelection("Jurídico")
        assertEquals(2, viewModel.uiState.value.pickerSelectedIds.size)

        viewModel.onConfirmPickerSelection()
        assertEquals(2, viewModel.uiState.value.assignedSigners.size)
    }

    @Test
    fun `Step 2 and Step 3 - configuring delivery link, auth method, additional auths and saving draft persists in Room`() = runTest(testDispatcher) {
        viewModel.onTitleChanged("Contrato de serviço")
        viewModel.onPdfUriSelected("content://valid/contrato.pdf", "contrato-servicos.pdf")
        viewModel.onContinueFromStep1()

        // Select Marina and Rafael
        viewModel.onOpenSignerSelectionSheet()
        viewModel.onTogglePickerSignerSelection("s-1")
        viewModel.onTogglePickerSignerSelection("s-2")
        viewModel.onConfirmPickerSelection()

        assertEquals(2, viewModel.uiState.value.assignedSigners.size)
        viewModel.onSignerRoleChanged("s-2", SignerRoleClass.CONTRATADO)

        // Advance to Step 3 (Envio e autenticação)
        viewModel.onContinueFromStep2()
        assertEquals(3, viewModel.uiState.value.currentStep)

        // Configure Delivery Channel, Auth Method and Additional Auths for s-1
        viewModel.onOpenDeliveryLinkSheet("s-1")
        viewModel.onSelectDeliveryChannel("s-1", SignerDeliveryChannel.WHATSAPP)

        viewModel.onOpenAuthMethodSheet("s-1")
        viewModel.onSelectAuthMethod("s-1", SignerAuthMethod.SMS)

        viewModel.onOpenAdditionalAuthsSheet("s-1")
        viewModel.onTogglePendingAdditionalAuth(AdditionalAuthOption.PHOTO_ID_DOCUMENT)
        viewModel.onTogglePendingAdditionalAuth(AdditionalAuthOption.SELFIE_WITH_BIOMETRICS)
        viewModel.onApplyAdditionalAuths()

        // Save draft from Step 3
        viewModel.onSaveDraftAndContinue("admin-1")

        val savedDoc = viewModel.uiState.value.completedDraftDocument
        assertNotNull(savedDoc)
        assertEquals("Contrato de serviço", savedDoc?.title)
        assertEquals("doc_internal_123.pdf", savedDoc?.fileName)
        assertEquals(6, savedDoc?.pageCount)
        assertEquals(DocumentStatus.DRAFT, savedDoc?.status)

        val savedSigners = fakeDocRepo.getDocumentSigners(savedDoc!!.id).getOrNull().orEmpty()
        assertEquals(2, savedSigners.size)
        val marinaRef = savedSigners.first { it.signerId == "s-1" }
        assertEquals(SignerRoleClass.CONTRATANTE, marinaRef.roleClass)
        assertEquals(SignerDeliveryChannel.WHATSAPP, marinaRef.deliveryChannel)
        assertEquals(SignerAuthMethod.SMS, marinaRef.authMethod)
        assertEquals(
            listOf(
                AdditionalAuthOption.PHOTO_ID_DOCUMENT,
                AdditionalAuthOption.SELFIE_WITH_BIOMETRICS
            ),
            marinaRef.additionalAuths
        )
    }

    private class FakePrepUserRepository : UserRepository {
        private val signersFlow = MutableStateFlow(
            listOf(
                User(
                    id = "s-1",
                    name = "Marina Albuquerque",
                    email = "marina.albuquerque@exemplo.com.br",
                    role = UserRole.SIGNER,
                    groupName = "Jurídico"
                ),
                User(
                    id = "s-2",
                    name = "Rafael Tavares",
                    email = "rafael.tavares@exemplo.com.br",
                    role = UserRole.SIGNER,
                    groupName = "Comercial"
                ),
                User(
                    id = "s-3",
                    name = "Beatriz Nogueira",
                    email = "beatriz.nogueira@exemplo.com.br",
                    role = UserRole.SIGNER,
                    groupName = "Jurídico"
                )
            )
        )

        override suspend fun login(email: String, password: String): AppResult<User> = AppResult.Error("")
        override suspend fun requestLoginCode(email: String): AppResult<User> = AppResult.Error("")
        override suspend fun verifyLoginCode(email: String, code: String): AppResult<User> = AppResult.Error("")
        override suspend fun getUserById(userId: String): AppResult<User> = AppResult.Error("")
        override suspend fun getUserByEmail(email: String): AppResult<User?> = AppResult.Success(null)
        override fun observeSigners(): Flow<List<User>> = signersFlow
        override fun observeUsersByRole(role: UserRole): Flow<List<User>> = signersFlow
        override suspend fun createUser(user: User): AppResult<User> = AppResult.Success(user)
        override suspend fun updateUser(user: User): AppResult<User> = AppResult.Success(user)
        override suspend fun deleteUser(userId: String): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakePdfStorageRepository : PdfStorageRepository {
        var shouldFail = false

        override suspend fun importPdfFromUri(
            uriString: String,
            fallbackName: String?
        ): AppResult<ImportedPdfFile> {
            val display = fallbackName ?: "documento.pdf"
            return if (shouldFail) {
                AppResult.Error("Não foi possível processar $display. Escolha outro arquivo PDF.")
            } else {
                AppResult.Success(
                    ImportedPdfFile(
                        internalFileName = "doc_internal_123.pdf",
                        originalDisplayName = display,
                        pageCount = 6,
                        fileSizeBytes = 1_887_436L
                    )
                )
            }
        }

        override suspend fun getPdfFile(internalFileName: String): AppResult<File> =
            AppResult.Error("Not used")

        override suspend fun deletePdfFile(internalFileName: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun embedSignaturesIntoPdf(
            internalFileName: String,
            fields: List<SignatureField>,
            signaturePngBytes: ByteArray
        ): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakePrepDocumentRepository : DocumentRepository {
        private val docs = mutableMapOf<String, Document>()
        private val signers = mutableListOf<DocumentSigner>()

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
            signers.removeAll { it.documentId == documentSigner.documentId && it.signerId == documentSigner.signerId }
            signers.add(documentSigner)
            return AppResult.Success(Unit)
        }

        override suspend fun removeSignerFromDocument(documentId: String, signerId: String): AppResult<Unit> {
            signers.removeAll { it.documentId == documentId && it.signerId == signerId }
            return AppResult.Success(Unit)
        }

        override suspend fun markDocumentAsSignedBySigner(
            documentId: String,
            signerId: String,
            signedAt: Long,
            signatureIp: String
        ): AppResult<Unit> = AppResult.Success(Unit)

        override fun observeSignatureFields(documentId: String): Flow<List<SignatureField>> = emptyFlow()
        override suspend fun getSignatureFields(documentId: String): AppResult<List<SignatureField>> = AppResult.Success(emptyList())
        override suspend fun getSignatureFieldsBySigner(documentId: String, signerId: String): AppResult<List<SignatureField>> = AppResult.Success(emptyList())
        override suspend fun saveSignatureField(field: SignatureField): AppResult<SignatureField> = AppResult.Success(field)
        override suspend fun deleteSignatureField(fieldId: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun deleteSignatureFieldsBySigner(documentId: String, signerId: String): AppResult<Unit> = AppResult.Success(Unit)
    }
}
