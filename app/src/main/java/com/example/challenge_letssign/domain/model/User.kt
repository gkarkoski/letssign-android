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
    val phone: String = "",
    val password: String = "123456",
    val role: UserRole,
    val personType: PersonType = PersonType.FISICA,
    val cpfCnpj: String = "",
    val birthDate: String = "",
    val groupName: String = ""
)
