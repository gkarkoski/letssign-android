package com.example.challenge_letssign.ui.auth

import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.domain.repository.UserRepository
import com.example.challenge_letssign.domain.usecase.LoginUseCase
import com.example.challenge_letssign.ui.navigation.LetsSignRoutes
import com.example.challenge_letssign.utils.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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

@OptIn(ExperimentalCoroutinesApi::class)
class LoginAndRoutingTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeUserRepository: FakeAuthUserRepository
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeUserRepository = FakeAuthUserRepository()
        loginUseCase = LoginUseCase(fakeUserRepository)
        viewModel = LoginViewModel(loginUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onSubmitLogin with invalid email format sets Figma emailError message`() = runTest(testDispatcher) {
        viewModel.onEmailChanged("marina@empresa")
        viewModel.onPasswordChanged("123456")

        viewModel.onSubmitLogin()

        val state = viewModel.uiState.value
        assertEquals("E-mail inválido. Confira o formato.", state.emailError)
        assertNull(state.authenticatedUser)
    }

    @Test
    fun `onSubmitLogin with empty fields sets email and password errors`() = runTest(testDispatcher) {
        viewModel.onSubmitLogin()

        val state = viewModel.uiState.value
        assertEquals("Informe o seu e-mail.", state.emailError)
        assertEquals("Informe a sua senha.", state.passwordError)
        assertNull(state.authenticatedUser)
    }

    @Test
    fun `ADMIN login authenticates and resolves to AdminDashboard route`() = runTest(testDispatcher) {
        viewModel.onEmailChanged("admin@letssign.com")
        viewModel.onPasswordChanged("123456")

        viewModel.onSubmitLogin()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.authenticatedUser)
        assertEquals(UserRole.ADMIN, state.authenticatedUser?.role)

        val route = LetsSignRoutes.resolvePostLoginRoute(state.authenticatedUser!!)
        assertTrue(route.startsWith("admin_dashboard/"))
    }

    @Test
    fun `SIGNER login authenticates and resolves to SignerDashboard route`() = runTest(testDispatcher) {
        viewModel.onEmailChanged("carlos@letssign.com")
        viewModel.onPasswordChanged("123456")

        viewModel.onSubmitLogin()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.authenticatedUser)
        assertEquals(UserRole.SIGNER, state.authenticatedUser?.role)

        val route = LetsSignRoutes.resolvePostLoginRoute(state.authenticatedUser!!)
        assertTrue(route.startsWith("signer_dashboard/"))
    }

    @Test
    fun `wrong password sets generalError and keeps authenticatedUser null`() = runTest(testDispatcher) {
        viewModel.onEmailChanged("admin@letssign.com")
        viewModel.onPasswordChanged("senha_errada")

        viewModel.onSubmitLogin()

        val state = viewModel.uiState.value
        assertEquals("E-mail ou senha inválidos.", state.generalError)
        assertNull(state.authenticatedUser)
    }

    @Test
    fun `onTogglePasswordVisibility flips password visibility state`() {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
    }

    private class FakeAuthUserRepository : UserRepository {
        private val users = listOf(
            User("admin-1", "Admin LetsSign", "admin@letssign.com", "11999990000", "123456", UserRole.ADMIN),
            User("signer-1", "Carlos Silva", "carlos@letssign.com", "11988881111", "123456", UserRole.SIGNER)
        )

        override suspend fun login(email: String, password: String): AppResult<User> {
            val found = users.firstOrNull { it.email.equals(email, ignoreCase = true) && it.password == password }
            return if (found != null) {
                AppResult.Success(found)
            } else {
                AppResult.Error("E-mail ou senha inválidos.")
            }
        }

        override suspend fun getUserById(userId: String): AppResult<User> =
            AppResult.Error("Not implemented")

        override suspend fun getUserByEmail(email: String): AppResult<User?> =
            AppResult.Success(null)

        override fun observeSigners(): Flow<List<User>> = emptyFlow()
        override fun observeUsersByRole(role: UserRole): Flow<List<User>> = emptyFlow()
        override suspend fun createUser(user: User): AppResult<User> = AppResult.Success(user)
        override suspend fun updateUser(user: User): AppResult<User> = AppResult.Success(user)
        override suspend fun deleteUser(userId: String): AppResult<Unit> = AppResult.Success(Unit)
    }
}
