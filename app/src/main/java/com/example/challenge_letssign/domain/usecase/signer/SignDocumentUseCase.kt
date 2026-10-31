package com.example.challenge_letssign.domain.usecase.signer

import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.repository.PdfStorageRepository
import com.example.challenge_letssign.utils.AppResult
import com.example.challenge_letssign.utils.IpAddressResolver
import javax.inject.Inject

/**
 * Fluxo do Signatário e Processamento PDF (PDFBox - Milestone 7):
 * 1. Busca o documento e os campos atribuídos ao signatário logado.
 * 2. Grava o Bitmap (PNG bytes) da assinatura no PDF original usando PDFBox.
 * 3. Atualiza o status no Room para SIGNED capturando data/hora UTC e IP local para validade jurídica.
 * 4. Executa a Regra 5 (Conclusão Automática): se não restar nenhum PENDING -> documento vira COMPLETED.
 */
class SignDocumentUseCase @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val pdfStorageRepository: PdfStorageRepository
) {
    suspend operator fun invoke(
        documentId: String,
        signerId: String,
        signaturePngBytes: ByteArray,
        signedAtMillis: Long = System.currentTimeMillis(),
        signatureIp: String = IpAddressResolver.resolveLocalIpAddress()
    ): AppResult<Unit> {
        if (documentId.isBlank() || signerId.isBlank()) {
            return AppResult.Error("Documento ou signatário inválido.")
        }
        if (signaturePngBytes.isEmpty()) {
            return AppResult.Error("Desenhe sua assinatura antes de confirmar.")
        }

        val docResult = documentRepository.getDocumentById(documentId)
        val document = when (docResult) {
            is AppResult.Success -> docResult.data
            is AppResult.Error -> return docResult
        }

        val fieldsResult = documentRepository.getSignatureFieldsBySigner(documentId, signerId)
        val signerFields = when (fieldsResult) {
            is AppResult.Success -> fieldsResult.data
            is AppResult.Error -> return fieldsResult
        }

        if (signerFields.isEmpty()) {
            return AppResult.Error("Nenhum campo de assinatura posicionado para este signatário.")
        }

        // Grava assinatura no arquivo PDF físico via PDFBox
        val embedResult = pdfStorageRepository.embedSignaturesIntoPdf(
            internalFileName = document.fileName,
            fields = signerFields,
            signaturePngBytes = signaturePngBytes
        )
        if (embedResult is AppResult.Error) {
            return embedResult
        }

        // Atualiza status no banco de dados e executa Regra 5 (Conclusão Automática)
        return documentRepository.markDocumentAsSignedBySigner(
            documentId = documentId,
            signerId = signerId,
            signedAt = signedAtMillis,
            signatureIp = signatureIp
        )
    }
}
