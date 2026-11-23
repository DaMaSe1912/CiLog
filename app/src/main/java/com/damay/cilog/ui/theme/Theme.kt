package com.damay.cilog.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CinematicPrimary,
    onPrimary = Color.White,
    secondary = CinematicSecondary,
    onSecondary = Color.Black,
    tertiary = CinematicRatingGold,
    background = CinematicDark,
    onBackground = CinematicTextPrimaryDark,
    surface = CinematicSurfaceDark,
    onSurface = CinematicTextPrimaryDark,
    surfaceVariant = CinematicSurfaceVariantDark,
    onSurfaceVariant = CinematicTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = CinematicPrimaryLight,
    onPrimary = Color.White,
    secondary = CinematicSecondary,
    onSecondary = Color.Black,
    tertiary = CinematicRatingGold,
    background = CinematicLight,
    onBackground = CinematicTextPrimaryLight,
    surface = CinematicSurfaceLight,
    onSurface = CinematicTextPrimaryLight,
    surfaceVariant = CinematicSurfaceVariantLight,
    onSurfaceVariant = CinematicTextSecondaryLight
)

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguageMode(val code: String, val apiCode: String) {
    SYSTEM("system", "en-US"),
    EN("en", "en-US"),
    ID("in", "id-ID")
}

@Composable
fun CilogTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK, // Streaming apps default to rich dark aesthetic
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
