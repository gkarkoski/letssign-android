package com.example.challenge_letssign.data.repository.local

import com.example.challenge_letssign.data.di.IoDispatcher
import com.example.challenge_letssign.data.local.dao.UserDao
import com.example.challenge_letssign.data.local.entity.UserEntity
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
                val resolvedEmail = resolveAliasEmail(email.trim())
                val userEntity = userDao.getUserByCredentials(resolvedEmail, password)
                    ?: userDao.getUserByCredentials(email.trim(), password)
                    ?: throw IllegalArgumentException("E-mail ou senha inválidos.")
                userEntity.toDomain()
            }
        }

    override suspend fun requestLoginCode(
        email: String,
        expectedRole: UserRole?
    ): AppResult<User> = withContext(ioDispatcher) {
        runCatchingResult("Informe um e-mail válido, como nome@empresa.com.br.") {
            val trimmed = email.trim()
            if (!isValidEmailFormat(trimmed)) {
                throw IllegalArgumentException("Informe um e-mail válido, como nome@empresa.com.br.")
            }
            val resolvedEmail = resolveAliasEmail(trimmed)
            val userEntity = findUserOrFallbackByRole(resolvedEmail, trimmed, expectedRole)
                ?: throw IllegalArgumentException("Informe um e-mail válido, como nome@empresa.com.br.")
            userEntity.toDomain()
        }
    }

    override suspend fun verifyLoginCode(
        email: String,
        code: String,
        expectedRole: UserRole?
    ): AppResult<User> = withContext(ioDispatcher) {
        runCatchingResult("Código inválido ou expirado.") {
            val digitsOnly = code.filter { it.isDigit() }
            if (digitsOnly.length != 6 || digitsOnly == INVALID_TEST_OTP_CODE) {
                throw IllegalArgumentException("Código inválido ou expirado. Peça um novo código para continuar.")
            }
            val trimmed = email.trim()
            val resolvedEmail = resolveAliasEmail(trimmed)
            val userEntity = findUserOrFallbackByRole(resolvedEmail, trimmed, expectedRole)
                ?: throw IllegalArgumentException("Usuário não encontrado para o e-mail informado.")
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
                val trimmed = email.trim()
                val direct = userDao.getUserByEmail(trimmed)
                direct?.toDomain()
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
                val trimmedEmail = user.email.trim()
                if (trimmedEmail.isNotEmpty()) {
                    val existing = userDao.getUserByEmail(trimmedEmail)
                    if (existing != null) {
                        throw IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.")
                    }
                }
                userDao.insertUser(user.toEntity())
                user
            }
        }

    override suspend fun updateUser(user: User): AppResult<User> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao atualizar usuário.") {
                val trimmedEmail = user.email.trim()
                if (trimmedEmail.isNotEmpty()) {
                    val existing = userDao.getUserByEmail(trimmedEmail)
                    if (existing != null && existing.id != user.id) {
                        throw IllegalArgumentException("Já existe outro usuário cadastrado com este e-mail.")
                    }
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

    private suspend fun findUserOrFallbackByRole(
        resolvedEmail: String,
        originalEmail: String,
        expectedRole: UserRole?
    ): UserEntity? {
        val byResolved = userDao.getUserByEmail(resolvedEmail)
        if (byResolved != null) return byResolved
        val byOriginal = userDao.getUserByEmail(originalEmail)
        if (byOriginal != null) return byOriginal

        // Para facilitar testes no protótipo: se o e-mail for válido e o perfil foi indicado
        // (ex: admin@empresa.com.br no Login Admin ou qualquer e-mail válido no Login Signatário),
        // retorna o usuário principal daquele perfil se não houver match exato.
        return when (expectedRole) {
            UserRole.ADMIN -> userDao.getUserByEmail("admin@exemplo.com.br")
                ?: userDao.getUserByEmail("admin@letssign.com")
            UserRole.SIGNER -> userDao.getUserByEmail("rafael.tavares@exemplo.com.br")
                ?: userDao.getUserByEmail("marina.albuquerque@exemplo.com.br")
            null -> null
        }
    }

    private fun resolveAliasEmail(email: String): String = when (email.lowercase()) {
        "admin@letssign.com" -> "admin@exemplo.com.br"
        "carlos@letssign.com" -> "rafael.tavares@exemplo.com.br"
        "mariana@letssign.com" -> "marina.albuquerque@exemplo.com.br"
        else -> email
    }

    private fun isValidEmailFormat(email: String): Boolean {
        if (email.isBlank()) return false
        val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return regex.matches(email)
    }

    companion object {
        const val INVALID_TEST_OTP_CODE = "000000"
    }
}
