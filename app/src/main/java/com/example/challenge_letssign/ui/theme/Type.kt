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

// Escala Tipográfica LetsSign (13, 15, 17, 19, 22, 28. Base 17)
val Typography = Typography(
    // H1: Plus Jakarta Sans 760 · 28 / 1.1 · -0.03em
    headlineLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.03).em,
        color = LetsSignColors.Ink
    ),
    // H2: Plus Jakarta Sans 640 · 22 / 1.1
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 25.sp,
        color = LetsSignColors.Ink
    ),
    // H3: Plus Jakarta Sans 640 · 19 / 1.1
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 22.sp,
        color = LetsSignColors.Ink
    ),
    // H4: Plus Jakarta Sans 640 · 17
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        color = LetsSignColors.Ink
    ),
    // Botão: Plus Jakarta Sans 620 · 17
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 20.sp
    ),
    // Corpo: Manrope 400 · 17 / 1.65
    bodyLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        color = LetsSignColors.Body
    ),
    // Corpo pequeno: Manrope 400 · 15 / 1.65
    bodyMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        color = LetsSignColors.Body
    ),
    // Legenda: Manrope 400 · 13 / 1.65
    bodySmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        color = LetsSignColors.Muted
    )
)
