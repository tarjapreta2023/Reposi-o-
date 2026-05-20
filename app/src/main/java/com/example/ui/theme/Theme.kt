package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BentoDeepBlueActionBg,
    secondary = BentoDarkActionBg,
    tertiary = BentoEfetivadosText,
    background = BentoBackground,
    surface = Color.White,
    error = BentoAlertAccent,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = BentoTextPrimary,
    onSurface = BentoTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = BentoDeepBlueActionBg,
    secondary = BentoDarkActionBg,
    tertiary = BentoEfetivadosText,
    background = BentoBackground,
    surface = Color.White,
    error = BentoAlertAccent,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = BentoTextPrimary,
    onSurface = BentoTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamic color to preserve our custom premiumCargo branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
