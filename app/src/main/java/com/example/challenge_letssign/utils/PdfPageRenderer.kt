package com.example.challenge_letssign.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.example.challenge_letssign.data.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class RenderedPdfPage(
    val pageIndex: Int,
    val pageCount: Int,
    val bitmap: Bitmap,
    val aspectRatio: Float
)

@Singleton
class PdfPageRenderer @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun renderPage(
        pdfFile: File,
        pageIndex: Int,
        targetWidthPx: Int = 1200
    ): AppResult<RenderedPdfPage> = withContext(ioDispatcher) {
        runCatchingResult("Erro ao renderizar página do PDF.") {
            if (!pdfFile.exists()) {
                throw IllegalStateException("Arquivo PDF não encontrado.")
            }

            ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val totalPages = renderer.pageCount
                    if (totalPages <= 0) {
                        throw IllegalStateException("O documento PDF não possui páginas.")
                    }
                    val safeIndex = pageIndex.coerceIn(0, totalPages - 1)

                    renderer.openPage(safeIndex).use { page ->
                        val pageWidth = page.width.coerceAtLeast(1)
                        val pageHeight = page.height.coerceAtLeast(1)
                        val aspectRatio = pageWidth.toFloat() / pageHeight.toFloat()

                        val bitmapWidth = targetWidthPx.coerceAtLeast(600)
                        val bitmapHeight = (bitmapWidth / aspectRatio).toInt().coerceAtLeast(1)

                        val bitmap = Bitmap.createBitmap(
                            bitmapWidth,
                            bitmapHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        // Fundo branco garantido para PDFs com transparência
                        bitmap.eraseColor(Color.WHITE)

                        page.render(
                            bitmap,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )

                        RenderedPdfPage(
                            pageIndex = safeIndex,
                            pageCount = totalPages,
                            bitmap = bitmap,
                            aspectRatio = aspectRatio
                        )
                    }
                }
            }
        }
    }
}
