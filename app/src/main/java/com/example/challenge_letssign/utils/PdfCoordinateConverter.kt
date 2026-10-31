package com.example.challenge_letssign.utils

data class RelativeCoordinates(
    val relativeX: Float,
    val relativeY: Float,
    val relativeWidth: Float,
    val relativeHeight: Float
)

data class PixelBounds(
    val xPx: Float,
    val yPx: Float,
    val widthPx: Float,
    val heightPx: Float
)

object PdfCoordinateConverter {

    const val DEFAULT_RELATIVE_WIDTH = 0.36f
    const val DEFAULT_RELATIVE_HEIGHT = 0.08f
    private const val EPSILON = 0.0001f

    /**
     * Matemática de Coordenadas (CRÍTICO - Milestone 5):
     * Converte dropX, dropY, width, height em pixels da tela para coordenadas relativas (0.0 a 1.0):
     * relativeX = dropX / viewWidth
     * relativeY = dropY / viewHeight
     * relativeWidth = width / viewWidth
     * relativeHeight = height / viewHeight
     */
    fun pixelsToRelative(
        dropXPx: Float,
        dropYPx: Float,
        widthPx: Float,
        heightPx: Float,
        viewWidthPx: Float,
        viewHeightPx: Float
    ): RelativeCoordinates {
        require(viewWidthPx > 0f && viewHeightPx > 0f) {
            "Dimensões da view do PDF devem ser maiores que zero."
        }
        return RelativeCoordinates(
            relativeX = dropXPx / viewWidthPx,
            relativeY = dropYPx / viewHeightPx,
            relativeWidth = widthPx / viewWidthPx,
            relativeHeight = heightPx / viewHeightPx
        )
    }

    /**
     * Converte coordenadas relativas (0.0 a 1.0) de volta para pixels na tela atual.
     */
    fun relativeToPixels(
        relativeX: Float,
        relativeY: Float,
        relativeWidth: Float,
        relativeHeight: Float,
        viewWidthPx: Float,
        viewHeightPx: Float
    ): PixelBounds {
        return PixelBounds(
            xPx = relativeX * viewWidthPx,
            yPx = relativeY * viewHeightPx,
            widthPx = relativeWidth * viewWidthPx,
            heightPx = relativeHeight * viewHeightPx
        )
    }

    /**
     * Regra 2 (Limites do PDF):
     * relativeX >= 0.0, relativeY >= 0.0,
     * relativeX + relativeWidth <= 1.0 e relativeY + relativeHeight <= 1.0
     */
    fun isWithinPageBounds(
        relativeX: Float,
        relativeY: Float,
        relativeWidth: Float,
        relativeHeight: Float
    ): Boolean {
        if (relativeWidth <= 0f || relativeHeight <= 0f) return false
        if (relativeX < -EPSILON || relativeY < -EPSILON) return false
        if (relativeX + relativeWidth > 1.0f + EPSILON) return false
        if (relativeY + relativeHeight > 1.0f + EPSILON) return false
        return true
    }

    fun isWithinPageBounds(coords: RelativeCoordinates): Boolean =
        isWithinPageBounds(
            relativeX = coords.relativeX,
            relativeY = coords.relativeY,
            relativeWidth = coords.relativeWidth,
            relativeHeight = coords.relativeHeight
        )
}
