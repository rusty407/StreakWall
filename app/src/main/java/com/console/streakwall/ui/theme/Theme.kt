package com.console.streakwall.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = SeedPrimaryLight,
    onPrimary = SeedOnPrimaryLight,
    primaryContainer = SeedPrimaryContainerLight,
    secondary = SeedSecondaryLight,
    background = SeedBackgroundLight,
    surface = SeedSurfaceLight,
    onSurface = SeedOnSurfaceLight
)

private val DarkColors = darkColorScheme(
    primary = SeedPrimaryDark,
    onPrimary = SeedOnPrimaryDark,
    primaryContainer = SeedPrimaryContainerDark,
    secondary = SeedSecondaryDark,
    background = SeedBackgroundDark,
    surface = SeedSurfaceDark,
    onSurface = SeedOnSurfaceDark
)

@Composable
fun StreakWallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color (Material You) is only available on Android 12+.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StreakWallTypography,
        content = content
    )
}
