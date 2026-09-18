package com.console.streakwall.data

import androidx.compose.ui.graphics.Color

/**
 * A wallpaper color theme the user can pick. This drives both the generated wallpaper
 * bitmap's colors (see wallpaper/WallpaperGenerator.kt) and the swatch shown to preview it
 * in Onboarding/Settings. It is independent of the app's own Material3 UI theme, which
 * follows system dynamic color / dark mode instead.
 */
enum class WallpaperTheme(
    val displayName: String,
    val background: Color,
    val backgroundSecondary: Color,
    val accent: Color,
    val onBackground: Color
) {
    MIDNIGHT(
        displayName = "Midnight",
        background = Color(0xFF0F1021),
        backgroundSecondary = Color(0xFF1B1B3A),
        accent = Color(0xFF7C9CFF),
        onBackground = Color(0xFFF5F5FF)
    ),
    FOREST(
        displayName = "Forest",
        background = Color(0xFF0E2B1E),
        backgroundSecondary = Color(0xFF163B29),
        accent = Color(0xFF4ADE80),
        onBackground = Color(0xFFF0FFF6)
    ),
    SUNSET(
        displayName = "Sunset",
        background = Color(0xFF2B1220),
        backgroundSecondary = Color(0xFF411A2C),
        accent = Color(0xFFFF8A5B),
        onBackground = Color(0xFFFFF3EC)
    ),
    MONOCHROME(
        displayName = "Monochrome",
        background = Color(0xFF141414),
        backgroundSecondary = Color(0xFF222222),
        accent = Color(0xFFE5E5E5),
        onBackground = Color(0xFFFAFAFA)
    );

    companion object {
        val Default = MIDNIGHT

        fun fromName(name: String?): WallpaperTheme =
            entries.firstOrNull { it.name == name } ?: Default
    }
}
