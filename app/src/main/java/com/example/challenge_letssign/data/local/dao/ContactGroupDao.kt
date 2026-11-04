package com.example.challenge_letssign.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.challenge_letssign.data.local.entity.ContactGroupEntity
import com.example.challenge_letssign.data.local.entity.ContactGroupMemberEntity
import com.example.challenge_letssign.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactGroupDao {

    @Query("SELECT * FROM contact_groups ORDER BY name COLLATE NOCASE")
    fun observeGroups(): Flow<List<ContactGroupEntity>>

    @Query("SELECT * FROM contact_groups WHERE id = :groupId LIMIT 1")
    suspend fun getGroupById(groupId: String): ContactGroupEntity?

    @Query("""
        SELECT users.* FROM users
        INNER JOIN contact_group_members
        ON users.id = contact_group_members.userId
        WHERE contact_group_members.groupId = :groupId
        ORDER BY users.name COLLATE NOCASE
    """)
    fun observeGroupMembers(groupId: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGroup(group: ContactGroupEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMembers(members: List<ContactGroupMemberEntity>)

    @Query("""
        DELETE FROM contact_group_members
        WHERE groupId = :groupId AND userId = :userId
    """)
    suspend fun removeMember(groupId: String, userId: String)

    @Query("DELETE FROM contact_group_members WHERE groupId = :groupId")
    suspend fun clearMembers(groupId: String)

    @Query("UPDATE contact_groups SET name = :newName WHERE id = :groupId")
    suspend fun renameGroup(groupId: String, newName: String)

    @Query("DELETE FROM contact_groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: String)

    @Transaction
    suspend fun replaceMembers(
        groupId: String,
        members: List<ContactGroupMemberEntity>
    ) {
        clearMembers(groupId)
        insertMembers(members)
    }
}