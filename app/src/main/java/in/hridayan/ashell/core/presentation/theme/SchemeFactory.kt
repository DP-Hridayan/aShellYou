@file:SuppressLint("RestrictedApi")

package `in`.hridayan.ashell.core.presentation.theme

import android.annotation.SuppressLint
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.SchemeContent
import com.google.android.material.color.utilities.SchemeExpressive
import com.google.android.material.color.utilities.SchemeFidelity
import com.google.android.material.color.utilities.SchemeFruitSalad
import com.google.android.material.color.utilities.SchemeMonochrome
import com.google.android.material.color.utilities.SchemeNeutral
import com.google.android.material.color.utilities.SchemeRainbow
import com.google.android.material.color.utilities.SchemeTonalSpot
import com.google.android.material.color.utilities.SchemeVibrant
import `in`.hridayan.ashell.core.domain.model.PaletteStyle

/**
 * Creates a [DynamicScheme] from a primary seed color and palette style.
 * This is the single source of truth for all color scheme generation.
 */
fun createDynamicScheme(
    primarySeedArgb: Int,
    paletteStyle: PaletteStyle,
    isDark: Boolean,
    contrastLevel: Double = 0.0
): DynamicScheme {
    val hct = Hct.fromInt(primarySeedArgb)
    return when (paletteStyle) {
        PaletteStyle.TONAL_SPOT -> SchemeTonalSpot(hct, isDark, contrastLevel)
        PaletteStyle.EXPRESSIVE -> SchemeExpressive(hct, isDark, contrastLevel)
        PaletteStyle.VIBRANT -> SchemeVibrant(hct, isDark, contrastLevel)
        PaletteStyle.MONOCHROME -> SchemeMonochrome(hct, isDark, contrastLevel)
        PaletteStyle.RAINBOW -> SchemeRainbow(hct, isDark, contrastLevel)
        PaletteStyle.FRUIT_SALAD -> SchemeFruitSalad(hct, isDark, contrastLevel)
        PaletteStyle.NEUTRAL -> SchemeNeutral(hct, isDark, contrastLevel)
        PaletteStyle.FIDELITY -> SchemeFidelity(hct, isDark, contrastLevel)
        PaletteStyle.CONTENT -> SchemeContent(hct, isDark, contrastLevel)
    }
}

/**
 * Extracts the 3 palette key colors (primary, secondary, tertiary) at tone 40
 * for display purposes (e.g. the palette wheel). These are NOT affected by
 * light/dark mode — they are always the same "representative" mid-tone colors.
 */
data class PaletteKeyColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
)

fun getPaletteKeyColors(primarySeedArgb: Int, paletteStyle: PaletteStyle): PaletteKeyColors {
    val scheme = createDynamicScheme(primarySeedArgb, paletteStyle, isDark = false)
    return PaletteKeyColors(
        primary = modifyColorForDisplay(Color(scheme.primaryPalette.tone(60)), toneFactor = 1f),
        secondary = modifyColorForDisplay(Color(scheme.secondaryPalette.tone(60)), toneFactor = 1.4f),
        tertiary = modifyColorForDisplay(Color(scheme.tertiaryPalette.tone(60)), toneFactor = 0.7f),
    )
}

/**
 * Adjusts a color's brightness and saturation for better visual appearance
 * in the palette wheel display. This keeps the display colors vibrant
 * and distinguishable regardless of the raw tone values.
 */
private fun modifyColorForDisplay(
    color: Color,
    toneFactor: Float = 1.4f,
    chromaFactor: Float = 1.15f
): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    hsv[1] = (hsv[1] * chromaFactor).coerceIn(0f, 1f)
    hsv[2] = (hsv[2] * toneFactor).coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

/**
 * Converts a [DynamicScheme] into a Compose [ColorScheme].
 * Extracts all 30+ color roles from the tonal palettes.
 */
fun DynamicScheme.toComposeColorScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()

    return base.copy(
        primary = Color(primaryPalette.tone(if (isDark) 80 else 40)),
        onPrimary = Color(primaryPalette.tone(if (isDark) 20 else 100)),
        primaryContainer = Color(primaryPalette.tone(if (isDark) 30 else 90)),
        onPrimaryContainer = Color(primaryPalette.tone(if (isDark) 90 else 10)),
        inversePrimary = Color(primaryPalette.tone(if (isDark) 40 else 80)),

        secondary = Color(secondaryPalette.tone(if (isDark) 80 else 40)),
        onSecondary = Color(secondaryPalette.tone(if (isDark) 20 else 100)),
        secondaryContainer = Color(secondaryPalette.tone(if (isDark) 30 else 90)),
        onSecondaryContainer = Color(secondaryPalette.tone(if (isDark) 90 else 10)),

        tertiary = Color(tertiaryPalette.tone(if (isDark) 80 else 40)),
        onTertiary = Color(tertiaryPalette.tone(if (isDark) 20 else 100)),
        tertiaryContainer = Color(tertiaryPalette.tone(if (isDark) 30 else 90)),
        onTertiaryContainer = Color(tertiaryPalette.tone(if (isDark) 90 else 10)),

        error = Color(errorPalette.tone(if (isDark) 80 else 40)),
        onError = Color(errorPalette.tone(if (isDark) 20 else 100)),
        errorContainer = Color(errorPalette.tone(if (isDark) 30 else 90)),
        onErrorContainer = Color(errorPalette.tone(if (isDark) 90 else 10)),

        background = Color(neutralPalette.tone(if (isDark) 6 else 98)),
        onBackground = Color(neutralPalette.tone(if (isDark) 90 else 10)),

        surface = Color(neutralPalette.tone(if (isDark) 6 else 98)),
        onSurface = Color(neutralPalette.tone(if (isDark) 90 else 10)),
        surfaceVariant = Color(neutralVariantPalette.tone(if (isDark) 30 else 90)),
        onSurfaceVariant = Color(neutralVariantPalette.tone(if (isDark) 80 else 30)),

        surfaceDim = Color(neutralPalette.tone(if (isDark) 6 else 87)),
        surfaceBright = Color(neutralPalette.tone(if (isDark) 24 else 98)),
        surfaceContainerLowest = Color(neutralVariantPalette.tone(if (isDark) 4 else 100)),
        surfaceContainerLow = Color(neutralVariantPalette.tone(if (isDark) 10 else 96)),
        surfaceContainer = Color(neutralVariantPalette.tone(if (isDark) 12 else 94)),
        surfaceContainerHigh = Color(neutralVariantPalette.tone(if (isDark) 17 else 92)),
        surfaceContainerHighest = Color(neutralVariantPalette.tone(if (isDark) 22 else 90)),

        inverseSurface = Color(neutralPalette.tone(if (isDark) 90 else 20)),
        inverseOnSurface = Color(neutralPalette.tone(if (isDark) 20 else 95)),

        outline = Color(neutralVariantPalette.tone(if (isDark) 60 else 50)),
        outlineVariant = Color(neutralVariantPalette.tone(if (isDark) 30 else 80)),
    )
}
