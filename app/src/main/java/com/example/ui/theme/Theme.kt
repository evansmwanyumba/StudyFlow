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
    primary = BluePrimaryDark,
    onPrimary = Color(0xFF002F6C),
    primaryContainer = BluePrimaryContainerDark,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = TealSecondaryDark,
    onSecondary = Color(0xFF003831),
    secondaryContainer = TealSecondaryContainerDark,
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = AmberTertiaryDark,
    onTertiary = Color(0xFF452B00),
    background = SlateBackgroundDark,
    onBackground = Color(0xFFE2E8F0),
    surface = SlateSurfaceDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = SlateSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = SlateOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = TealSecondary,
    onSecondary = Color.White,
    secondaryContainer = TealSecondaryContainer,
    onSecondaryContainer = Color(0xFF134E4A),
    tertiary = AmberTertiary,
    onTertiary = Color.White,
    background = SlateBackground,
    onBackground = Color(0xFF0F172A),
    surface = SlateSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = SlateOutline
)

@Composable
fun StudyFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to prioritize our polished cohesive brand design
    content: @Composable () -> Unit
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
