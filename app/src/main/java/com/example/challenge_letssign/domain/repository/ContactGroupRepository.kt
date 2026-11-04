package com.example.challenge_letssign.domain.repository

import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.utils.AppResult
import kotlinx.coroutines.flow.Flow

data class ContactGroup(
    val id: String,
    val name: String,
    val members: List<User>
)

interface ContactGroupRepository {
    fun observeGroups(): Flow<List<ContactGroup>>
    fun observeGroupMembers(groupId: String): Flow<List<User>>
    suspend fun createGroup(name: String, memberIds: Set<String>): AppResult<String>
    suspend fun addMembers(groupId: String, memberIds: Set<String>): AppResult<Unit>
    suspend fun removeMember(groupId: String, userId: String): AppResult<Unit>
    suspend fun renameGroup(groupId: String, newName: String): AppResult<Unit>
    suspend fun deleteGroup(groupId: String): AppResult<Unit>
}