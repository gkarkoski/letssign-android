package com.example.challenge_letssign.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// LetsSign Design Tokens (Figma)
object LetsSignColors {
    val Primary = Color(0xFF6D3BF5)       // Ação, links, foco
    val VioletDeep = Color(0xFF5527D6)    // Texto sobre violeta-soft
    val VioletSoft = Color(0xFFEFEAFE)    // Fundos suaves
    val Ink = Color(0xFF17123A)           // Títulos, seção escura
    val InkSoft = Color(0xFF241D52)       // Variação de ink
    val Body = Color(0xFF444060)          // Texto corrido
    val Muted = Color(0xFF6F6B8A)         // Texto de apoio
    val Line = Color(0xFFE6E3F2)          // Bordas
    val Mist = Color(0xFFF7F6FC)          // Fundo de telas
    val Cyan = Color(0xFF0BBFD6)          // Só decoração
    val CyanGlow = Color(0xFF09EEFE)      // Só sobre escuro
    val Success = Color(0xFF178A52)       // Sucesso
    val SuccessSoft = Color(0xFFE7F6EE)   // Fundo suave de sucesso
    val Error = Color(0xFFC92A45)         // Erro
    val ErrorSoft = Color(0xFFFCECEE)     // Fundo suave de erro
    val SurfaceWhite = Color(0xFFFFFFFF)

    // Gradiente Header e Splash: 160deg #7a4cff, #6d3bf5 40%, #4f22d0
    val HeaderGradient = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF7A4CFF),
            0.45f to Color(0xFF6D3BF5),
            1.0f to Color(0xFF4F22D0)
        ),
        start = Offset(0f, 0f),
        end = Offset(400f, 900f)
    )

    // Gradiente Ícone: 135deg #8654ff, #6d3bf5 55%, #0bbfd6
    val IconGradient = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF8654FF),
            0.55f to Color(0xFF6D3BF5),
            1.0f to Color(0xFF0BBFD6)
        ),
        start = Offset(0f, 0f),
        end = Offset(200f, 200f)
    )
}
