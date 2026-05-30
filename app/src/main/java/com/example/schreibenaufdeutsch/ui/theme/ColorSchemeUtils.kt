package com.example.schreibenaufdeutsch.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Creates a readable ColorScheme derived from a seed color.
 */
fun colorSchemeFromSeed(seedColor: Color, isDark: Boolean): ColorScheme {
    // Ensure the seed color is readable in its respective mode
    val adjustedSeed = if (isDark) {
        // If luminance is too low, make it lighter for dark mode
        if (seedColor.luminance() < 0.3f) {
            Color(
                red = (seedColor.red * 0.5f + 0.5f).coerceIn(0f, 1f),
                green = (seedColor.green * 0.5f + 0.5f).coerceIn(0f, 1f),
                blue = (seedColor.blue * 0.5f + 0.5f).coerceIn(0f, 1f)
            )
        } else seedColor
    } else {
        // If luminance is too high, make it slightly darker for light mode readability
        if (seedColor.luminance() > 0.7f) {
            Color(
                red = (seedColor.red * 0.8f).coerceIn(0f, 1f),
                green = (seedColor.green * 0.8f).coerceIn(0f, 1f),
                blue = (seedColor.blue * 0.8f).coerceIn(0f, 1f)
            )
        } else seedColor
    }

    return if (isDark) {
        DarkColorScheme.copy(
            primary = adjustedSeed,
            onPrimary = Color.Black,
            primaryContainer = adjustedSeed.copy(alpha = 0.4f),
            onPrimaryContainer = Color.White,
            secondary = adjustedSeed.copy(alpha = 0.9f),
            outline = adjustedSeed.copy(alpha = 0.7f),
            surface = Color(0xFF121212), // Deeper black for better depth
            onSurface = Color.White,
            surfaceVariant = Color(0xFF1E1E1E),
            onSurfaceVariant = Color.White.copy(alpha = 0.8f)
        )
    } else {
        LightColorScheme.copy(
            primary = adjustedSeed,
            primaryContainer = adjustedSeed.copy(alpha = 0.15f),
            onPrimaryContainer = adjustedSeed,
            secondary = adjustedSeed.copy(alpha = 0.8f),
            outline = adjustedSeed.copy(alpha = 0.5f),
            surface = Color.White,
            onSurface = Color.Black,
            surfaceVariant = Color(0xFFF5F5F5)
        )
    }
}
