package com.example.challenge_letssign.data.local

import com.example.challenge_letssign.data.local.converter.RoomConverters
import com.example.challenge_letssign.data.local.entity.DocumentEntity
import com.example.challenge_letssign.data.local.entity.DocumentSignerCrossRef
import com.example.challenge_letssign.data.local.entity.DocumentSignerWithUserEntity
import com.example.challenge_letssign.data.local.entity.SignatureFieldEntity
import com.example.challenge_letssign.data.local.entity.UserEntity
import com.example.challenge_letssign.data.local.entity.toDomain
import com.example.challenge_letssign.data.local.entity.toEntity
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.utils.AppResult
import com.example.challenge_letssign.utils.runCatchingResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersAndConvertersTest {

    private val converters = RoomConverters()

    @Test
    fun `RoomConverters should convert all enums to String and back`() {
        UserRole.entries.forEach { role ->
            val raw = converters.fromUserRole(role)
            assertEquals(role, converters.toUserRole(raw))
        }

        DocumentStatus.entries.forEach { status ->
            val raw = converters.fromDocumentStatus(status)
            assertEquals(status, converters.toDocumentStatus(raw))
        }

        SignerRoleClass.entries.forEach { roleClass ->
            val raw = converters.fromSignerRoleClass(roleClass)
            assertEquals(roleClass, converters.toSignerRoleClass(raw))
        }

        SignatureStatus.entries.forEach { sigStatus ->
            val raw = converters.fromSignatureStatus(sigStatus)
            assertEquals(sigStatus, converters.toSignatureStatus(raw))
        }
    }

    @Test
    fun `Entity and Domain mappers should preserve all fields`() {
        val user = User(
            id = "u-1",
            name = "Teste",
            email = "teste@letssign.com",
            phone = "11999999999",
            password = "secret",
            role = UserRole.SIGNER
        )
        assertEquals(user, user.toEntity().toDomain())

        val doc = Document(
            id = "d-1",
            title = "Contrato Social",
            fileName = "contrato.pdf",
            status = DocumentStatus.DRAFT,
            createdAt = 1700000000000L,
            ownerAdminId = "admin-1"
        )
        assertEquals(doc, doc.toEntity().toDomain())

        val signerRef = DocumentSigner(
            documentId = "d-1",
            signerId = "u-1",
            roleClass = SignerRoleClass.CONTRATANTE,
            status = SignatureStatus.SIGNED,
            signedAt = 1700000050000L,
            signatureIp = "192.168.1.10"
        )
        assertEquals(signerRef, signerRef.toEntity().toDomain())

        val relationEntity = DocumentSignerWithUserEntity(
            crossRef = signerRef.toEntity(),
            signer = user.toEntity()
        )
        val relationDomain = relationEntity.toDomain()
        assertEquals(signerRef, relationDomain.documentSigner)
        assertEquals(user, relationDomain.signer)

        val field = SignatureField(
            id = "f-1",
            documentId = "d-1",
            signerId = "u-1",
            pageNumber = 0,
            relativeX = 0.15f,
            relativeY = 0.75f,
            relativeWidth = 0.30f,
            relativeHeight = 0.08f
        )
        assertEquals(field, field.toEntity().toDomain())
    }

    @Test
    fun `AppResult and runCatchingResult should wrap success and exception properly`() {
        val successResult = runCatchingResult { 42 }
        assertTrue(successResult.isSuccess)
        assertFalse(successResult.isError)
        assertEquals(42, successResult.getOrNull())

        val errorResult = runCatchingResult("Falha padrão") {
            throw IllegalStateException("Erro customizado")
        }
        assertTrue(errorResult.isError)
        assertFalse(errorResult.isSuccess)
        assertNull(errorResult.getOrNull())
        assertEquals("Erro customizado", (errorResult as AppResult.Error).message)
    }
}
