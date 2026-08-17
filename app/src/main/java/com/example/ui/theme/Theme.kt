package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LuqaColorScheme = lightColorScheme(
    primary = LuqaPrimary,
    onPrimary = LuqaOnPrimary,
    primaryContainer = LuqaPrimaryContainer,
    onPrimaryContainer = LuqaOnPrimaryContainer,
    secondary = LuqaSecondary,
    onSecondary = LuqaOnSecondary,
    secondaryContainer = LuqaSecondaryContainer,
    onSecondaryContainer = LuqaOnSecondaryContainer,
    background = LuqaBackground,
    onBackground = LuqaOnSurface,
    surface = LuqaSurface,
    onSurface = LuqaOnSurface,
    surfaceVariant = LuqaSurfaceContainerHighest,
    onSurfaceVariant = LuqaOnSurfaceVariant,
    outline = LuqaOutline,
    outlineVariant = LuqaOutlineVariant,
    error = LuqaErrorRed,
    errorContainer = LuqaErrorContainer
)

@Composable
fun LuqaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LuqaColorScheme,
        typography = Typography,
        content = content
    )
}

