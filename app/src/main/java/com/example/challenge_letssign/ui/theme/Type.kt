package com.example.challenge_letssign.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.challenge_letssign.R

val PlusJakartaSansFamily = FontFamily(
    Font(resId = R.font.plus_jakarta_sans, weight = FontWeight.Medium),
    Font(resId = R.font.plus_jakarta_sans, weight = FontWeight.SemiBold),
    Font(resId = R.font.plus_jakarta_sans, weight = FontWeight.Bold),
    Font(resId = R.font.plus_jakarta_sans, weight = FontWeight.ExtraBold)
)

val ManropeFamily = FontFamily(
    Font(resId = R.font.manrope, weight = FontWeight.Normal),
    Font(resId = R.font.manrope, weight = FontWeight.Medium),
    Font(resId = R.font.manrope, weight = FontWeight.SemiBold)
)

// Fontes de assinatura e rubrica digitadas (fluxo_telas.md: Dancing Script, Caveat e Homemade Apple)
val DancingScriptFamily = FontFamily(
    Font(resId = R.font.dancing_script, weight = FontWeight.Normal),
    Font(resId = R.font.dancing_script, weight = FontWeight.Bold)
)

val CaveatFamily = FontFamily(
    Font(resId = R.font.caveat, weight = FontWeight.Normal),
    Font(resId = R.font.caveat, weight = FontWeight.Bold)
)

val HomemadeAppleFamily = FontFamily(
    Font(resId = R.font.homemade_apple, weight = FontWeight.Normal)
)

/**
 * Tokens tipográficos explícitos da Biblioteca LetsSign (fluxo_telas.md & Tokens.png).
 */
object LetsSignTypeTokens {
    // type/h1: Plus Jakarta Sans ExtraBold 28 / 110% / -0.03em
    val H1 = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.03).em,
        color = LetsSignColors.Ink
    )

    // type/h2: Plus Jakarta Sans SemiBold 22 / 110%
    val H2 = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 25.sp,
        color = LetsSignColors.Ink
    )

    // type/h3: Plus Jakarta Sans SemiBold 19 / 110%
    val H3 = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 22.sp,
        color = LetsSignColors.Ink
    )

    // type/h4: Plus Jakarta Sans SemiBold 17 / 125%
    val H4 = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        color = LetsSignColors.Ink
    )

    // type/button: Plus Jakarta Sans SemiBold 17 / 100%
    val Button = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 18.sp
    )

    // type/button-sm: Plus Jakarta Sans SemiBold 15 / 100%
    val ButtonSm = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 16.sp
    )

    // type/label: Plus Jakarta Sans SemiBold 13 / 110%
    val Label = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 15.sp,
        color = LetsSignColors.Ink
    )

    // type/label-15: Plus Jakarta Sans SemiBold 15 / 120%
    val Label15 = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 18.sp,
        color = LetsSignColors.Ink
    )

    // type/body: Manrope Regular 17 / 165%
    val Body = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 28.sp,
        color = LetsSignColors.Body
    )

    // type/body-sm: Manrope Regular 15 / 165%
    val BodySm = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 25.sp,
        color = LetsSignColors.Body
    )

    // type/caption: Manrope Regular 13 / 165%
    val Caption = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 21.sp,
        color = LetsSignColors.Muted
    )
}

// Escala Tipográfica LetsSign mapeada para o Material3 Typography
val Typography = Typography(
    headlineLarge = LetsSignTypeTokens.H1,
    headlineMedium = LetsSignTypeTokens.H2,
    headlineSmall = LetsSignTypeTokens.H3,
    titleMedium = LetsSignTypeTokens.H4,
    labelLarge = LetsSignTypeTokens.Button,
    labelMedium = LetsSignTypeTokens.Label15,
    labelSmall = LetsSignTypeTokens.Label,
    bodyLarge = LetsSignTypeTokens.Body,
    bodyMedium = LetsSignTypeTokens.BodySm,
    bodySmall = LetsSignTypeTokens.Caption
)
