package com.example.challenge_letssign.data.local.seed

import com.example.challenge_letssign.data.local.dao.UserDao
import com.example.challenge_letssign.data.local.entity.UserEntity
import com.example.challenge_letssign.domain.model.UserRole

object DatabaseSeeder {

    const val MOCK_ADMIN_ID = "00000000-0000-0000-0000-000000000001"
    const val MOCK_SIGNER_1_ID = "00000000-0000-0000-0000-000000000002"
    const val MOCK_SIGNER_2_ID = "00000000-0000-0000-0000-000000000003"

    val initialUsers: List<UserEntity> = listOf(
        UserEntity(
            id = MOCK_ADMIN_ID,
            name = "Administrador LetsSign",
            email = "admin@letssign.com",
            phone = "(11) 99999-0000",
            password = "123456",
            role = UserRole.ADMIN
        ),
        UserEntity(
            id = MOCK_SIGNER_1_ID,
            name = "Carlos Silva (Signatário 1)",
            email = "carlos@letssign.com",
            phone = "(11) 98888-1111",
            password = "123456",
            role = UserRole.SIGNER
        ),
        UserEntity(
            id = MOCK_SIGNER_2_ID,
            name = "Mariana Souza (Signatária 2)",
            email = "mariana@letssign.com",
            phone = "(11) 97777-2222",
            password = "123456",
            role = UserRole.SIGNER
        )
    )

    suspend fun seedIfNeeded(userDao: UserDao) {
        if (userDao.getUserCount() == 0) {
            userDao.insertUsersIgnore(initialUsers)
        }
    }
}
