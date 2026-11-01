package com.example.challenge_letssign.data.repository.local

import com.example.challenge_letssign.data.local.dao.UserDao
import com.example.challenge_letssign.data.local.entity.UserEntity
import com.example.challenge_letssign.data.local.seed.DatabaseSeeder
import com.example.challenge_letssign.domain.model.PersonType
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.utils.AppResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocalUserRepositoryTest {

    private lateinit var fakeUserDao: FakeUserDao
    private lateinit var repository: LocalUserRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        fakeUserDao = FakeUserDao()
        repository = LocalUserRepository(
            userDao = fakeUserDao,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `DatabaseSeeder should seed 1 Admin and 6 Figma Signers (Fisica and Juridica) without duplicating`() = runTest(testDispatcher) {
        DatabaseSeeder.seedIfNeeded(fakeUserDao)
        assertEquals(7, fakeUserDao.getUserCount())

        // Calling seedIfNeeded again should not duplicate users
        DatabaseSeeder.seedIfNeeded(fakeUserDao)
        assertEquals(7, fakeUserDao.getUserCount())

        val signers = repository.observeSigners().first()
        assertEquals(6, signers.size)
        assertTrue(signers.any { it.name == "Marina Albuquerque" && it.groupName == "Jurídico" })
        assertTrue(signers.any { it.name == "Nova Soluções Ltda." && it.personType == PersonType.JURIDICA })

        val admins = repository.observeUsersByRole(UserRole.ADMIN).first()
        assertEquals(1, admins.size)
        assertEquals("admin@exemplo.com.br", admins.first().email)
    }

    @Test
    fun `login and OTP 6-digit flow should support both Figma emails and legacy alias emails`() = runTest(testDispatcher) {
        DatabaseSeeder.seedIfNeeded(fakeUserDao)

        // Legacy alias login
        val aliasLogin = repository.login("admin@letssign.com", "123456")
        assertTrue(aliasLogin is AppResult.Success)
        assertEquals(UserRole.ADMIN, aliasLogin.getOrNull()?.role)

        // Figma OTP request & verification
        val requestAdminOtp = repository.requestLoginCode("admin@exemplo.com.br", UserRole.ADMIN)
        assertTrue(requestAdminOtp.isSuccess)

        val verifyAdminOtp = repository.verifyLoginCode("admin@exemplo.com.br", "482910", UserRole.ADMIN)
        assertTrue(verifyAdminOtp.isSuccess)
        assertEquals(UserRole.ADMIN, verifyAdminOtp.getOrNull()?.role)

        // Invalid OTP code (000000 or < 6 digits) triggers Login Código inválido error
        val invalidOtp = repository.verifyLoginCode("admin@exemplo.com.br", "000000", UserRole.ADMIN)
        assertTrue(invalidOtp.isError)
        assertEquals(
            "Código inválido ou expirado. Peça um novo código para continuar.",
            (invalidOtp as AppResult.Error).message
        )
    }

    @Test
    fun `createUser should reject duplicate email case-insensitively`() = runTest(testDispatcher) {
        DatabaseSeeder.seedIfNeeded(fakeUserDao)

        val duplicateUser = User(
            id = "new-id",
            name = "Outra Marina",
            email = "MARINA.ALBUQUERQUE@exemplo.com.br",
            phone = "11900000000",
            password = "123",
            role = UserRole.SIGNER
        )

        val result = repository.createUser(duplicateUser)
        assertTrue(result is AppResult.Error)
        assertEquals(
            "Já existe um usuário cadastrado com este e-mail.",
            (result as AppResult.Error).message
        )
    }

    @Test
    fun `createUser and updateUser should persist when email is unique`() = runTest(testDispatcher) {
        val newUser = User(
            id = "signer-10",
            name = "Ana Costa",
            email = "ana@letssign.com",
            phone = "(11) 95555-4444",
            password = "123456",
            role = UserRole.SIGNER
        )

        val createResult = repository.createUser(newUser)
        assertTrue(createResult.isSuccess)

        val updatedUser = newUser.copy(name = "Ana Costa Atualizada")
        val updateResult = repository.updateUser(updatedUser)
        assertTrue(updateResult.isSuccess)

        val fetched = repository.getUserById("signer-10")
        assertEquals("Ana Costa Atualizada", fetched.getOrNull()?.name)
    }

    private class FakeUserDao : UserDao {
        private val usersFlow = MutableStateFlow<List<UserEntity>>(emptyList())

        override suspend fun getUserByCredentials(email: String, password: String): UserEntity? =
            usersFlow.value.firstOrNull { it.email.equals(email, ignoreCase = true) && it.password == password }

        override suspend fun getUserById(userId: String): UserEntity? =
            usersFlow.value.firstOrNull { it.id == userId }

        override suspend fun getUserByEmail(email: String): UserEntity? =
            usersFlow.value.firstOrNull { it.email.equals(email, ignoreCase = true) }

        override fun observeUsersByRole(role: UserRole): Flow<List<UserEntity>> =
            usersFlow.map { list -> list.filter { it.role == role }.sortedBy { it.name } }

        override suspend fun getUserCount(): Int = usersFlow.value.size

        override suspend fun insertUser(user: UserEntity) {
            usersFlow.value = usersFlow.value + user
        }

        override suspend fun insertUsersIgnore(users: List<UserEntity>) {
            val existingIds = usersFlow.value.map { it.id }.toSet()
            val existingEmails = usersFlow.value.map { it.email.lowercase() }.toSet()
            val toAdd = users.filter {
                it.id !in existingIds && it.email.lowercase() !in existingEmails
            }
            usersFlow.value = usersFlow.value + toAdd
        }

        override suspend fun updateUser(user: UserEntity) {
            usersFlow.value = usersFlow.value.map { if (it.id == user.id) user else it }
        }

        override suspend fun deleteUserById(userId: String) {
            usersFlow.value = usersFlow.value.filterNot { it.id == userId }
        }
    }
}
