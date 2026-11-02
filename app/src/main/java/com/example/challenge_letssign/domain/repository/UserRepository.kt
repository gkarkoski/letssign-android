package com.example.challenge_letssign.domain.repository

import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.utils.AppResult
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(email: String, password: String): AppResult<User>
    suspend fun requestLoginCode(email: String): AppResult<User>
    suspend fun verifyLoginCode(email: String, code: String): AppResult<User>
    suspend fun getUserById(userId: String): AppResult<User>
    suspend fun getUserByEmail(email: String): AppResult<User?>
    fun observeSigners(): Flow<List<User>>
    fun observeUsersByRole(role: UserRole): Flow<List<User>>
    suspend fun createUser(user: User): AppResult<User>
    suspend fun updateUser(user: User): AppResult<User>
    suspend fun deleteUser(userId: String): AppResult<Unit>
}
