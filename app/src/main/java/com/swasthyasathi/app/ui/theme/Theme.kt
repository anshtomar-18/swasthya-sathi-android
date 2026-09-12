package com.swasthyasathi.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = SurfaceContainerLowest,
    primaryContainer = PrimaryTealContainer,
    onPrimaryContainer = PrimaryTealFixed,
    secondary = SecondaryCoral,
    onSecondary = SurfaceContainerLowest,
    secondaryContainer = SecondaryCoralContainer,
    onSecondaryContainer = SecondaryCoralFixed,
    tertiary = TertiaryNavy,
    surface = AppSurface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerLow,
    onSurfaceVariant = OnSurfaceVariant,
    outline = AppOutline
)

@Composable
fun SwasthyaSathiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
