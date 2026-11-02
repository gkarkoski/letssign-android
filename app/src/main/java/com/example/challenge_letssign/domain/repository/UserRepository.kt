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

    suspend fun assignGroupToUsers(groupName: String, userIds: Set<String>): AppResult<Unit> {
        val trimmedGroup = groupName.trim()
        if (trimmedGroup.isBlank() || userIds.isEmpty()) {
            return AppResult.Error("Informe o nome do grupo e selecione ao menos 1 contato.")
        }
        for (id in userIds) {
            val user = getUserById(id).getOrNull() ?: continue
            updateUser(user.copy(groupName = trimmedGroup))
        }
        return AppResult.Success(Unit)
    }
}
