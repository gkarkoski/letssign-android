package com.example.challenge_letssign.data.repository.local

import com.example.challenge_letssign.data.di.IoDispatcher
import com.example.challenge_letssign.data.local.dao.ContactGroupDao
import com.example.challenge_letssign.data.local.entity.ContactGroupEntity
import com.example.challenge_letssign.data.local.entity.ContactGroupMemberEntity
import com.example.challenge_letssign.data.local.entity.toDomain
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.repository.ContactGroup
import com.example.challenge_letssign.domain.repository.ContactGroupRepository
import com.example.challenge_letssign.utils.AppResult
import com.example.challenge_letssign.utils.runCatchingResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalContactGroupRepository @Inject constructor(
    private val contactGroupDao: ContactGroupDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ContactGroupRepository {

    override fun observeGroups(): Flow<List<ContactGroup>> =
        contactGroupDao.observeGroups()
            .flatMapLatest { groupEntities ->
                if (groupEntities.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val memberFlows = groupEntities.map { group ->
                        contactGroupDao.observeGroupMembers(group.id)
                    }

                    combine(memberFlows) { membersByGroup ->
                        groupEntities.mapIndexed { index, groupEntity ->
                            ContactGroup(
                                id = groupEntity.id,
                                name = groupEntity.name,
                                members = membersByGroup[index].map { entity ->
                                    entity.toDomain()
                                }
                            )
                        }
                    }
                }
            }

    override fun observeGroupMembers(groupId: String): Flow<List<User>> =
        contactGroupDao.observeGroupMembers(groupId)
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }

    override suspend fun createGroup(
        name: String,
        memberIds: Set<String>
    ): AppResult<String> = withContext(ioDispatcher) {
        runCatchingResult("Erro ao criar grupo de contatos.") {
            val trimmedName = name.trim()

            if (trimmedName.isBlank()) {
                throw IllegalArgumentException("Informe o nome do grupo.")
            }

            if (memberIds.isEmpty()) {
                throw IllegalArgumentException(
                    "Selecione ao menos 1 contato para o grupo."
                )
            }

            val groupId = UUID.randomUUID().toString()

            contactGroupDao.insertGroup(
                ContactGroupEntity(
                    id = groupId,
                    name = trimmedName
                )
            )

            contactGroupDao.insertMembers(
                memberIds.map { userId ->
                    ContactGroupMemberEntity(
                        groupId = groupId,
                        userId = userId
                    )
                }
            )

            groupId
        }
    }

    override suspend fun addMembers(
        groupId: String,
        memberIds: Set<String>
    ): AppResult<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Erro ao adicionar integrantes ao grupo.") {
            if (contactGroupDao.getGroupById(groupId) == null) {
                throw NoSuchElementException("Grupo não encontrado.")
            }

            if (memberIds.isEmpty()) {
                throw IllegalArgumentException(
                    "Selecione ao menos 1 contato para adicionar."
                )
            }

            contactGroupDao.insertMembers(
                memberIds.map { userId ->
                    ContactGroupMemberEntity(
                        groupId = groupId,
                        userId = userId
                    )
                }
            )
        }
    }

    override suspend fun removeMember(
        groupId: String,
        userId: String
    ): AppResult<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Erro ao remover integrante do grupo.") {
            contactGroupDao.removeMember(
                groupId = groupId,
                userId = userId
            )
        }
    }

    override suspend fun renameGroup(
        groupId: String,
        newName: String
    ): AppResult<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Erro ao renomear grupo.") {
            val trimmedName = newName.trim()

            if (trimmedName.isBlank()) {
                throw IllegalArgumentException("Informe o nome do grupo.")
            }

            if (contactGroupDao.getGroupById(groupId) == null) {
                throw NoSuchElementException("Grupo não encontrado.")
            }

            contactGroupDao.renameGroup(
                groupId = groupId,
                newName = trimmedName
            )
        }
    }

    override suspend fun deleteGroup(
        groupId: String
    ): AppResult<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Erro ao excluir grupo.") {
            if (contactGroupDao.getGroupById(groupId) == null) {
                throw NoSuchElementException("Grupo não encontrado.")
            }

            contactGroupDao.deleteGroup(groupId)
        }
    }
}