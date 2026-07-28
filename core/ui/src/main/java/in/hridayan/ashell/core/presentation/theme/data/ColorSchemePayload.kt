package `in`.hridayan.ashell.core.presentation.theme.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ColorSchemePayload(
    val name: String,
    // Primary
    val primary: String,
    val onPrimary: String,
    val primaryContainer: String,
    val onPrimaryContainer: String,
    val inversePrimary: String,
    // Secondary
    val secondary: String,
    val onSecondary: String,
    val secondaryContainer: String,
    val onSecondaryContainer: String,
    // Tertiary
    val tertiary: String,
    val onTertiary: String,
    val tertiaryContainer: String,
    val onTertiaryContainer: String,
    // Error
    val error: String,
    val onError: String,
    val errorContainer: String,
    val onErrorContainer: String,
    // Surface
    val background: String,
    val onBackground: String,
    val surface: String,
    val onSurface: String,
    val surfaceVariant: String,
    val onSurfaceVariant: String,
    val surfaceTint: String,
    val inverseSurface: String,
    val inverseOnSurface: String,
    val surfaceBright: String,
    val surfaceDim: String,
    val surfaceContainer: String,
    val surfaceContainerHigh: String,
    val surfaceContainerHighest: String,
    val surfaceContainerLow: String,
    val surfaceContainerLowest: String,
    val svgPathData: String = "",
    // Misc
    val outline: String,
    val outlineVariant: String,
    val scrim: String,
    // Versioning
    val version: Int = 1,
    // Theme Type
    val isDarkTheme: Boolean = false
)
