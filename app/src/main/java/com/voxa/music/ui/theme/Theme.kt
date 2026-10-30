package com.voxa.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VoxaPrimary,
    onPrimary = VoxaTextPrimary,
    background = VoxaBackground,
    onBackground = VoxaTextPrimary,
    surface = VoxaSurface,
    onSurface = VoxaTextPrimary,
    surfaceVariant = VoxaSurfaceVariant,
    onSurfaceVariant = VoxaTextSecondary
)

@Composable
fun VoxaMusicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
