package com.example.challenge_letssign.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "contact_group_members",
    primaryKeys = ["groupId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = ContactGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("groupId"),
        Index("userId")
    ]
)
data class ContactGroupMemberEntity(
    val groupId: String,
    val userId: String
)