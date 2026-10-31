package com.example.challenge_letssign.data.repository.local

import com.example.challenge_letssign.data.local.dao.UserDao
import com.example.challenge_letssign.data.local.entity.UserEntity
import com.example.challenge_letssign.data.local.seed.DatabaseSeeder
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
    fun `DatabaseSeeder should seed 1 Admin and 2 Signers without duplicating`() = runTest(testDispatcher) {
        DatabaseSeeder.seedIfNeeded(fakeUserDao)
        assertEquals(3, fakeUserDao.getUserCount())

        // Calling seedIfNeeded again should not duplicate users
        DatabaseSeeder.seedIfNeeded(fakeUserDao)
        assertEquals(3, fakeUserDao.getUserCount())

        val signers = repository.observeSigners().first()
        assertEquals(2, signers.size)

        val admins = repository.observeUsersByRole(UserRole.ADMIN).first()
        assertEquals(1, admins.size)
        assertEquals("admin@letssign.com", admins.first().email)
    }

    @Test
    fun `login should return Success for valid credentials and Error for invalid`() = runTest(testDispatcher) {
        DatabaseSeeder.seedIfNeeded(fakeUserDao)

        val validResult = repository.login("admin@letssign.com", "123456")
        assertTrue(validResult is AppResult.Success)
        assertEquals(UserRole.ADMIN, validResult.getOrNull()?.role)

        val invalidResult = repository.login("admin@letssign.com", "wrong_pass")
        assertTrue(invalidResult is AppResult.Error)
        assertEquals("E-mail ou senha inválidos.", (invalidResult as AppResult.Error).message)
    }

    @Test
    fun `createUser should reject duplicate email case-insensitively`() = runTest(testDispatcher) {
        DatabaseSeeder.seedIfNeeded(fakeUserDao)

        val duplicateUser = User(
            id = "new-id",
            name = "Outro Carlos",
            email = "CARLOS@letssign.com",
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
            usersFlow.value.firstOrNull { it.email == email && it.password == password }

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
