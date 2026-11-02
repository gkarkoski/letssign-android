package com.example.challenge_letssign.data.local.seed

import com.example.challenge_letssign.data.local.dao.DocumentDao
import com.example.challenge_letssign.data.local.dao.SignatureFieldDao
import com.example.challenge_letssign.data.local.dao.UserDao
import com.example.challenge_letssign.data.local.entity.DocumentEntity
import com.example.challenge_letssign.data.local.entity.DocumentSignerCrossRef
import com.example.challenge_letssign.data.local.entity.SignatureFieldEntity
import com.example.challenge_letssign.data.local.entity.UserEntity
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.PersonType
import com.example.challenge_letssign.domain.model.SignatureFieldType
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerAuthMethod
import com.example.challenge_letssign.domain.model.SignerDeliveryChannel
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.model.UserRole
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import java.io.File
import java.util.Calendar
import java.util.TimeZone

object DatabaseSeeder {

    const val MOCK_ADMIN_ID = "00000000-0000-0000-0000-000000000001"
    const val MOCK_SIGNER_1_ID = "00000000-0000-0000-0000-000000000002" // Marina Albuquerque
    const val MOCK_SIGNER_2_ID = "00000000-0000-0000-0000-000000000003" // Rafael Tavares
    const val MOCK_SIGNER_3_ID = "00000000-0000-0000-0000-000000000004" // Beatriz Nogueira
    const val MOCK_SIGNER_4_ID = "00000000-0000-0000-0000-000000000005" // Camila Duarte
    const val MOCK_SIGNER_5_ID = "00000000-0000-0000-0000-000000000006" // Lucas Ferraz
    const val MOCK_SIGNER_6_ID = "00000000-0000-0000-0000-000000000007" // Nova Soluções Ltda.

    const val DOC_CONTRATO_SERVICOS_ID = "doc-0000-0000-0000-000000000001"
    const val DOC_ADITIVO_CONTRATUAL_ID = "doc-0000-0000-0000-000000000002"
    const val DOC_PROPOSTA_COMERCIAL_ID = "doc-0000-0000-0000-000000000003"
    const val DOC_TERMO_ADESAO_ID = "doc-0000-0000-0000-000000000004"
    const val DOC_AUTORIZACAO_IMAGEM_ID = "doc-0000-0000-0000-000000000005"

    const val SEED_PDF_FILE_NAME = "contrato-servicos.pdf"

    val initialUsers: List<UserEntity> = listOf(
        UserEntity(
            id = MOCK_ADMIN_ID,
            name = "Administrador LetsSign",
            email = "admin@exemplo.com.br",
            phone = "(11) 99999-0000",
            password = "123456",
            role = UserRole.ADMIN,
            personType = PersonType.FISICA,
            cpfCnpj = "111.222.333-44",
            birthDate = "10/01/1985",
            groupName = "Administração"
        ),
        UserEntity(
            id = MOCK_SIGNER_3_ID,
            name = "Beatriz Nogueira",
            email = "beatriz.nogueira@exemplo.com.br",
            phone = "(11) 97777-2222",
            password = "123456",
            role = UserRole.SIGNER,
            personType = PersonType.FISICA,
            cpfCnpj = "456.789.123-44",
            birthDate = "22/07/1993",
            groupName = "Jurídico"
        ),
        UserEntity(
            id = MOCK_SIGNER_4_ID,
            name = "Camila Duarte",
            email = "camila.duarte@exemplo.com.br",
            phone = "(21) 96666-3333",
            password = "123456",
            role = UserRole.SIGNER,
            personType = PersonType.FISICA,
            cpfCnpj = "654.321.789-22",
            birthDate = "09/11/1990",
            groupName = "Comercial"
        ),
        UserEntity(
            id = MOCK_SIGNER_5_ID,
            name = "Lucas Ferraz",
            email = "lucas.ferraz@exemplo.com.br",
            phone = "(41) 95555-4444",
            password = "123456",
            role = UserRole.SIGNER,
            personType = PersonType.FISICA,
            cpfCnpj = "789.123.456-55",
            birthDate = "18/05/1987",
            groupName = "Financeiro"
        ),
        UserEntity(
            id = MOCK_SIGNER_1_ID,
            name = "Marina Albuquerque",
            email = "marina.albuquerque@exemplo.com.br",
            phone = "(31) 98341-2200",
            password = "123456",
            role = UserRole.SIGNER,
            personType = PersonType.FISICA,
            cpfCnpj = "123.456.789-09",
            birthDate = "14/03/1988",
            groupName = "Jurídico"
        ),
        UserEntity(
            id = MOCK_SIGNER_6_ID,
            name = "Nova Soluções Ltda.",
            email = "contato@novasolucoes.com.br",
            phone = "(11) 3000-5000",
            password = "123456",
            role = UserRole.SIGNER,
            personType = PersonType.JURIDICA,
            cpfCnpj = "12.345.678/0001-90",
            groupName = "Comercial"
        ),
        UserEntity(
            id = MOCK_SIGNER_2_ID,
            name = "Rafael Tavares",
            email = "rafael.tavares@exemplo.com.br",
            phone = "(11) 98888-1111",
            password = "123456",
            role = UserRole.SIGNER,
            personType = PersonType.FISICA,
            cpfCnpj = "321.654.987-10",
            birthDate = "03/09/1985",
            groupName = "Comercial"
        )
    )

    private fun timestampOf(
        year: Int,
        monthOneBased: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): Long {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.set(year, monthOneBased - 1, day, hour, minute, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val defaultDeadlineMillis: Long = timestampOf(2026, 11, 5, 23, 59)

    val initialDocuments: List<DocumentEntity> = listOf(
        DocumentEntity(
            id = DOC_CONTRATO_SERVICOS_ID,
            title = "Contrato de serviços",
            fileName = SEED_PDF_FILE_NAME,
            originalDisplayName = "contrato-servicos.pdf",
            status = DocumentStatus.PENDING,
            createdAt = timestampOf(2026, 10, 28, 10, 32),
            sentAt = timestampOf(2026, 10, 28, 10, 32),
            deadlineAt = defaultDeadlineMillis,
            pageCount = 6,
            fileSizeBytes = 1_887_436L,
            ownerAdminId = MOCK_ADMIN_ID,
            inviteMessage = Document.DEFAULT_INVITE_MESSAGE
        ),
        DocumentEntity(
            id = DOC_PROPOSTA_COMERCIAL_ID,
            title = "Proposta comercial",
            fileName = SEED_PDF_FILE_NAME,
            originalDisplayName = "proposta-comercial.pdf",
            status = DocumentStatus.PENDING,
            createdAt = timestampOf(2026, 10, 27, 16, 10),
            sentAt = timestampOf(2026, 10, 27, 16, 10),
            deadlineAt = defaultDeadlineMillis,
            pageCount = 6,
            fileSizeBytes = 1_420_000L,
            ownerAdminId = MOCK_ADMIN_ID,
            inviteMessage = Document.DEFAULT_INVITE_MESSAGE
        ),
        DocumentEntity(
            id = DOC_ADITIVO_CONTRATUAL_ID,
            title = "Aditivo contratual",
            fileName = SEED_PDF_FILE_NAME,
            originalDisplayName = "aditivo-contratual.pdf",
            status = DocumentStatus.PENDING,
            createdAt = timestampOf(2026, 10, 26, 10, 5),
            sentAt = timestampOf(2026, 10, 26, 10, 5),
            deadlineAt = defaultDeadlineMillis,
            pageCount = 6,
            fileSizeBytes = 1_250_000L,
            ownerAdminId = MOCK_ADMIN_ID,
            inviteMessage = Document.DEFAULT_INVITE_MESSAGE
        ),
        DocumentEntity(
            id = DOC_TERMO_ADESAO_ID,
            title = "Termo de adesão",
            fileName = SEED_PDF_FILE_NAME,
            originalDisplayName = "termo-adesao.pdf",
            status = DocumentStatus.COMPLETED,
            createdAt = timestampOf(2026, 10, 25, 17, 48),
            sentAt = timestampOf(2026, 10, 25, 17, 48),
            deadlineAt = defaultDeadlineMillis,
            pageCount = 6,
            fileSizeBytes = 980_000L,
            ownerAdminId = MOCK_ADMIN_ID,
            inviteMessage = Document.DEFAULT_INVITE_MESSAGE
        ),
        DocumentEntity(
            id = DOC_AUTORIZACAO_IMAGEM_ID,
            title = "Autorização de imagem",
            fileName = SEED_PDF_FILE_NAME,
            originalDisplayName = "autorizacao-imagem.pdf",
            status = DocumentStatus.COMPLETED,
            createdAt = timestampOf(2026, 10, 25, 14, 40),
            sentAt = timestampOf(2026, 10, 25, 14, 40),
            deadlineAt = defaultDeadlineMillis,
            pageCount = 6,
            fileSizeBytes = 860_000L,
            ownerAdminId = MOCK_ADMIN_ID,
            inviteMessage = Document.DEFAULT_INVITE_MESSAGE
        )
    )

    val initialDocumentSigners: List<DocumentSignerCrossRef> = listOf(
        // 1. Contrato de serviços (0/3 assinaturas - Enviado)
        DocumentSignerCrossRef(
            documentId = DOC_CONTRATO_SERVICOS_ID,
            signerId = MOCK_SIGNER_1_ID,
            roleClass = SignerRoleClass.CONTRATANTE,
            status = SignatureStatus.PENDING,
            deliveryChannel = SignerDeliveryChannel.EMAIL,
            authMethod = SignerAuthMethod.EMAIL,
            colorIndex = 0
        ),
        DocumentSignerCrossRef(
            documentId = DOC_CONTRATO_SERVICOS_ID,
            signerId = MOCK_SIGNER_2_ID,
            roleClass = SignerRoleClass.CONTRATADO,
            status = SignatureStatus.PENDING,
            deliveryChannel = SignerDeliveryChannel.EMAIL,
            authMethod = SignerAuthMethod.EMAIL,
            colorIndex = 1
        ),
        DocumentSignerCrossRef(
            documentId = DOC_CONTRATO_SERVICOS_ID,
            signerId = MOCK_SIGNER_3_ID,
            roleClass = SignerRoleClass.TESTEMUNHA,
            status = SignatureStatus.PENDING,
            deliveryChannel = SignerDeliveryChannel.EMAIL,
            authMethod = SignerAuthMethod.EMAIL,
            colorIndex = 2
        ),

        // 2. Aditivo contratual (2/3 assinaturas - Parcialmente assinado, aguardando Beatriz)
        DocumentSignerCrossRef(
            documentId = DOC_ADITIVO_CONTRATUAL_ID,
            signerId = MOCK_SIGNER_1_ID,
            roleClass = SignerRoleClass.CONTRATANTE,
            status = SignatureStatus.SIGNED,
            signedAt = timestampOf(2026, 10, 28, 11, 4),
            signatureIp = "192.168.1.101",
            colorIndex = 0
        ),
        DocumentSignerCrossRef(
            documentId = DOC_ADITIVO_CONTRATUAL_ID,
            signerId = MOCK_SIGNER_2_ID,
            roleClass = SignerRoleClass.CONTRATADO,
            status = SignatureStatus.SIGNED,
            signedAt = timestampOf(2026, 10, 28, 13, 20),
            signatureIp = "192.168.1.102",
            colorIndex = 1
        ),
        DocumentSignerCrossRef(
            documentId = DOC_ADITIVO_CONTRATUAL_ID,
            signerId = MOCK_SIGNER_3_ID,
            roleClass = SignerRoleClass.TESTEMUNHA,
            status = SignatureStatus.PENDING,
            colorIndex = 2
        ),

        // 3. Proposta comercial (0/3 assinaturas - Enviado)
        DocumentSignerCrossRef(
            documentId = DOC_PROPOSTA_COMERCIAL_ID,
            signerId = MOCK_SIGNER_1_ID,
            roleClass = SignerRoleClass.CONTRATANTE,
            status = SignatureStatus.PENDING,
            colorIndex = 0
        ),
        DocumentSignerCrossRef(
            documentId = DOC_PROPOSTA_COMERCIAL_ID,
            signerId = MOCK_SIGNER_2_ID,
            roleClass = SignerRoleClass.CONTRATADO,
            status = SignatureStatus.PENDING,
            colorIndex = 1
        ),
        DocumentSignerCrossRef(
            documentId = DOC_PROPOSTA_COMERCIAL_ID,
            signerId = MOCK_SIGNER_3_ID,
            roleClass = SignerRoleClass.TESTEMUNHA,
            status = SignatureStatus.PENDING,
            colorIndex = 2
        ),

        // 4. Termo de adesão (3/3 assinaturas - Concluído)
        DocumentSignerCrossRef(
            documentId = DOC_TERMO_ADESAO_ID,
            signerId = MOCK_SIGNER_1_ID,
            roleClass = SignerRoleClass.CONTRATANTE,
            status = SignatureStatus.SIGNED,
            signedAt = timestampOf(2026, 10, 28, 11, 4),
            signatureIp = "192.168.1.101",
            colorIndex = 0
        ),
        DocumentSignerCrossRef(
            documentId = DOC_TERMO_ADESAO_ID,
            signerId = MOCK_SIGNER_2_ID,
            roleClass = SignerRoleClass.CONTRATADO,
            status = SignatureStatus.SIGNED,
            signedAt = timestampOf(2026, 10, 28, 13, 20),
            signatureIp = "192.168.1.102",
            colorIndex = 1
        ),
        DocumentSignerCrossRef(
            documentId = DOC_TERMO_ADESAO_ID,
            signerId = MOCK_SIGNER_3_ID,
            roleClass = SignerRoleClass.TESTEMUNHA,
            status = SignatureStatus.SIGNED,
            signedAt = timestampOf(2026, 10, 28, 15, 42),
            signatureIp = "192.168.1.103",
            colorIndex = 2
        ),

        // 5. Autorização de imagem (3/3 assinaturas - Concluído)
        DocumentSignerCrossRef(
            documentId = DOC_AUTORIZACAO_IMAGEM_ID,
            signerId = MOCK_SIGNER_1_ID,
            roleClass = SignerRoleClass.CONTRATANTE,
            status = SignatureStatus.SIGNED,
            signedAt = timestampOf(2026, 10, 26, 9, 15),
            signatureIp = "192.168.1.101",
            colorIndex = 0
        ),
        DocumentSignerCrossRef(
            documentId = DOC_AUTORIZACAO_IMAGEM_ID,
            signerId = MOCK_SIGNER_2_ID,
            roleClass = SignerRoleClass.CONTRATADO,
            status = SignatureStatus.SIGNED,
            signedAt = timestampOf(2026, 10, 26, 11, 30),
            signatureIp = "192.168.1.102",
            colorIndex = 1
        ),
        DocumentSignerCrossRef(
            documentId = DOC_AUTORIZACAO_IMAGEM_ID,
            signerId = MOCK_SIGNER_3_ID,
            roleClass = SignerRoleClass.TESTEMUNHA,
            status = SignatureStatus.SIGNED,
            signedAt = timestampOf(2026, 10, 26, 14, 10),
            signatureIp = "192.168.1.103",
            colorIndex = 2
        )
    )

    val initialSignatureFields: List<SignatureFieldEntity> = listOf(
        // Contrato de serviços (página 2 -> pageNumber = 1)
        SignatureFieldEntity(
            id = "seed-field-1",
            documentId = DOC_CONTRATO_SERVICOS_ID,
            signerId = MOCK_SIGNER_1_ID,
            pageNumber = 1,
            relativeX = 0.08f,
            relativeY = 0.68f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f,
            fieldType = SignatureFieldType.SIGNATURE
        ),
        SignatureFieldEntity(
            id = "seed-field-2",
            documentId = DOC_CONTRATO_SERVICOS_ID,
            signerId = MOCK_SIGNER_2_ID,
            pageNumber = 1,
            relativeX = 0.08f,
            relativeY = 0.56f,
            relativeWidth = 0.22f,
            relativeHeight = 0.07f,
            fieldType = SignatureFieldType.INITIALS
        ),
        SignatureFieldEntity(
            id = "seed-field-3",
            documentId = DOC_CONTRATO_SERVICOS_ID,
            signerId = MOCK_SIGNER_2_ID,
            pageNumber = 1,
            relativeX = 0.54f,
            relativeY = 0.68f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f,
            fieldType = SignatureFieldType.SIGNATURE
        ),
        SignatureFieldEntity(
            id = "seed-field-4",
            documentId = DOC_CONTRATO_SERVICOS_ID,
            signerId = MOCK_SIGNER_3_ID,
            pageNumber = 1,
            relativeX = 0.31f,
            relativeY = 0.82f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f,
            fieldType = SignatureFieldType.SIGNATURE
        ),
        // Aditivo contratual
        SignatureFieldEntity(
            id = "seed-field-5",
            documentId = DOC_ADITIVO_CONTRATUAL_ID,
            signerId = MOCK_SIGNER_1_ID,
            pageNumber = 1,
            relativeX = 0.08f,
            relativeY = 0.68f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f,
            fieldType = SignatureFieldType.SIGNATURE
        ),
        SignatureFieldEntity(
            id = "seed-field-6",
            documentId = DOC_ADITIVO_CONTRATUAL_ID,
            signerId = MOCK_SIGNER_2_ID,
            pageNumber = 1,
            relativeX = 0.54f,
            relativeY = 0.68f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f,
            fieldType = SignatureFieldType.SIGNATURE
        ),
        SignatureFieldEntity(
            id = "seed-field-7",
            documentId = DOC_ADITIVO_CONTRATUAL_ID,
            signerId = MOCK_SIGNER_3_ID,
            pageNumber = 1,
            relativeX = 0.31f,
            relativeY = 0.82f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f,
            fieldType = SignatureFieldType.SIGNATURE
        ),
        // Proposta comercial
        SignatureFieldEntity(
            id = "seed-field-8",
            documentId = DOC_PROPOSTA_COMERCIAL_ID,
            signerId = MOCK_SIGNER_1_ID,
            pageNumber = 1,
            relativeX = 0.08f,
            relativeY = 0.68f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f,
            fieldType = SignatureFieldType.SIGNATURE
        ),
        SignatureFieldEntity(
            id = "seed-field-9",
            documentId = DOC_PROPOSTA_COMERCIAL_ID,
            signerId = MOCK_SIGNER_2_ID,
            pageNumber = 1,
            relativeX = 0.54f,
            relativeY = 0.68f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f,
            fieldType = SignatureFieldType.SIGNATURE
        ),
        SignatureFieldEntity(
            id = "seed-field-10",
            documentId = DOC_PROPOSTA_COMERCIAL_ID,
            signerId = MOCK_SIGNER_3_ID,
            pageNumber = 1,
            relativeX = 0.31f,
            relativeY = 0.82f,
            relativeWidth = 0.36f,
            relativeHeight = 0.08f,
            fieldType = SignatureFieldType.SIGNATURE
        )
    )

    suspend fun seedIfNeeded(
        userDao: UserDao,
        documentDao: DocumentDao? = null,
        signatureFieldDao: SignatureFieldDao? = null,
        filesDir: File? = null
    ) {
        if (filesDir != null) {
            ensureSeedPdfFileExists(filesDir)
        }
        if (userDao.getUserCount() == 0) {
            userDao.insertUsersIgnore(initialUsers)
            if (documentDao != null && signatureFieldDao != null) {
                initialDocuments.forEach { doc ->
                    documentDao.insertDocument(doc)
                }
                initialDocumentSigners.forEach { crossRef ->
                    documentDao.upsertDocumentSigner(crossRef)
                }
                initialSignatureFields.forEach { field ->
                    signatureFieldDao.upsertSignatureField(field)
                }
            }
        }
    }

    /**
     * Gera um arquivo PDF válido de 6 páginas em `filesDir/contrato-servicos.pdf` caso ainda não exista,
     * para que todos os documentos pré-carregados possam ser abertos com PdfRenderer e assinados com PDFBox.
     */
    fun ensureSeedPdfFileExists(filesDir: File): File {
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }
        val targetFile = File(filesDir, SEED_PDF_FILE_NAME)
        if (targetFile.exists() && targetFile.length() > 0L) {
            return targetFile
        }
        runCatching {
            PDDocument().use { document ->
                val canUseStandardFonts = com.tom_roush.pdfbox.android.PDFBoxResourceLoader.isReady()
                for (pageIndex in 0 until 6) {
                    val page = PDPage(PDRectangle.A4)
                    document.addPage(page)
                    PDPageContentStream(document, page).use { stream ->
                        if (canUseStandardFonts) {
                            stream.beginText()
                            stream.setFont(PDType1Font.HELVETICA_BOLD, 14f)
                            stream.newLineAtOffset(60f, 760f)
                            stream.showText("CONTRATO DE PRESTACAO DE SERVICOS - PAGINA ${pageIndex + 1} DE 6")
                            stream.endText()

                            stream.beginText()
                            stream.setFont(PDType1Font.HELVETICA_BOLD, 11f)
                            stream.newLineAtOffset(60f, 700f)
                            stream.showText("CLAUSULA 2a - DO OBJETO")
                            stream.endText()

                            stream.beginText()
                            stream.setFont(PDType1Font.HELVETICA, 10f)
                            stream.newLineAtOffset(60f, 675f)
                            stream.showText("O presente instrumento tem como objeto a prestacao de servicos")
                            stream.newLineAtOffset(0f, -18f)
                            stream.showText("corporativos com assinatura eletronica de validade juridica LetsSign.")
                            stream.endText()

                            stream.beginText()
                            stream.setFont(PDType1Font.HELVETICA_BOLD, 11f)
                            stream.newLineAtOffset(60f, 590f)
                            stream.showText("CLAUSULA 3a - DO PRAZO")
                            stream.endText()

                            stream.beginText()
                            stream.setFont(PDType1Font.HELVETICA, 10f)
                            stream.newLineAtOffset(60f, 565f)
                            stream.showText("Prazo de vigencia e coleta das assinaturas ate 05/11/2026.")
                            stream.endText()
                        }

                        // Linhas de assinatura
                        stream.moveTo(60f, 220f)
                        stream.lineTo(240f, 220f)
                        stream.stroke()

                        if (canUseStandardFonts) {
                            stream.beginText()
                            stream.setFont(PDType1Font.HELVETICA_BOLD, 9f)
                            stream.newLineAtOffset(60f, 205f)
                            stream.showText("CONTRATANTE")
                            stream.endText()
                        }

                        stream.moveTo(320f, 220f)
                        stream.lineTo(500f, 220f)
                        stream.stroke()

                        if (canUseStandardFonts) {
                            stream.beginText()
                            stream.setFont(PDType1Font.HELVETICA_BOLD, 9f)
                            stream.newLineAtOffset(320f, 205f)
                            stream.showText("CONTRATADO")
                            stream.endText()
                        }

                        stream.moveTo(190f, 110f)
                        stream.lineTo(370f, 110f)
                        stream.stroke()

                        if (canUseStandardFonts) {
                            stream.beginText()
                            stream.setFont(PDType1Font.HELVETICA_BOLD, 9f)
                            stream.newLineAtOffset(190f, 95f)
                            stream.showText("TESTEMUNHA")
                            stream.endText()
                        }
                    }
                }
                document.save(targetFile)
            }
        }
        return targetFile
    }
}
