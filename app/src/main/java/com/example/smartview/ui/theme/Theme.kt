package com.example.smartview.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Field-ready Dark Scheme: Default and optimized for field visibility, contrast and battery preservation
private val SmartViewDarkColorScheme = darkColorScheme(
    primary = PrecisionSkyLight,
    onPrimary = Slate950,
    primaryContainer = PrecisionSkyContainer,
    onPrimaryContainer = PrecisionSkyLight,
    secondary = ConstructionAmber,
    onSecondary = Slate950,
    secondaryContainer = ConstructionAmberContainer,
    onSecondaryContainer = ConstructionAmber,
    tertiary = PrecisionEmerald,
    onTertiary = Slate950,
    background = Slate950,
    onBackground = Slate50,
    surface = Slate900,
    onSurface = Slate50,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = GridBorder,
    outlineVariant = GridBorderSubtle
)

private val SmartViewLightColorScheme = lightColorScheme(
    primary = PrecisionSkyDark,
    onPrimary = Slate50,
    primaryContainer = PrecisionSkyLight.copy(alpha = 0.2f),
    onPrimaryContainer = PrecisionSkyDark,
    secondary = ConstructionAmberDark,
    onSecondary = Slate50,
    secondaryContainer = ConstructionAmber.copy(alpha = 0.2f),
    onSecondaryContainer = ConstructionAmberDark,
    tertiary = PrecisionEmerald,
    onTertiary = Slate50,
    background = Slate50,
    onBackground = Slate900,
    surface = Color(0xFFFFFFFF),
    onSurface = Slate900,
    surfaceVariant = Slate200,
    onSurfaceVariant = Slate700,
    outline = Slate400,
    outlineVariant = Slate200
)

@Composable
fun SmartViewTheme(
    darkTheme: Boolean = true, // Default to field dark theme for technical instrument styling
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SmartViewDarkColorScheme else SmartViewLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SmartViewTypography,
        content = content
    )
}
