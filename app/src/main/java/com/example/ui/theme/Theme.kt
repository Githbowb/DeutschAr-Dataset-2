package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAccent,
    onPrimary = DarkBg,
    primaryContainer = DarkCardBg,
    onPrimaryContainer = TextMain,
    secondary = SecondaryAccent,
    onSecondary = Color.White,
    secondaryContainer = DarkCardBg,
    onSecondaryContainer = TextMain,
    tertiary = AmberAccent,
    background = DarkBg,
    onBackground = TextMain,
    surface = DarkCardBg,
    onSurface = TextMain,
    surfaceVariant = DarkSubCardBg,
    onSurfaceVariant = TextMuted,
    outline = DarkCardBorder,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun DeutschArabischTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Always use the refined DarkColorScheme to match the dark mockup design
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    DeutschArabischTheme(darkTheme = darkTheme, content = content)
}

