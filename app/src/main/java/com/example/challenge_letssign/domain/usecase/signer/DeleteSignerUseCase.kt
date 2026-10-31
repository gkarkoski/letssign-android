package com.example.challenge_letssign.domain.usecase.signer

import com.example.challenge_letssign.domain.repository.UserRepository
import com.example.challenge_letssign.utils.AppResult
import javax.inject.Inject

class DeleteSignerUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(signerId: String): AppResult<Unit> {
        if (signerId.isBlank()) {
            return AppResult.Error("ID do signatário inválido.")
        }
        return userRepository.deleteUser(signerId)
    }
}
