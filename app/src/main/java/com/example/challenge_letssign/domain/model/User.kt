package com.example.challenge_letssign.domain.model

enum class UserRole {
    ADMIN,
    SIGNER
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: UserRole
)
