package com.example.challenge_letssign.data.repository.local

import com.example.challenge_letssign.data.di.IoDispatcher
import com.example.challenge_letssign.data.local.dao.UserDao
import com.example.challenge_letssign.data.local.entity.toDomain
import com.example.challenge_letssign.data.local.entity.toEntity
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.domain.repository.UserRepository
import com.example.challenge_letssign.utils.AppResult
import com.example.challenge_letssign.utils.runCatchingResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalUserRepository @Inject constructor(
    private val userDao: UserDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    override suspend fun login(email: String, password: String): AppResult<User> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao realizar login.") {
                val userEntity = userDao.getUserByCredentials(email.trim(), password)
                    ?: throw IllegalArgumentException("E-mail ou senha inválidos.")
                userEntity.toDomain()
            }
        }

    override suspend fun getUserById(userId: String): AppResult<User> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao buscar usuário.") {
                val userEntity = userDao.getUserById(userId)
                    ?: throw NoSuchElementException("Usuário não encontrado.")
                userEntity.toDomain()
            }
        }

    override suspend fun getUserByEmail(email: String): AppResult<User?> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao verificar e-mail.") {
                userDao.getUserByEmail(email.trim())?.toDomain()
            }
        }

    override fun observeSigners(): Flow<List<User>> =
        observeUsersByRole(UserRole.SIGNER)

    override fun observeUsersByRole(role: UserRole): Flow<List<User>> =
        userDao.observeUsersByRole(role)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun createUser(user: User): AppResult<User> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao cadastrar usuário.") {
                val existing = userDao.getUserByEmail(user.email.trim())
                if (existing != null) {
                    throw IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.")
                }
                userDao.insertUser(user.toEntity())
                user
            }
        }

    override suspend fun updateUser(user: User): AppResult<User> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao atualizar usuário.") {
                val existing = userDao.getUserByEmail(user.email.trim())
                if (existing != null && existing.id != user.id) {
                    throw IllegalArgumentException("Já existe outro usuário cadastrado com este e-mail.")
                }
                userDao.updateUser(user.toEntity())
                user
            }
        }

    override suspend fun deleteUser(userId: String): AppResult<Unit> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao remover usuário.") {
                userDao.deleteUserById(userId)
            }
        }
}
