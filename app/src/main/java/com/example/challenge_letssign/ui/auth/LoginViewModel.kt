package com.example.challenge_letssign.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.usecase.LoginUseCase
import com.example.challenge_letssign.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginStep {
    EMAIL,
    CODE
}

data class LoginUiState(
    val step: LoginStep = LoginStep.EMAIL,
    val email: String = "",
    val otpCode: String = "",
    val isSendingCode: Boolean = false,
    val emailError: String? = null,
    val codeError: String? = null,
    val resendCountdownSeconds: Int = DEFAULT_RESEND_SECONDS,
    val authenticatedUser: User? = null
) {
    val isCodeComplete: Boolean
        get() = otpCode.filter { it.isDigit() }.length == 6

    val formattedCountdown: String
        get() = "00:${resendCountdownSeconds.coerceAtLeast(0).toString().padStart(2, '0')}"

    companion object {
        const val DEFAULT_RESEND_SECONDS = 42
        const val SEND_CODE_DELAY_MS = 1200L
    }
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    var sendCodeDelayMillis: Long = LoginUiState.SEND_CODE_DELAY_MS
    var enableCountdownTicker: Boolean = true

    private var sendCodeJob: Job? = null
    private var countdownJob: Job? = null

    fun onEmailChanged(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                emailError = null
            )
        }
    }

    /**
     * Fluxo 1 (Login Geral -> Enviar código):
     * - Se e-mail inválido: mostra estado `Login Erro` ("Informe um e-mail válido, como nome@empresa.com.br.").
     * - Se e-mail válido: mostra estado `Login Carregando` ("Enviando código...") por 1,2 s e avança para `Login Código`.
     */
    fun onSendCodeClick() {
        val currentState = _uiState.value
        if (currentState.isSendingCode) return

        val trimmedEmail = currentState.email.trim()
        if (trimmedEmail.isBlank() || !EMAIL_REGEX.matches(trimmedEmail)) {
            _uiState.update {
                it.copy(
                    emailError = LoginUseCase.INVALID_EMAIL_MESSAGE,
                    isSendingCode = false
                )
            }
            return
        }

        sendCodeJob?.cancel()
        sendCodeJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingCode = true,
                    emailError = null,
                    codeError = null
                )
            }

            if (sendCodeDelayMillis > 0L) {
                delay(sendCodeDelayMillis)
            }

            when (val result = loginUseCase.requestLoginCode(trimmedEmail)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            step = LoginStep.CODE,
                            email = trimmedEmail,
                            otpCode = "",
                            isSendingCode = false,
                            emailError = null,
                            codeError = null,
                            resendCountdownSeconds = LoginUiState.DEFAULT_RESEND_SECONDS
                        )
                    }
                    startCountdownTimer()
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            emailError = result.message
                        )
                    }
                }
            }
        }
    }

    fun onOtpCodeChanged(newCode: String) {
        val sanitized = newCode.filter { it.isDigit() }.take(6)
        _uiState.update {
            it.copy(
                otpCode = sanitized,
                codeError = null
            )
        }
    }

    /**
     * Fluxo 1 (Login Código / Login Código inválido -> Entrar):
     * Valida os 6 dígitos e autentica o usuário, redirecionando conforme o papel (ADMIN ou SIGNER).
     */
    fun onVerifyCodeClick() {
        val currentState = _uiState.value
        val trimmedEmail = currentState.email.trim()

        viewModelScope.launch {
            when (val result = loginUseCase.verifyLoginCode(trimmedEmail, currentState.otpCode)) {
                is AppResult.Success -> {
                    countdownJob?.cancel()
                    _uiState.update {
                        it.copy(
                            codeError = null,
                            authenticatedUser = result.data
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            codeError = result.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Fluxo 1 (Login Código inválido -> Reenviar código -> Login Código):
     */
    fun onResendCodeClick() {
        _uiState.update {
            it.copy(
                otpCode = "",
                codeError = null,
                resendCountdownSeconds = LoginUiState.DEFAULT_RESEND_SECONDS
            )
        }
        startCountdownTimer()
    }

    /**
     * Fluxo 1 (Login Código / Login Código inválido -> Usar outro e-mail -> Login Geral):
     */
    fun onUseAnotherEmailClick() {
        sendCodeJob?.cancel()
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                step = LoginStep.EMAIL,
                otpCode = "",
                codeError = null,
                isSendingCode = false
            )
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(authenticatedUser = null) }
    }

    private fun startCountdownTimer() {
        countdownJob?.cancel()
        if (!enableCountdownTicker) return
        countdownJob = viewModelScope.launch {
            while (_uiState.value.step == LoginStep.CODE && _uiState.value.resendCountdownSeconds > 0) {
                delay(1000L)
                _uiState.update { state ->
                    state.copy(
                        resendCountdownSeconds = (state.resendCountdownSeconds - 1).coerceAtLeast(0)
                    )
                }
            }
        }
    }

    override fun onCleared() {
        sendCodeJob?.cancel()
        countdownJob?.cancel()
        super.onCleared()
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
