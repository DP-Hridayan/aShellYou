package `in`.hridayan.ashell.core.presentation.theme.color

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import `in`.hridayan.ashell.core.presentation.theme.domain.model.UserGeneratedColorScheme

fun UserGeneratedColorScheme.toColorScheme(): ColorScheme {
    fun parseColor(hex: String): Color {
        return try {
            val hex = if (hex.startsWith("#")) hex else "#$hex"
            Color(hex.toColorInt())
        } catch (e: Exception) {
            Color.Transparent // Fallback
        }
    }

    return darkColorScheme(
        primary = parseColor(primary),
        onPrimary = parseColor(onPrimary),
        primaryContainer = parseColor(primaryContainer),
        onPrimaryContainer = parseColor(onPrimaryContainer),
        inversePrimary = parseColor(inversePrimary),
        secondary = parseColor(secondary),
        onSecondary = parseColor(onSecondary),
        secondaryContainer = parseColor(secondaryContainer),
        onSecondaryContainer = parseColor(onSecondaryContainer),
        tertiary = parseColor(tertiary),
        onTertiary = parseColor(onTertiary),
        tertiaryContainer = parseColor(tertiaryContainer),
        onTertiaryContainer = parseColor(onTertiaryContainer),
        error = parseColor(error),
        onError = parseColor(onError),
        errorContainer = parseColor(errorContainer),
        onErrorContainer = parseColor(onErrorContainer),
        background = parseColor(background),
        onBackground = parseColor(onBackground),
        surface = parseColor(surface),
        onSurface = parseColor(onSurface),
        surfaceVariant = parseColor(surfaceVariant),
        onSurfaceVariant = parseColor(onSurfaceVariant),
        surfaceTint = parseColor(surfaceTint),
        inverseSurface = parseColor(inverseSurface),
        inverseOnSurface = parseColor(inverseOnSurface),
        outline = parseColor(outline),
        outlineVariant = parseColor(outlineVariant),
        scrim = parseColor(scrim),
        surfaceBright = parseColor(surfaceBright),
        surfaceDim = parseColor(surfaceDim),
        surfaceContainer = parseColor(surfaceContainer),
        surfaceContainerHigh = parseColor(surfaceContainerHigh),
        surfaceContainerHighest = parseColor(surfaceContainerHighest),
        surfaceContainerLow = parseColor(surfaceContainerLow),
        surfaceContainerLowest = parseColor(surfaceContainerLowest)
    )
}
