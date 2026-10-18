package com.example.ui.theme

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

private val CockpitDarkColorScheme = darkColorScheme(
    primary = NavRouteCyan,
    onPrimary = Color.Black,
    primaryContainer = CockpitSurfaceVariant,
    onPrimaryContainer = CockpitTextPrimary,
    secondary = NavGlowBlue,
    onSecondary = Color.Black,
    surface = CockpitSurface,
    onSurface = CockpitTextPrimary,
    surfaceVariant = CockpitSurfaceVariant,
    onSurfaceVariant = CockpitTextSecondary,
    background = CockpitBackground,
    onBackground = CockpitTextPrimary,
    error = SpeedDanger,
    onError = Color.White
)

private val CockpitLightColorScheme = lightColorScheme(
    primary = NavRouteBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    error = SpeedDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Automotive dark mode for cockpit glanceability
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CockpitDarkColorScheme else CockpitLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                it.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
