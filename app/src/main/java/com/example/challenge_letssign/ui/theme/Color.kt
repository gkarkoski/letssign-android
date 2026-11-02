package com.example.challenge_letssign.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * LetsSign Design System Tokens (extraídos da Biblioteca LetsSign no Figma / Tokens.png / fluxo_telas.md).
 */
object LetsSignColors {
    val Primary = Color(0xFF6D3BF5)       // color/primary: Ação principal, abas ativas, links, foco
    val VioletDeep = Color(0xFF5527D6)    // color/violet-deep: Variante escura do primário / texto sobre violet-soft
    val VioletSoft = Color(0xFFEFEAFE)    // color/violet-soft: Fundo suave do primário
    val Ink = Color(0xFF17123A)           // color/ink: Títulos, seção escura
    val InkSoft = Color(0xFF241D52)       // color/ink-soft: Texto forte secundário / variação de ink
    val Body = Color(0xFF444060)          // color/body: Texto de corpo / texto corrido
    val Muted = Color(0xFF6F6B8A)         // color/muted: Texto de apoio
    val Line = Color(0xFFE6E3F2)          // color/line: Bordas e divisores
    val Mist = Color(0xFFF7F6FC)          // color/mist: Fundo de áreas e telas
    val Paper = Color(0xFFFFFFFF)         // color/paper: Fundo de cartões e telas
    val SurfaceWhite = Paper

    // Sucesso
    val Success = Color(0xFF178A52)       // color/success
    val SuccessText = Color(0xFF0F6B3E)   // color/success-text
    val SuccessSoft = Color(0xFFE7F7EE)   // color/success-soft

    // Erro
    val Error = Color(0xFFC92A45)         // color/error
    val ErrorText = Color(0xFFA8213A)     // color/error-text
    val ErrorSoft = Color(0xFFFCECEE)     // Fundo suave de erro

    // Cores de cada signatário nos campos e indicadores
    val Signer1 = Color(0xFF6D3BF5)       // color/signer-1 (Marina / 1º signatário)
    val Signer1Soft = Color(0xFFEFEAFE)
    val Signer2 = Color(0xFF17123A)       // color/signer-2 (Rafael / 2º signatário)
    val Signer2Soft = Color(0xFFECEBF2)
    val Signer3 = Color(0xFF0A7886)       // color/signer-3 (Beatriz / 3º signatário)
    val Signer3Soft = Color(0xFFE6F4F6)

    // Destaques e gradientes
    val Cyan = Color(0xFF0BBFD6)          // color/cyan: Só decoração
    val CyanGlow = Color(0xFF09EEFE)      // color/cyan-glow: Só sobre escuro
    val GradTop = Color(0xFF7A4CFF)       // color/grad-top
    val GradBase = Color(0xFF4F22D0)      // color/grad-base

    // Gradiente Header e Splash: 160deg #7a4cff, #6d3bf5 40%, #4f22d0
    val HeaderGradient = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to GradTop,
            0.40f to Primary,
            1.0f to GradBase
        ),
        start = Offset(0f, 0f),
        end = Offset(400f, 900f)
    )

    // Gradiente Ícone: 135deg #8654ff, #6d3bf5 55%, #0bbfd6
    val IconGradient = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF8654FF),
            0.55f to Primary,
            1.0f to Cyan
        ),
        start = Offset(0f, 0f),
        end = Offset(200f, 200f)
    )

    // Gradiente Seção Escura: #17123a com brilhos violeta e ciano (usado em Documento enviado! e Documento assinado!)
    val DarkSectionGradient = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to Ink,
            0.65f to InkSoft,
            1.0f to Color(0xFF1E1954)
        ),
        start = Offset(0f, 0f),
        end = Offset(500f, 500f)
    )

    fun signerColor(index: Int): Color = when (index % 3) {
        0 -> Signer1
        1 -> Signer2
        else -> Signer3
    }

    fun signerSoftColor(index: Int): Color = when (index % 3) {
        0 -> Signer1Soft
        1 -> Signer2Soft
        else -> Signer3Soft
    }
}

/**
 * Tokens de espaçamento (grade de 8pt) e raios de borda da Biblioteca LetsSign.
 */
object LetsSignDimens {
    val Space4 = 4.dp
    val Space8 = 8.dp
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Space24 = 24.dp     // Margem lateral padrão das telas
    val Space32 = 32.dp
    val Space40 = 40.dp
    val Space48 = 48.dp

    val RadiusInput = 8.dp
    val RadiusChip = 12.dp
    val RadiusCard = 14.dp
    val RadiusBlock = 22.dp
    val RadiusPill = 999.dp

    val ShapeInput = RoundedCornerShape(RadiusInput)
    val ShapeChip = RoundedCornerShape(RadiusChip)
    val ShapeCard = RoundedCornerShape(RadiusCard)
    val ShapeBlock = RoundedCornerShape(RadiusBlock)
    val ShapePill = RoundedCornerShape(RadiusPill)
}
