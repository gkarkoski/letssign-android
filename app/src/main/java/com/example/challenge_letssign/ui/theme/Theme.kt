package com.example.challenge_letssign.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LetsSignLightColorScheme = lightColorScheme(
    primary = LetsSignColors.Primary,
    onPrimary = Color.White,
    primaryContainer = LetsSignColors.VioletSoft,
    onPrimaryContainer = LetsSignColors.VioletDeep,
    secondary = LetsSignColors.VioletDeep,
    onSecondary = Color.White,
    tertiary = LetsSignColors.Cyan,
    background = LetsSignColors.Mist,
    onBackground = LetsSignColors.Ink,
    surface = LetsSignColors.SurfaceWhite,
    onSurface = LetsSignColors.Ink,
    surfaceVariant = LetsSignColors.Mist,
    onSurfaceVariant = LetsSignColors.Body,
    outline = LetsSignColors.Line,
    error = LetsSignColors.Error,
    onError = Color.White,
    errorContainer = LetsSignColors.ErrorSoft,
    onErrorContainer = LetsSignColors.Error
)

@Composable
fun Challenge_letsSignTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LetsSignLightColorScheme,
        typography = Typography,
        content = content
    )
}
