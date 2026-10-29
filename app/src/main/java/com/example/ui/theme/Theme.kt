package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealPrimaryLight,
    onPrimaryContainer = TealDark,
    secondary = SaffronSecondary,
    onSecondary = Color.White,
    secondaryContainer = SaffronLight,
    onSecondaryContainer = Color(0xFF78350F),
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,
    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerRedLight,
    onErrorContainer = DangerRed
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4FD1C5),
    onPrimary = Color(0xFF003833),
    primaryContainer = Color(0xFF0F5257),
    onPrimaryContainer = Color(0xFFD4ECE9),
    secondary = Color(0xFFF6AD55),
    onSecondary = Color(0xFF4A2800),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // false to preserve high-contrast accessible brand palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
