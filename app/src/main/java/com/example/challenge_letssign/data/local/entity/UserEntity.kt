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
    val phone: String,
    val password: String,
    val role: UserRole,
    val personType: PersonType = PersonType.FISICA,
    val cpfCnpj: String = "",
    val nicknameOrTradeName: String = "",
    val contactType: String = "",
    val rg: String = "",
    val issuingAgency: String = "",
    val documentUf: String = "",
    val nationality: String = "",
    val profession: String = "",
    val birthDate: String = "",
    val maritalStatus: String = "",
    val stateRegistration: String = "",
    val municipalRegistration: String = "",
    val nire: String = "",
    val phone2: String = "",
    val zipCode: String = "",
    val street: String = "",
    val addressNumber: String = "",
    val complement: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val state: String = "",
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
    nicknameOrTradeName = nicknameOrTradeName,
    contactType = contactType,
    rg = rg,
    issuingAgency = issuingAgency,
    documentUf = documentUf,
    nationality = nationality,
    profession = profession,
    birthDate = birthDate,
    maritalStatus = maritalStatus,
    stateRegistration = stateRegistration,
    municipalRegistration = municipalRegistration,
    nire = nire,
    phone2 = phone2,
    zipCode = zipCode,
    street = street,
    addressNumber = addressNumber,
    complement = complement,
    neighborhood = neighborhood,
    city = city,
    state = state,
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
    nicknameOrTradeName = nicknameOrTradeName,
    contactType = contactType,
    rg = rg,
    issuingAgency = issuingAgency,
    documentUf = documentUf,
    nationality = nationality,
    profession = profession,
    birthDate = birthDate,
    maritalStatus = maritalStatus,
    stateRegistration = stateRegistration,
    municipalRegistration = municipalRegistration,
    nire = nire,
    phone2 = phone2,
    zipCode = zipCode,
    street = street,
    addressNumber = addressNumber,
    complement = complement,
    neighborhood = neighborhood,
    city = city,
    state = state,
    groupName = groupName
)
