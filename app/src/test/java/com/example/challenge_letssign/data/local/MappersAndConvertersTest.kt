package com.example.challenge_letssign.data.local

import com.example.challenge_letssign.data.local.converter.RoomConverters
import com.example.challenge_letssign.data.local.entity.DocumentSignerWithUserEntity
import com.example.challenge_letssign.data.local.entity.toDomain
import com.example.challenge_letssign.data.local.entity.toEntity
import com.example.challenge_letssign.data.local.seed.DatabaseSeeder
import com.example.challenge_letssign.domain.model.AdditionalAuthOption
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.PersonType
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureFieldType
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerAuthMethod
import com.example.challenge_letssign.domain.model.SignerDeliveryChannel
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.utils.AppResult
import com.example.challenge_letssign.utils.runCatchingResult
import com.tom_roush.pdfbox.pdmodel.PDDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MappersAndConvertersTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val converters = RoomConverters()

    @Test
    fun `RoomConverters should convert all enums and lists to String and back`() {
        UserRole.entries.forEach { role ->
            val raw = converters.fromUserRole(role)
            assertEquals(role, converters.toUserRole(raw))
        }

        PersonType.entries.forEach { personType ->
            val raw = converters.fromPersonType(personType)
            assertEquals(personType, converters.toPersonType(raw))
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

        SignerDeliveryChannel.entries.forEach { channel ->
            val raw = converters.fromSignerDeliveryChannel(channel)
            assertEquals(channel, converters.toSignerDeliveryChannel(raw))
        }

        SignerAuthMethod.entries.forEach { authMethod ->
            val raw = converters.fromSignerAuthMethod(authMethod)
            assertEquals(authMethod, converters.toSignerAuthMethod(raw))
        }

        SignatureFieldType.entries.forEach { fieldType ->
            val raw = converters.fromSignatureFieldType(fieldType)
            assertEquals(fieldType, converters.toSignatureFieldType(raw))
        }

        val authOptions = listOf(
            AdditionalAuthOption.PHOTO_ID_DOCUMENT,
            AdditionalAuthOption.SELFIE_WITH_BIOMETRICS
        )
        val rawOptions = converters.fromAdditionalAuthList(authOptions)
        assertEquals(authOptions, converters.toAdditionalAuthList(rawOptions))
        assertTrue(converters.toAdditionalAuthList("").isEmpty())
    }

    @Test
    fun `Entity and Domain mappers should preserve all Figma v3 fields`() {
        val user = User(
            id = "u-1",
            name = "Marina Albuquerque",
            email = "marina.albuquerque@exemplo.com.br",
            phone = "(31) 98341-2200",
            password = "secret",
            role = UserRole.SIGNER,
            personType = PersonType.FISICA,
            cpfCnpj = "123.456.789-09",
            nicknameOrTradeName = "Mari",
            contactType = "Contratante",
            rg = "MG-12.345.678",
            issuingAgency = "SSP",
            documentUf = "MG",
            nationality = "Brasileira",
            profession = "Advogada",
            birthDate = "14/03/1988",
            maritalStatus = "Casada",
            zipCode = "30140-071",
            street = "Av. Afonso Pena",
            addressNumber = "1500",
            neighborhood = "Centro",
            city = "Belo Horizonte",
            state = "Minas Gerais",
            groupName = "Jurídico"
        )
        assertEquals(user, user.toEntity().toDomain())

        val doc = Document(
            id = "d-1",
            title = "Contrato de serviços",
            fileName = "contrato-servicos.pdf",
            originalDisplayName = "contrato-servicos.pdf",
            status = DocumentStatus.PENDING,
            createdAt = 1700000000000L,
            sentAt = 1700000010000L,
            deadlineAt = 1700500000000L,
            pageCount = 6,
            fileSizeBytes = 1_887_436L,
            ownerAdminId = "admin-1",
            inviteMessage = Document.DEFAULT_INVITE_MESSAGE
        )
        assertEquals(doc, doc.toEntity().toDomain())

        val signerRef = DocumentSigner(
            documentId = "d-1",
            signerId = "u-1",
            roleClass = SignerRoleClass.CONTRATANTE,
            status = SignatureStatus.SIGNED,
            signedAt = 1700000050000L,
            signatureIp = "192.168.1.10",
            deliveryChannel = SignerDeliveryChannel.EMAIL,
            authMethod = SignerAuthMethod.EMAIL,
            additionalAuths = listOf(
                AdditionalAuthOption.PHOTO_ID_DOCUMENT,
                AdditionalAuthOption.SELFIE_WITH_BIOMETRICS
            ),
            language = "Português",
            colorIndex = 0
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
            pageNumber = 1,
            relativeX = 0.15f,
            relativeY = 0.75f,
            relativeWidth = 0.30f,
            relativeHeight = 0.08f,
            fieldType = SignatureFieldType.INITIALS,
            isAiSuggested = true,
            aiContextHint = "perto de \"CONTRATANTE\""
        )
        assertEquals(field, field.toEntity().toDomain())
    }

    @Test
    fun `DatabaseSeeder ensureSeedPdfFileExists generates valid 6-page PDF via PDFBox`() {
        val dir = tempFolder.newFolder("seed_pdfs")
        val pdfFile = DatabaseSeeder.ensureSeedPdfFileExists(dir)

        assertTrue(pdfFile.exists())
        assertTrue(pdfFile.length() > 0L)

        PDDocument.load(pdfFile).use { doc ->
            assertEquals(6, doc.numberOfPages)
        }
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
