@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.theme.color

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import `in`.hridayan.ashell.core.common.LocalPaletteStyle
import `in`.hridayan.ashell.core.domain.provider.SeedColorProvider

@Composable
fun lightColorSchemeFromSeed(): ColorScheme {
    val primary = SeedColorProvider.primary
    val paletteStyle = LocalPaletteStyle.current
    val scheme =
        createDynamicScheme(primarySeedArgb = primary, paletteStyle = paletteStyle, isDark = false)

    return scheme.toComposeColorScheme()
}

@Composable
fun darkColorSchemeFromSeed(): ColorScheme {
    val primary = SeedColorProvider.primary
    val paletteStyle = LocalPaletteStyle.current
    val scheme =
        createDynamicScheme(primarySeedArgb = primary, paletteStyle = paletteStyle, isDark = true)

    return scheme.toComposeColorScheme()
}

@SuppressLint("RestrictedApi")
@Composable
fun highContrastDarkColorSchemeFromSeed(): ColorScheme {
    val primary = SeedColorProvider.primary
    val paletteStyle = LocalPaletteStyle.current
    val scheme = createDynamicScheme(
        primarySeedArgb = primary,
        paletteStyle = paletteStyle,
        isDark = true,
        contrastLevel = 1.0
    )

    return scheme.toComposeColorScheme().copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color(scheme.neutralVariantPalette.tone(6)),
        surfaceContainer = Color(scheme.neutralVariantPalette.tone(10)),
        surfaceContainerHigh = Color(scheme.neutralVariantPalette.tone(12)),
        surfaceContainerHighest = Color(scheme.neutralVariantPalette.tone(17)),
    )
}

@RequiresApi(Build.VERSION_CODES.S)
fun highContrastDynamicDarkColorScheme(context: Context): ColorScheme =
    with(dynamicDarkColorScheme(context)) {
        return this.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = surfaceContainerLowest,
            surfaceContainer = surfaceContainerLow,
            surfaceContainerHigh = surfaceContainer,
            surfaceContainerHighest = surfaceContainerHigh,
        )
    }

@Composable
fun Color.harmonizeWithPrimary(
    @FloatRange(from = 0.0, to = 1.0) fraction: Float = 0.1f
): Color = blend(MaterialTheme.colorScheme.primary, fraction)


fun Color.blend(
    color: Color,
    @FloatRange(from = 0.0, to = 1.0) fraction: Float = 0.1f
): Color = Color(ColorUtils.blendARGB(this.toArgb(), color.toArgb(), fraction))

fun colorLerp(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)

    val startRed = start.red
    val startGreen = start.green
    val startBlue = start.blue
    val startAlpha = start.alpha

    val endRed = end.red
    val endGreen = end.green
    val endBlue = end.blue
    val endAlpha = end.alpha

    return Color(
        red = startRed + (endRed - startRed) * f,
        green = startGreen + (endGreen - startGreen) * f,
        blue = startBlue + (endBlue - startBlue) * f,
        alpha = startAlpha + (endAlpha - startAlpha) * f
    )
}
