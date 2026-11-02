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
        viewModel = LoginViewModel(loginUseCase).apply {
            sendCodeDelayMillis = 0L
            enableCountdownTicker = false
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Login Erro - onSendCodeClick with invalid email sets exact Figma error message`() = runTest(testDispatcher) {
        viewModel.onEmailChanged("admin@exemplo")
        viewModel.onSendCodeClick()

        val state = viewModel.uiState.value
        assertEquals(LoginStep.EMAIL, state.step)
        assertEquals("Informe um e-mail válido, como nome@empresa.com.br.", state.emailError)
        assertNull(state.authenticatedUser)
    }

    @Test
    fun `Login Geral to Login Codigo - valid email transitions to CODE step with 00_42 countdown`() = runTest(testDispatcher) {
        viewModel.onEmailChanged("admin@exemplo.com.br")
        viewModel.onSendCodeClick()

        val state = viewModel.uiState.value
        assertEquals(LoginStep.CODE, state.step)
        assertFalse(state.isSendingCode)
        assertNull(state.emailError)
        assertEquals(42, state.resendCountdownSeconds)
        assertEquals("00:42", state.formattedCountdown)
    }

    @Test
    fun `ADMIN OTP verification authenticates and resolves to AdminDashboard route`() = runTest(testDispatcher) {
        viewModel.onEmailChanged("admin@exemplo.com.br")
        viewModel.onSendCodeClick()

        viewModel.onOtpCodeChanged("482910")
        assertTrue(viewModel.uiState.value.isCodeComplete)

        viewModel.onVerifyCodeClick()

        val state = viewModel.uiState.value
        assertNull(state.codeError)
        assertNotNull(state.authenticatedUser)
        assertEquals(UserRole.ADMIN, state.authenticatedUser?.role)

        val route = LetsSignRoutes.resolvePostLoginRoute(state.authenticatedUser!!)
        assertTrue(route.startsWith("admin_dashboard/"))
    }

    @Test
    fun `SIGNER OTP verification authenticates and resolves to SignerDashboard route`() = runTest(testDispatcher) {
        viewModel.onEmailChanged("rafael.tavares@exemplo.com.br")
        viewModel.onSendCodeClick()

        viewModel.onOtpCodeChanged("123456")
        viewModel.onVerifyCodeClick()

        val state = viewModel.uiState.value
        assertNull(state.codeError)
        assertNotNull(state.authenticatedUser)
        assertEquals(UserRole.SIGNER, state.authenticatedUser?.role)

        val route = LetsSignRoutes.resolvePostLoginRoute(state.authenticatedUser!!)
        assertTrue(route.startsWith("signer_dashboard/"))
    }

    @Test
    fun `Login Codigo invalido - invalid code 482915 sets codeError and Reenviar codigo resets state`() = runTest(testDispatcher) {
        viewModel.onEmailChanged("admin@exemplo.com.br")
        viewModel.onSendCodeClick()

        viewModel.onOtpCodeChanged("482915")
        viewModel.onVerifyCodeClick()

        val errorState = viewModel.uiState.value
        assertEquals(
            "Código inválido ou expirado. Confira os dígitos ou peça um novo código.",
            errorState.codeError
        )
        assertNull(errorState.authenticatedUser)

        // Tapping Reenviar código clears error and resets code & countdown
        viewModel.onResendCodeClick()
        val afterResend = viewModel.uiState.value
        assertNull(afterResend.codeError)
        assertEquals("", afterResend.otpCode)
        assertEquals(42, afterResend.resendCountdownSeconds)
    }

    @Test
    fun `Usar outro e-mail returns from CODE step to EMAIL step`() = runTest(testDispatcher) {
        viewModel.onEmailChanged("admin@exemplo.com.br")
        viewModel.onSendCodeClick()
        assertEquals(LoginStep.CODE, viewModel.uiState.value.step)

        viewModel.onUseAnotherEmailClick()
        val state = viewModel.uiState.value
        assertEquals(LoginStep.EMAIL, state.step)
        assertEquals("", state.otpCode)
        assertNull(state.codeError)
    }

    private class FakeAuthUserRepository : UserRepository {
        private val users = listOf(
            User("admin-1", "Administrador LetsSign", "admin@exemplo.com.br", "11999990000", "123456", UserRole.ADMIN),
            User("signer-1", "Rafael Tavares", "rafael.tavares@exemplo.com.br", "11988881111", "123456", UserRole.SIGNER)
        )

        override suspend fun login(email: String, password: String): AppResult<User> {
            val found = users.firstOrNull { it.email.equals(email, ignoreCase = true) && it.password == password }
            return if (found != null) {
                AppResult.Success(found)
            } else {
                AppResult.Error("E-mail ou senha inválidos.")
            }
        }

        override suspend fun requestLoginCode(email: String): AppResult<User> {
            val found = users.firstOrNull { it.email.equals(email, ignoreCase = true) }
            return if (found != null) {
                AppResult.Success(found)
            } else {
                AppResult.Error(LoginUseCase.INVALID_EMAIL_MESSAGE)
            }
        }

        override suspend fun verifyLoginCode(email: String, code: String): AppResult<User> {
            if (code.length != 6 || code == "482915" || code == "000000") {
                return AppResult.Error(LoginUseCase.INVALID_OTP_MESSAGE)
            }
            val found = users.firstOrNull { it.email.equals(email, ignoreCase = true) }
                ?: return AppResult.Error(LoginUseCase.INVALID_EMAIL_MESSAGE)
            return AppResult.Success(found)
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
