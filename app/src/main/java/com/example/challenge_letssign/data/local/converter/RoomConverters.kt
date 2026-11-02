package com.example.challenge_letssign.data.local.converter

import androidx.room.TypeConverter
import com.example.challenge_letssign.domain.model.AdditionalAuthOption
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.PersonType
import com.example.challenge_letssign.domain.model.SignatureFieldType
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerAuthMethod
import com.example.challenge_letssign.domain.model.SignerDeliveryChannel
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.model.UserRole

class RoomConverters {

    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromPersonType(personType: PersonType): String = personType.name

    @TypeConverter
    fun toPersonType(value: String): PersonType =
        runCatching { PersonType.valueOf(value) }.getOrDefault(PersonType.FISICA)

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

    @TypeConverter
    fun fromSignerDeliveryChannel(channel: SignerDeliveryChannel): String = channel.name

    @TypeConverter
    fun toSignerDeliveryChannel(value: String): SignerDeliveryChannel =
        runCatching { SignerDeliveryChannel.valueOf(value) }.getOrDefault(SignerDeliveryChannel.EMAIL)

    @TypeConverter
    fun fromSignerAuthMethod(method: SignerAuthMethod): String = method.name

    @TypeConverter
    fun toSignerAuthMethod(value: String): SignerAuthMethod =
        runCatching { SignerAuthMethod.valueOf(value) }.getOrDefault(SignerAuthMethod.EMAIL)

    @TypeConverter
    fun fromAdditionalAuthList(options: List<AdditionalAuthOption>): String =
        options.joinToString(",") { it.name }

    @TypeConverter
    fun toAdditionalAuthList(value: String): List<AdditionalAuthOption> {
        if (value.isBlank()) return emptyList()
        return value.split(",")
            .mapNotNull { token ->
                runCatching { AdditionalAuthOption.valueOf(token.trim()) }.getOrNull()
            }
    }

    @TypeConverter
    fun fromSignatureFieldType(fieldType: SignatureFieldType): String = fieldType.name

    @TypeConverter
    fun toSignatureFieldType(value: String): SignatureFieldType =
        runCatching { SignatureFieldType.valueOf(value) }.getOrDefault(SignatureFieldType.SIGNATURE)
}
