package com.example.challenge_letssign.domain.usecase.contactgroup

import com.example.challenge_letssign.domain.repository.ContactGroupRepository
import javax.inject.Inject

class ObserveContactGroupMembersUseCase @Inject constructor(
    private val repository: ContactGroupRepository
) {
    operator fun invoke(groupId: String) =
        repository.observeGroupMembers(groupId)
}