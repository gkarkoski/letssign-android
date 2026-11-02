package com.example.challenge_letssign.ui.admin.signers

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
        assertEquals("CS", extractInitials("Carlos Silva"))
        assertEquals("JO", extractInitials("João"))
        assertEquals("?", extractInitials("   "))
    }

    @Test
    fun `onSaveSigner with empty fields sets validation errors`() = runTest(testDispatcher) {
        viewModel.onOpenCreateForm()
        viewModel.onPasswordChanged("")

        viewModel.onSaveSigner()

        val state = viewModel.uiState.value
        assertEquals("Informe o nome completo do signatário.", state.nameError)
        assertEquals("E-mail inválido. Confira o formato.", state.emailError)
        assertEquals("Informe o telefone do signatário.", state.phoneError)
        assertEquals("Informe uma senha de acesso para o signatário.", state.passwordError)
        assertTrue(state.isFormVisible)
    }

    @Test
    fun `onSaveSigner with duplicate email shows duplicate email error`() = runTest(testDispatcher) {
        viewModel.onOpenCreateForm()
        viewModel.onNameChanged("Outro Carlos")
        viewModel.onEmailChanged("carlos@letssign.com")
        viewModel.onPhoneChanged("(11) 91111-2222")

        viewModel.onSaveSigner()

        val state = viewModel.uiState.value
        assertEquals("Já existe um usuário cadastrado com este e-mail.", state.emailError)
        assertTrue(state.isFormVisible)
    }

    @Test
    fun `onSaveSigner with valid new signer adds to list and shows snackbar`() = runTest(testDispatcher) {
        viewModel.onOpenCreateForm()
        viewModel.onNameChanged("Marina Albuquerque")
        viewModel.onEmailChanged("marina.albuquerque@exemplo.com.br")
        viewModel.onPhoneChanged("(11) 95555-6666")

        viewModel.onSaveSigner()

        val state = viewModel.uiState.value
        assertFalse(state.isFormVisible)
        assertEquals("Signatário salvo", state.snackbarMessage)
        assertTrue(state.allSigners.any { it.email == "marina.albuquerque@exemplo.com.br" })
    }

    @Test
    fun `editing existing signer allows keeping same email and updates name`() = runTest(testDispatcher) {
        val carlos = viewModel.uiState.value.allSigners.first { it.email == "carlos@letssign.com" }
        viewModel.onOpenEditForm(carlos)
        viewModel.onNameChanged("Carlos Silva Junior")

        viewModel.onSaveSigner()

        val state = viewModel.uiState.value
        assertFalse(state.isFormVisible)
        assertEquals("Signatário salvo", state.snackbarMessage)
        val updated = state.allSigners.first { it.id == carlos.id }
        assertEquals("Carlos Silva Junior", updated.name)
    }

    @Test
    fun `search query filters signers by name or email`() = runTest(testDispatcher) {
        viewModel.onSearchQueryChanged("mariana")
        assertEquals(1, viewModel.uiState.value.filteredSigners.size)
        assertEquals("mariana@letssign.com", viewModel.uiState.value.filteredSigners.first().email)

        viewModel.onSearchQueryChanged("")
        assertEquals(2, viewModel.uiState.value.filteredSigners.size)
    }

    @Test
    fun `delete signer confirmation flow removes signer and displays snackbar`() = runTest(testDispatcher) {
        val target = viewModel.uiState.value.allSigners.first()
        viewModel.onRequestDeleteSigner(target)
        assertNotNull(viewModel.uiState.value.signerPendingDeletion)

        viewModel.onConfirmDeleteSigner()

        val state = viewModel.uiState.value
        assertNull(state.signerPendingDeletion)
        assertEquals("Signatário excluído", state.snackbarMessage)
        assertFalse(state.allSigners.any { it.id == target.id })
    }

    private class FakeSignerUserRepository : UserRepository {
        private val usersFlow = MutableStateFlow(
            listOf(
                User("admin-1", "Admin", "admin@letssign.com", "11999990000", "123456", UserRole.ADMIN),
                User("s-1", "Carlos Silva", "carlos@letssign.com", "11988881111", "123456", UserRole.SIGNER),
                User("s-2", "Mariana Souza", "mariana@letssign.com", "11977772222", "123456", UserRole.SIGNER)
            )
        )

        override suspend fun login(email: String, password: String): AppResult<User> =
            AppResult.Error("Not used")

        override suspend fun requestLoginCode(email: String): AppResult<User> =
            AppResult.Error("Not used")

        override suspend fun verifyLoginCode(
            email: String,
            code: String
        ): AppResult<User> = AppResult.Error("Not used")

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
