package com.example.challenge_letssign.data.local.converter

import androidx.room.TypeConverter
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.model.UserRole

class RoomConverters {

    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromDocumentStatus(status: DocumentStatus): String = status.name

    @TypeConverter
    fun toDocumentStatus(value: String): DocumentStatus = DocumentStatus.valueOf(value)

    @TypeConverter
    fun fromSignerRoleClass(roleClass: SignerRoleClass): String = roleClass.name

    @TypeConverter
    fun toSignerRoleClass(value: String): SignerRoleClass = SignerRoleClass.valueOf(value)

    @TypeConverter
    fun fromSignatureStatus(status: SignatureStatus): String = status.name

    @TypeConverter
    fun toSignatureStatus(value: String): SignatureStatus = SignatureStatus.valueOf(value)
}
