package com.example.challenge_letssign.data.repository.local

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.example.challenge_letssign.data.di.IoDispatcher
import com.example.challenge_letssign.domain.model.ImportedPdfFile
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.repository.PdfStorageRepository
import com.example.challenge_letssign.utils.AppResult
import com.example.challenge_letssign.utils.PdfCoordinateConverter
import com.example.challenge_letssign.utils.runCatchingResult
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPdfStorageRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PdfStorageRepository {

    override suspend fun importPdfFromUri(
        uriString: String,
        fallbackName: String?
    ): AppResult<ImportedPdfFile> = withContext(ioDispatcher) {
        val uri = Uri.parse(uriString)
        val displayName = resolveDisplayName(uri) ?: fallbackName ?: "documento.pdf"
        val internalFileName = "doc_${UUID.randomUUID()}.pdf"
        val destFile = File(context.filesDir, internalFileName)

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext AppResult.Error(
                    "Não foi possível abrir o arquivo selecionado."
                )

            inputStream.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (!destFile.exists() || destFile.length() == 0L) {
                destFile.delete()
                return@withContext AppResult.Error(
                    "Não foi possível processar $displayName. Escolha outro arquivo PDF."
                )
            }

            // Valida se é um PDF real e obtém quantidade de páginas via PdfRenderer
            val pageCount = validatePdfAndCountPages(destFile)
            if (pageCount <= 0) {
                destFile.delete()
                return@withContext AppResult.Error(
                    "Não foi possível processar $displayName. Escolha outro arquivo PDF."
                )
            }

            AppResult.Success(
                ImportedPdfFile(
                    internalFileName = internalFileName,
                    originalDisplayName = displayName,
                    pageCount = pageCount,
                    fileSizeBytes = destFile.length()
                )
            )
        } catch (e: Exception) {
            if (destFile.exists()) {
                destFile.delete()
            }
            AppResult.Error(
                message = "Não foi possível processar $displayName. Escolha outro arquivo PDF.",
                cause = e
            )
        }
    }

    override suspend fun getPdfFile(internalFileName: String): AppResult<File> =
        withContext(ioDispatcher) {
            runCatchingResult("Arquivo PDF não encontrado no armazenamento interno.") {
                val file = File(context.filesDir, internalFileName)
                if (!file.exists()) {
                    throw IllegalStateException("Arquivo PDF não encontrado: $internalFileName")
                }
                file
            }
        }

    override suspend fun deletePdfFile(internalFileName: String): AppResult<Unit> =
        withContext(ioDispatcher) {
            runCatchingResult("Erro ao remover arquivo PDF.") {
                val file = File(context.filesDir, internalFileName)
                if (file.exists()) {
                    file.delete()
                }
            }
        }

    /**
     * Renderização no PDF (PDFBox - Milestone 7):
     * 1. Carregar arquivo original via PDDocument.load().
     * 2. Ir para a página correta (PDPage).
     * 3. Calcular posição absoluta: absX = relativeX * page.cropBox.width.
     * 4. Desenhar o Bitmap da assinatura usando PDPageContentStream.
     * 5. Salvar o documento (sobrescrevendo o arquivo interno).
     */
    override suspend fun embedSignaturesIntoPdf(
        internalFileName: String,
        fields: List<SignatureField>,
        signaturePngBytes: ByteArray
    ): AppResult<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Erro ao gravar assinatura no documento PDF.") {
            val pdfFile = File(context.filesDir, internalFileName)
            if (!pdfFile.exists()) {
                throw IllegalStateException("Arquivo PDF original não encontrado.")
            }
            if (fields.isEmpty()) {
                throw IllegalArgumentException("Nenhum campo de assinatura atribuído ao signatário.")
            }

            val signatureBitmap = BitmapFactory.decodeByteArray(
                signaturePngBytes,
                0,
                signaturePngBytes.size
            ) ?: throw IllegalArgumentException("Imagem da assinatura inválida.")

            val tempOutFile = File(context.filesDir, "signed_tmp_${UUID.randomUUID()}.pdf")
            try {
                PDDocument.load(pdfFile).use { document ->
                    val pdImage = LosslessFactory.createFromImage(document, signatureBitmap)
                    val totalPages = document.numberOfPages

                    for (field in fields) {
                        val pageIdx = field.pageNumber.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
                        val page = document.getPage(pageIdx)
                        val cropBox = page.cropBox

                        val placement = PdfCoordinateConverter.relativeToPdfPageCoordinates(
                            relativeX = field.relativeX,
                            relativeY = field.relativeY,
                            relativeWidth = field.relativeWidth,
                            relativeHeight = field.relativeHeight,
                            cropBoxWidth = cropBox.width,
                            cropBoxHeight = cropBox.height,
                            cropBoxLowerLeftX = cropBox.lowerLeftX,
                            cropBoxLowerLeftY = cropBox.lowerLeftY
                        )

                        PDPageContentStream(
                            document,
                            page,
                            PDPageContentStream.AppendMode.APPEND,
                            true,
                            true
                        ).use { contentStream ->
                            contentStream.drawImage(
                                pdImage,
                                placement.absX,
                                placement.absY,
                                placement.absWidth,
                                placement.absHeight
                            )
                        }
                    }

                    document.save(tempOutFile)
                }

                // Sobrescreve o arquivo original de forma segura
                if (!tempOutFile.renameTo(pdfFile)) {
                    tempOutFile.copyTo(pdfFile, overwrite = true)
                    tempOutFile.delete()
                }
            } finally {
                signatureBitmap.recycle()
                if (tempOutFile.exists()) {
                    tempOutFile.delete()
                }
            }
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    null
                }
            } ?: uri.lastPathSegment
        } catch (_: Exception) {
            uri.lastPathSegment
        }
    }

    private fun validatePdfAndCountPages(file: File): Int {
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                renderer.pageCount
            }
        }
    }
}
