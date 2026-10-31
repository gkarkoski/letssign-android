package com.example.challenge_letssign.domain.usecase

import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.repository.UserRepository
import com.example.challenge_letssign.utils.AppResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): AppResult<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            return AppResult.Error("Informe o seu e-mail.")
        }
        if (!EMAIL_REGEX.matches(trimmedEmail)) {
            return AppResult.Error("E-mail inválido. Confira o formato.")
        }
        if (password.isBlank()) {
            return AppResult.Error("Informe a sua senha.")
        }
        return userRepository.login(trimmedEmail, password)
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
