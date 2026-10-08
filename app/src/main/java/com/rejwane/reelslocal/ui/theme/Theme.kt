package com.rejwane.reelslocal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.rejwane.reelslocal.data.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = AccentPink,
    onPrimary = DarkBackground,
    primaryContainer = AccentPinkDark,
    onPrimaryContainer = DarkOnSurface,
    secondary = AccentCyan,
    onSecondary = DarkBackground,
    secondaryContainer = AccentCyanDark,
    onSecondaryContainer = DarkOnSurface,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = AccentPinkDark,
    onPrimary = LightSurface,
    primaryContainer = AccentPink,
    onPrimaryContainer = LightSurface,
    secondary = AccentCyanDark,
    onSecondary = LightSurface,
    secondaryContainer = AccentCyan,
    onSecondaryContainer = LightOnSurface,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = ErrorRed
)

@Composable
fun ReelsLocalTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColorScheme else LightColorScheme,
        typography = ReelsLocalTypography,
        content = content
    )
}
