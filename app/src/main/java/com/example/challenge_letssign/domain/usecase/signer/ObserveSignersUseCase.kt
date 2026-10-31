package com.example.challenge_letssign.domain.usecase.signer

import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSignersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<List<User>> = userRepository.observeSigners()
}
