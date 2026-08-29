package com.ontheroad.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ontheroad.core.model.ThemeMode

// Cockpit Dark Palette (OLED high-contrast for day/night driving)
private val DarkColorScheme = darkColorScheme(
    primary = BrandEmeraldLight,
    onPrimary = Color.Black,
    primaryContainer = BrandEmeraldDark,
    onPrimaryContainer = BrandEmeraldLight,
    secondary = GreenProfit,
    onSecondary = Color.Black,
    background = RoadNavyDark,
    onBackground = OnSurfaceWhite,
    surface = RoadNavySurface,
    onSurface = OnSurfaceWhite,
    surfaceVariant = RoadNavySurfaceVariant,
    onSurfaceVariant = OnSurfaceSecondary,
    error = RedDiscrepancy
)

// Daytime High-Contrast Light Palette
private val LightColorScheme = lightColorScheme(
    primary = BrandEmerald,
    onPrimary = Color.White,
    primaryContainer = BrandEmeraldLight,
    onPrimaryContainer = BrandEmeraldDark,
    secondary = GreenProfit,
    onSecondary = Color.White,
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    error = RedDiscrepancy
)

@Composable
fun OnTheRoadTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun OnTheRoadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
