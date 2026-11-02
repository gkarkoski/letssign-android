package com.example.challenge_letssign.domain.usecase.signer

import com.example.challenge_letssign.domain.model.PersonType
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
        personType: PersonType = PersonType.FISICA,
        cpfCnpj: String,
        name: String,
        email: String,
        birthDate: String = "",
        phone: String = "",
        password: String = "123456",
        groupName: String = ""
    ): AppResult<User> {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedCpfCnpj = cpfCnpj.trim()
        val digitsCpfCnpj = trimmedCpfCnpj.filter { it.isDigit() }
        val trimmedBirthDate = birthDate.trim()
        val effectivePassword = password.trim().ifBlank { "123456" }

        if (personType == PersonType.FISICA) {
            if (digitsCpfCnpj.length != 11) {
                return AppResult.Error(CPF_ERROR_MESSAGE)
            }
        } else {
            if (digitsCpfCnpj.length != 14) {
                return AppResult.Error(CNPJ_ERROR_MESSAGE)
            }
        }

        if (trimmedName.isBlank()) {
            return AppResult.Error(NAME_ERROR_MESSAGE)
        }

        if (trimmedEmail.isBlank() || !EMAIL_REGEX.matches(trimmedEmail)) {
            return AppResult.Error(EMAIL_ERROR_MESSAGE)
        }

        if (personType == PersonType.FISICA && !isValidBirthDate(trimmedBirthDate)) {
            return AppResult.Error(BIRTH_DATE_ERROR_MESSAGE)
        }

        // Verificar se e-mail já está em uso por outro usuário
        when (val existingResult = userRepository.getUserByEmail(trimmedEmail)) {
            is AppResult.Success -> {
                val existingUser = existingResult.data
                if (existingUser != null && existingUser.id != signerId) {
                    return AppResult.Error(DUPLICATE_EMAIL_ERROR_MESSAGE)
                }
            }
            is AppResult.Error -> return existingResult
        }

        val existingSigner = if (!signerId.isNullOrBlank()) {
            userRepository.getUserById(signerId).getOrNull()
        } else {
            null
        }

        val formattedDoc = if (personType == PersonType.FISICA) {
            formatCpf(digitsCpfCnpj)
        } else {
            formatCnpj(digitsCpfCnpj)
        }

        val signer = User(
            id = signerId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
            name = trimmedName,
            email = trimmedEmail,
            phone = phone.trim().ifBlank { existingSigner?.phone.orEmpty() },
            password = effectivePassword,
            role = UserRole.SIGNER,
            personType = personType,
            cpfCnpj = formattedDoc,
            birthDate = if (personType == PersonType.FISICA) formatBirthDate(trimmedBirthDate) else "",
            groupName = groupName.trim().ifBlank { existingSigner?.groupName.orEmpty() }
        )

        return if (signerId.isNullOrBlank()) {
            userRepository.createUser(signer)
        } else {
            userRepository.updateUser(signer)
        }
    }

    companion object {
        const val CPF_ERROR_MESSAGE = "CPF incompleto. Use 11 dígitos."
        const val CNPJ_ERROR_MESSAGE = "CNPJ incompleto. Use 14 dígitos."
        const val NAME_ERROR_MESSAGE = "Informe o nome do contato."
        const val EMAIL_ERROR_MESSAGE = "Informe um e-mail válido, como nome@empresa.com.br."
        const val BIRTH_DATE_ERROR_MESSAGE = "Informe a data de nascimento no formato dd/mm/aaaa"
        const val DUPLICATE_EMAIL_ERROR_MESSAGE = "Já existe um usuário cadastrado com este e-mail."

        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        private val BIRTH_DATE_REGEX = Regex("^\\d{2}/\\d{2}/\\d{4}$")

        fun isValidBirthDate(raw: String): Boolean {
            val trimmed = raw.trim()
            if (BIRTH_DATE_REGEX.matches(trimmed)) return true
            val digits = trimmed.filter { it.isDigit() }
            return digits.length == 8
        }

        fun formatCpf(digits: String): String {
            val d = digits.filter { it.isDigit() }.take(11)
            if (d.length != 11) return digits
            return "${d.substring(0, 3)}.${d.substring(3, 6)}.${d.substring(6, 9)}-${d.substring(9, 11)}"
        }

        fun formatCnpj(digits: String): String {
            val d = digits.filter { it.isDigit() }.take(14)
            if (d.length != 14) return digits
            return "${d.substring(0, 2)}.${d.substring(2, 5)}.${d.substring(5, 8)}/${d.substring(8, 12)}-${d.substring(12, 14)}"
        }

        fun formatBirthDate(raw: String): String {
            val trimmed = raw.trim()
            if (BIRTH_DATE_REGEX.matches(trimmed)) return trimmed
            val d = trimmed.filter { it.isDigit() }.take(8)
            if (d.length != 8) return trimmed
            return "${d.substring(0, 2)}/${d.substring(2, 4)}/${d.substring(4, 8)}"
        }
    }
}
