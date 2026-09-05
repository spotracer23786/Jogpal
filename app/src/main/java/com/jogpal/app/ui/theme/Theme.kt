package com.jogpal.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = JogpalPrimary,
    secondary = JogpalSecondary,
    tertiary = JogpalTertiary,
    background = JogpalBackgroundDark,
    surface = JogpalSurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = JogpalOnBackgroundDark,
    onSurface = JogpalOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = JogpalPrimary,
    secondary = JogpalSecondary,
    tertiary = JogpalTertiary,
    background = JogpalBackgroundLight,
    surface = JogpalSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = JogpalOnBackgroundLight,
    onSurface = JogpalOnSurfaceLight,
    surfaceVariant = JogpalPillBgLight,
    outline = JogpalCardBorderLight
)


@Composable
fun JogpalTheme(
    darkTheme: Boolean = true, // Default to sleek black background and green accents
    dynamicColor: Boolean = false, // Prefer our custom health tracking theme tokens
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}