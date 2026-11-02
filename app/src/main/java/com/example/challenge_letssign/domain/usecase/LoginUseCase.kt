package com.example.challenge_letssign.domain.usecase

import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.repository.UserRepository
import com.example.challenge_letssign.utils.AppResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun requestLoginCode(email: String): AppResult<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !EMAIL_REGEX.matches(trimmedEmail)) {
            return AppResult.Error(INVALID_EMAIL_MESSAGE)
        }
        return userRepository.requestLoginCode(trimmedEmail)
    }

    suspend fun verifyLoginCode(email: String, code: String): AppResult<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !EMAIL_REGEX.matches(trimmedEmail)) {
            return AppResult.Error(INVALID_EMAIL_MESSAGE)
        }
        val digitsOnly = code.filter { it.isDigit() }
        if (digitsOnly.length != 6 || digitsOnly in INVALID_OTP_CODES) {
            return AppResult.Error(INVALID_OTP_MESSAGE)
        }
        return userRepository.verifyLoginCode(trimmedEmail, digitsOnly)
    }

    suspend operator fun invoke(email: String, password: String): AppResult<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !EMAIL_REGEX.matches(trimmedEmail)) {
            return AppResult.Error(INVALID_EMAIL_MESSAGE)
        }
        if (password.isBlank()) {
            return AppResult.Error("Informe a sua senha.")
        }
        return userRepository.login(trimmedEmail, password)
    }

    companion object {
        const val INVALID_EMAIL_MESSAGE = "Informe um e-mail válido, como nome@empresa.com.br."
        const val INVALID_OTP_MESSAGE =
            "Código inválido ou expirado. Confira os dígitos ou peça um novo código."
        private val INVALID_OTP_CODES = setOf("000000", "482915")
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
