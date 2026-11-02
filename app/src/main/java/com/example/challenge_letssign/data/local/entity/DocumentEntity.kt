package com.example.challenge_letssign.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentStatus

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerAdminId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ownerAdminId"])
    ]
)
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val fileName: String,
    val status: DocumentStatus,
    val createdAt: Long,
    val ownerAdminId: String,
    val sentAt: Long? = null,
    val inviteMessage: String? = null,
    val pageCount: Int = 1,
    val fileSizeBytes: Long = 0L,
    val deadlineAt: Long? = null,
    val originalDisplayName: String = ""
)

fun DocumentEntity.toDomain(): Document = Document(
    id = id,
    title = title,
    fileName = fileName,
    status = status,
    createdAt = createdAt,
    ownerAdminId = ownerAdminId,
    sentAt = sentAt,
    inviteMessage = inviteMessage,
    pageCount = pageCount,
    fileSizeBytes = fileSizeBytes,
    deadlineAt = deadlineAt,
    originalDisplayName = originalDisplayName
)

fun Document.toEntity(): DocumentEntity = DocumentEntity(
    id = id,
    title = title,
    fileName = fileName,
    status = status,
    createdAt = createdAt,
    ownerAdminId = ownerAdminId,
    sentAt = sentAt,
    inviteMessage = inviteMessage,
    pageCount = pageCount,
    fileSizeBytes = fileSizeBytes,
    deadlineAt = deadlineAt,
    originalDisplayName = originalDisplayName
)
