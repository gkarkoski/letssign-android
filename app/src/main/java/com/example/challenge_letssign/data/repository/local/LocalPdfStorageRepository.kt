package com.example.challenge_letssign.data.repository.local

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.example.challenge_letssign.data.di.IoDispatcher
import com.example.challenge_letssign.domain.model.ImportedPdfFile
import com.example.challenge_letssign.domain.repository.PdfStorageRepository
import com.example.challenge_letssign.utils.AppResult
import com.example.challenge_letssign.utils.runCatchingResult
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
                    pageCount = pageCount
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
