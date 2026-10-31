package com.example.challenge_letssign.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Relation
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerRoleClass

@Entity(
    tableName = "document_signer_cross_ref",
    primaryKeys = ["documentId", "signerId"],
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
data class DocumentSignerCrossRef(
    val documentId: String,
    val signerId: String,
    val roleClass: SignerRoleClass,
    val status: SignatureStatus,
    val signedAt: Long? = null,
    val signatureIp: String? = null
)

data class DocumentSignerWithUserEntity(
    @Embedded
    val crossRef: DocumentSignerCrossRef,
    @Relation(
        parentColumn = "signerId",
        entityColumn = "id"
    )
    val signer: UserEntity
)

fun DocumentSignerCrossRef.toDomain(): DocumentSigner = DocumentSigner(
    documentId = documentId,
    signerId = signerId,
    roleClass = roleClass,
    status = status,
    signedAt = signedAt,
    signatureIp = signatureIp
)

fun DocumentSigner.toEntity(): DocumentSignerCrossRef = DocumentSignerCrossRef(
    documentId = documentId,
    signerId = signerId,
    roleClass = roleClass,
    status = status,
    signedAt = signedAt,
    signatureIp = signatureIp
)

fun DocumentSignerWithUserEntity.toDomain(): DocumentSignerWithUser = DocumentSignerWithUser(
    documentSigner = crossRef.toDomain(),
    signer = signer.toDomain()
)
