package com.example.challenge_letssign.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.challenge_letssign.domain.model.PersonType
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val password: String = "123456",
    val role: UserRole,
    val personType: PersonType = PersonType.FISICA,
    val cpfCnpj: String = "",
    val birthDate: String = "",
    val groupName: String = ""
)

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    password = password,
    role = role,
    personType = personType,
    cpfCnpj = cpfCnpj,
    birthDate = birthDate,
    groupName = groupName
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    phone = phone,
    password = password,
    role = role,
    personType = personType,
    cpfCnpj = cpfCnpj,
    birthDate = birthDate,
    groupName = groupName
)
