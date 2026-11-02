package com.example.challenge_letssign.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureFieldType

@Entity(
    tableName = "signature_fields",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["signerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["documentId"]),
        Index(value = ["signerId"])
    ]
)
data class SignatureFieldEntity(
    @PrimaryKey
    val id: String,
    val documentId: String,
    val signerId: String,
    val pageNumber: Int,
    val relativeX: Float,
    val relativeY: Float,
    val relativeWidth: Float,
    val relativeHeight: Float,
    val fieldType: SignatureFieldType = SignatureFieldType.SIGNATURE,
    val isAiSuggested: Boolean = false,
    val aiContextHint: String? = null
)

fun SignatureFieldEntity.toDomain(): SignatureField = SignatureField(
    id = id,
    documentId = documentId,
    signerId = signerId,
    pageNumber = pageNumber,
    relativeX = relativeX,
    relativeY = relativeY,
    relativeWidth = relativeWidth,
    relativeHeight = relativeHeight,
    fieldType = fieldType,
    isAiSuggested = isAiSuggested,
    aiContextHint = aiContextHint
)

fun SignatureField.toEntity(): SignatureFieldEntity = SignatureFieldEntity(
    id = id,
    documentId = documentId,
    signerId = signerId,
    pageNumber = pageNumber,
    relativeX = relativeX,
    relativeY = relativeY,
    relativeWidth = relativeWidth,
    relativeHeight = relativeHeight,
    fieldType = fieldType,
    isAiSuggested = isAiSuggested,
    aiContextHint = aiContextHint
)
