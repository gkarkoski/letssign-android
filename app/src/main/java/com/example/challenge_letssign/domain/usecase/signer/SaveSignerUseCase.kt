package com.example.challenge_letssign.domain.usecase.signer

import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.domain.repository.UserRepository
import com.example.challenge_letssign.utils.AppResult
import java.util.UUID
import javax.inject.Inject

class SaveSignerUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        signerId: String? = null,
        name: String,
        email: String,
        phone: String,
        password: String
    ): AppResult<User> {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()
        val effectivePassword = password.trim().ifBlank { "123456" }

        if (trimmedName.isBlank()) {
            return AppResult.Error("Informe o nome completo do signatário.")
        }
        if (trimmedEmail.isBlank() || !EMAIL_REGEX.matches(trimmedEmail)) {
            return AppResult.Error("E-mail inválido. Confira o formato.")
        }
        if (trimmedPhone.isBlank()) {
            return AppResult.Error("Informe o telefone do signatário.")
        }

        // Verificar se e-mail já está em uso
        when (val existingResult = userRepository.getUserByEmail(trimmedEmail)) {
            is AppResult.Success -> {
                val existingUser = existingResult.data
                if (existingUser != null && existingUser.id != signerId) {
                    return AppResult.Error("Já existe um usuário cadastrado com este e-mail.")
                }
            }
            is AppResult.Error -> return existingResult
        }

        val signer = User(
            id = signerId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
            name = trimmedName,
            email = trimmedEmail,
            phone = trimmedPhone,
            password = effectivePassword,
            role = UserRole.SIGNER
        )

        return if (signerId.isNullOrBlank()) {
            userRepository.createUser(signer)
        } else {
            userRepository.updateUser(signer)
        }
    }

    companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
