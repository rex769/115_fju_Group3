package com.example.smartfridge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSeniorMode = staticCompositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = AccentOrange,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = SurfaceLight,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun SmartFridgeTheme(
    isSeniorMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val fontScale = if (isSeniorMode) 1.35f else 1.0f
    val typography = getScaledTypography(fontScale)

    CompositionLocalProvider(LocalSeniorMode provides isSeniorMode) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = typography,
            content = content
        )
    }
}
