package com.example.challenge_letssign.domain.model

enum class UserRole {
    ADMIN,
    SIGNER
}

enum class PersonType(val displayLabel: String) {
    FISICA("Física"),
    JURIDICA("Jurídica")
}

data class User(
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
