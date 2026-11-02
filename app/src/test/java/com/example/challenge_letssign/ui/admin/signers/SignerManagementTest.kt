package com.example.challenge_letssign.ui.admin.signers

import com.example.challenge_letssign.domain.model.PersonType
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.domain.repository.UserRepository
import com.example.challenge_letssign.domain.usecase.signer.DeleteSignerUseCase
import com.example.challenge_letssign.domain.usecase.signer.ObserveSignersUseCase
import com.example.challenge_letssign.domain.usecase.signer.SaveSignerUseCase
import com.example.challenge_letssign.ui.components.extractInitials
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignerManagementTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeUserRepository: FakeSignerUserRepository
    private lateinit var viewModel: SignerManagementViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeUserRepository = FakeSignerUserRepository()
        viewModel = SignerManagementViewModel(
            observeSignersUseCase = ObserveSignersUseCase(fakeUserRepository),
            saveSignerUseCase = SaveSignerUseCase(fakeUserRepository),
            deleteSignerUseCase = DeleteSignerUseCase(fakeUserRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `extractInitials generates expected 2-letter uppercase initials`() {
        assertEquals("MA", extractInitials("Marina Albuquerque"))
        assertEquals("NS", extractInitials("Nova Soluções Ltda."))
        assertEquals("JO", extractInitials("João"))
        assertEquals("?", extractInitials("   "))
    }

    @Test
    fun `Contato Novo Erros - saving invalid Fisica fields sets exact Figma field errors and summary banner`() = runTest(testDispatcher) {
        viewModel.onOpenCreateForm()
        viewModel.onCpfCnpjChanged("123.456.78")
        viewModel.onNameChanged("Camila Duarte")
        viewModel.onEmailChanged("camila.duarte@exemplo")
        viewModel.onBirthDateChanged("01/01/22")

        viewModel.onSaveSigner()

        val state = viewModel.uiState.value
        assertEquals("CPF incompleto. Use 11 dígitos.", state.cpfCnpjError)
        assertNull(state.nameError)
        assertEquals("Informe um e-mail válido, como nome@empresa.com.br.", state.emailError)
        assertEquals("Informe a data de nascimento no formato dd/mm/aaaa", state.birthDateError)
        assertEquals(3, state.validationErrorCount)
        assertEquals("Corrija 3 campos para salvar o contato.", state.formSummaryErrorBanner)
        assertTrue(state.isFormVisible)
    }

    @Test
    fun `Contato Novo Fisica - valid fields format CPF and birthDate, save contact and show Contato salvo snackbar`() = runTest(testDispatcher) {
        viewModel.onOpenCreateForm()
        viewModel.onCpfCnpjChanged("12345678909")
        viewModel.onNameChanged("Marina Albuquerque")
        viewModel.onEmailChanged("marina.albuquerque@exemplo.com.br")
        viewModel.onBirthDateChanged("14031988")

        viewModel.onSaveSigner()

        val state = viewModel.uiState.value
        assertFalse(state.isFormVisible)
        assertEquals("Contato salvo com sucesso.", state.snackbarMessage)
        val saved = state.allSigners.firstOrNull { it.email == "marina.albuquerque@exemplo.com.br" }
        assertNotNull(saved)
        assertEquals(PersonType.FISICA, saved?.personType)
        assertEquals("123.456.789-09", saved?.cpfCnpj)
        assertEquals("14/03/1988", saved?.birthDate)
    }

    @Test
    fun `Contato Novo Juridica - validates 14-digit CNPJ, Razao social and Email without requiring birthDate`() = runTest(testDispatcher) {
        viewModel.onOpenCreateForm()
        viewModel.onPersonTypeChanged(PersonType.JURIDICA)
        viewModel.onCpfCnpjChanged("12345678000190")
        viewModel.onNameChanged("Nova Soluções Ltda.")
        viewModel.onEmailChanged("contato@novasolucoes.com.br")

        viewModel.onSaveSigner()

        val state = viewModel.uiState.value
        assertFalse(state.isFormVisible)
        assertEquals("Contato salvo com sucesso.", state.snackbarMessage)
        val saved = state.allSigners.firstOrNull { it.email == "contato@novasolucoes.com.br" }
        assertNotNull(saved)
        assertEquals(PersonType.JURIDICA, saved?.personType)
        assertEquals("12.345.678/0001-90", saved?.cpfCnpj)
        assertEquals("", saved?.birthDate)
    }

    @Test
    fun `onSaveSigner with duplicate email shows duplicate email error`() = runTest(testDispatcher) {
        viewModel.onOpenCreateForm()
        viewModel.onCpfCnpjChanged("11122233344")
        viewModel.onNameChanged("Outro Rafael")
        viewModel.onEmailChanged("rafael.tavares@exemplo.com.br")
        viewModel.onBirthDateChanged("03/09/1985")

        viewModel.onSaveSigner()

        val state = viewModel.uiState.value
        assertEquals("Já existe um usuário cadastrado com este e-mail.", state.emailError)
        assertTrue(state.isFormVisible)
    }

    @Test
    fun `Contato Editar - locks personType, allows updating contact and deleting contact`() = runTest(testDispatcher) {
        val rafael = viewModel.uiState.value.allSigners.first { it.email == "rafael.tavares@exemplo.com.br" }
        viewModel.onOpenEditForm(rafael)

        // Attempting to change personType while editing should be ignored
        viewModel.onPersonTypeChanged(PersonType.JURIDICA)
        assertEquals(PersonType.FISICA, viewModel.uiState.value.personType)

        viewModel.onNameChanged("Rafael Tavares Atualizado")
        viewModel.onSaveSigner()

        val afterEdit = viewModel.uiState.value
        assertFalse(afterEdit.isFormVisible)
        assertEquals("Contato salvo com sucesso.", afterEdit.snackbarMessage)
        assertEquals("Rafael Tavares Atualizado", afterEdit.allSigners.first { it.id == rafael.id }.name)

        // Reopen edit form and delete contact
        val updatedRafael = afterEdit.allSigners.first { it.id == rafael.id }
        viewModel.onOpenEditForm(updatedRafael)
        viewModel.onDeleteEditingSigner()
        assertNotNull(viewModel.uiState.value.signerPendingDeletion)

        viewModel.onConfirmDeleteSigner()
        val afterDelete = viewModel.uiState.value
        assertNull(afterDelete.signerPendingDeletion)
        assertFalse(afterDelete.isFormVisible)
        assertEquals("Contato excluído.", afterDelete.snackbarMessage)
        assertFalse(afterDelete.allSigners.any { it.id == rafael.id })
    }

    @Test
    fun `Contatos Busca and Limpar busca filter by name, email or CPF_CNPJ`() = runTest(testDispatcher) {
        viewModel.onSearchQueryChanged("beatriz")
        assertEquals(1, viewModel.uiState.value.filteredSigners.size)
        assertEquals("beatriz.nogueira@exemplo.com.br", viewModel.uiState.value.filteredSigners.first().email)

        // Search by CPF digits
        viewModel.onSearchQueryChanged("321654")
        assertEquals(1, viewModel.uiState.value.filteredSigners.size)
        assertEquals("Rafael Tavares", viewModel.uiState.value.filteredSigners.first().name)

        // Clear search
        viewModel.onClearSearch()
        assertEquals(2, viewModel.uiState.value.filteredSigners.size)
    }

    private class FakeSignerUserRepository : UserRepository {
        private val usersFlow = MutableStateFlow(
            listOf(
                User(
                    id = "admin-1",
                    name = "Admin",
                    email = "admin@exemplo.com.br",
                    role = UserRole.ADMIN
                ),
                User(
                    id = "s-1",
                    name = "Rafael Tavares",
                    email = "rafael.tavares@exemplo.com.br",
                    role = UserRole.SIGNER,
                    personType = PersonType.FISICA,
                    cpfCnpj = "321.654.987-10",
                    birthDate = "03/09/1985"
                ),
                User(
                    id = "s-2",
                    name = "Beatriz Nogueira",
                    email = "beatriz.nogueira@exemplo.com.br",
                    role = UserRole.SIGNER,
                    personType = PersonType.FISICA,
                    cpfCnpj = "456.789.123-44",
                    birthDate = "22/07/1993"
                )
            )
        )

        override suspend fun login(email: String, password: String): AppResult<User> =
            AppResult.Error("Not used")

        override suspend fun requestLoginCode(email: String): AppResult<User> =
            AppResult.Error("Not used")

        override suspend fun verifyLoginCode(email: String, code: String): AppResult<User> =
            AppResult.Error("Not used")

        override suspend fun getUserById(userId: String): AppResult<User> {
            val user = usersFlow.value.firstOrNull { it.id == userId }
                ?: return AppResult.Error("Não encontrado")
            return AppResult.Success(user)
        }

        override suspend fun getUserByEmail(email: String): AppResult<User?> {
            val found = usersFlow.value.firstOrNull { it.email.equals(email, ignoreCase = true) }
            return AppResult.Success(found)
        }

        override fun observeSigners(): Flow<List<User>> =
            observeUsersByRole(UserRole.SIGNER)

        override fun observeUsersByRole(role: UserRole): Flow<List<User>> =
            usersFlow.map { list -> list.filter { it.role == role }.sortedBy { it.name } }

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
}
