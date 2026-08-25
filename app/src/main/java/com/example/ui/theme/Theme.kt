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

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF003258),
    primaryContainer = CyberCyan,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = NeonOrange,
    onSecondary = Color(0xFF4A1800),
    secondaryContainer = Color(0xFF7A2700),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = BrightAmber,
    onTertiary = Color(0xFF422B00),
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkSurfaceBorder,
    outlineVariant = Color(0xFF1E293B),
    error = TerminalRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CyberCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = NeonOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF380D00),
    tertiary = BrightAmber,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = TerminalRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent cyber branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Default to sleek dark cyber look for high-tech SMS blaster experience
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
