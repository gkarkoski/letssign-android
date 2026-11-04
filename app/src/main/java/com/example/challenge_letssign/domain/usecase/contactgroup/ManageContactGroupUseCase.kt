package com.example.challenge_letssign.domain.usecase.contactgroup

import com.example.challenge_letssign.domain.repository.ContactGroupRepository
import javax.inject.Inject

class ManageContactGroupUseCase @Inject constructor(
    private val repository: ContactGroupRepository
) {
    suspend fun create(name: String, memberIds: Set<String>) =
        repository.createGroup(name, memberIds)

    suspend fun addMembers(groupId: String, memberIds: Set<String>) =
        repository.addMembers(groupId, memberIds)

    suspend fun removeMember(groupId: String, userId: String) =
        repository.removeMember(groupId, userId)

    suspend fun rename(groupId: String, newName: String) =
        repository.renameGroup(groupId, newName)

    suspend fun delete(groupId: String) =
        repository.deleteGroup(groupId)
}