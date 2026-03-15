package com.example.academicleveling.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Teal,
    secondary = Accent,
    tertiary = Gold,
    background = BgPrimary,
    surface = BgCard,
    onPrimary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun AcademicLevelingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}