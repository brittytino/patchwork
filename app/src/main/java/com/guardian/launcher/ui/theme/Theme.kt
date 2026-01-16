package com.guardian.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Guardian Launcher Color Scheme
private val LightColors = lightColorScheme(
    primary = Color(0xFF4A90E2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E9FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535E71),
    onSecondary = Color.White,
    error = Color(0xFFBA1A1A),
    background = Color(0xFFFDFCFF),
    surface = Color(0xFFFDFCFF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DCAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD6E9FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF263141),
    error = Color(0xFFFFB4AB),
    background = Color(0xFF1A1C1E),
    surface = Color(0xFF1A1C1E),
)

@Composable
fun GuardianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
