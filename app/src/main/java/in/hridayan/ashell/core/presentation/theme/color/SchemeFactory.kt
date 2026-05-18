@file:SuppressLint("RestrictedApi")

package `in`.hridayan.ashell.core.presentation.theme.color

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
import com.google.android.material.color.utilities.TonalPalette
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
 * Extracts the 3 palette key colors (primary, secondary, tertiary) at tone 60
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
        secondary = modifyColorForDisplay(
            Color(scheme.secondaryPalette.tone(60)),
            toneFactor = 1.4f
        ),
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
        primary = primaryPalette.resolve(80, 40, isDark),
        onPrimary = primaryPalette.resolve(20, 100, isDark),
        primaryContainer = primaryPalette.resolve(30, 90, isDark),
        onPrimaryContainer = primaryPalette.resolve(90, 10, isDark),
        inversePrimary = primaryPalette.resolve(40, 80, isDark),

        secondary = secondaryPalette.resolve(80, 40, isDark),
        onSecondary = secondaryPalette.resolve(20, 100, isDark),
        secondaryContainer = secondaryPalette.resolve(30, 90, isDark),
        onSecondaryContainer = secondaryPalette.resolve(90, 10, isDark),

        tertiary = tertiaryPalette.resolve(80, 40, isDark),
        onTertiary = tertiaryPalette.resolve(20, 100, isDark),
        tertiaryContainer = tertiaryPalette.resolve(30, 90, isDark),
        onTertiaryContainer = tertiaryPalette.resolve(90, 10, isDark),

        error = errorPalette.resolve(80, 40, isDark),
        onError = errorPalette.resolve(20, 100, isDark),
        errorContainer = errorPalette.resolve(30, 90, isDark),
        onErrorContainer = errorPalette.resolve(90, 10, isDark),

        background = neutralPalette.resolve(6, 98, isDark),
        onBackground = neutralPalette.resolve(90, 10, isDark),

        surface = neutralPalette.resolve(6, 98, isDark),
        onSurface = neutralPalette.resolve(90, 10, isDark),

        surfaceVariant = neutralVariantPalette.resolve(30, 90, isDark),
        onSurfaceVariant = neutralVariantPalette.resolve(80, 30, isDark),

        surfaceDim = neutralPalette.resolve(6, 87, isDark),
        surfaceBright = neutralPalette.resolve(24, 98, isDark),

        surfaceContainerLowest = neutralVariantPalette.resolve(4, 100, isDark),
        surfaceContainerLow = neutralVariantPalette.resolve(10, 96, isDark),
        surfaceContainer = neutralVariantPalette.resolve(12, 94, isDark),
        surfaceContainerHigh = neutralVariantPalette.resolve(17, 92, isDark),
        surfaceContainerHighest = neutralVariantPalette.resolve(22, 90, isDark),

        inverseSurface = neutralPalette.resolve(90, 20, isDark),
        inverseOnSurface = neutralPalette.resolve(20, 95, isDark),

        outline = neutralVariantPalette.resolve(60, 50, isDark),
        outlineVariant = neutralVariantPalette.resolve(30, 80, isDark),
    )
}

private fun TonalPalette.resolve(
    darkTone: Int,
    lightTone: Int,
    isDark: Boolean
): Color {
    return Color(
        tone(
            if (isDark) darkTone
            else lightTone
        )
    )
}