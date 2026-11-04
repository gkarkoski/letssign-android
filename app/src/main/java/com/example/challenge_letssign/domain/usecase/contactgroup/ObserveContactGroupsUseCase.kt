package com.example.challenge_letssign.domain.usecase.contactgroup

import com.example.challenge_letssign.domain.repository.ContactGroupRepository
import javax.inject.Inject

class ObserveContactGroupsUseCase @Inject constructor(
    private val repository: ContactGroupRepository
) {
    operator fun invoke() = repository.observeGroups()
}