@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.theme

import android.content.Context
import android.os.Build
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import `in`.hridayan.ashell.core.presentation.utils.a1
import `in`.hridayan.ashell.core.presentation.utils.a2
import `in`.hridayan.ashell.core.presentation.utils.a3
import `in`.hridayan.ashell.core.presentation.utils.n1
import `in`.hridayan.ashell.core.presentation.utils.n2

@Composable
fun lightColorSchemeFromSeed(): ColorScheme {
    return expressiveLightColorScheme().copy(
        primary = 40.a1,
        primaryContainer = 90.a1,
        onPrimary = 100.a1,
        onPrimaryContainer = 10.a1,
        inversePrimary = 80.a1,

        secondary = 40.a2.harmonizeWithPrimary(0.1f),
        secondaryContainer = 90.a2.harmonizeWithPrimary(0.1f),
        onSecondary = 100.a2.harmonizeWithPrimary(0.1f),
        onSecondaryContainer = 10.a2.harmonizeWithPrimary(0.1f),

        tertiary = 40.a3.harmonizeWithPrimary(0.1f),
        tertiaryContainer = 90.a3.harmonizeWithPrimary(0.1f),
        onTertiary = 100.a3.harmonizeWithPrimary(0.1f),
        onTertiaryContainer = 10.a3.harmonizeWithPrimary(0.1f),

        background = 98.n1,
        onBackground = 10.n1,

        surface = 98.n1,
        onSurface = 10.n1,
        surfaceVariant = 90.n2,
        onSurfaceVariant = 30.n2,
        surfaceDim = 87.n1,
        surfaceBright = 98.n1,
        surfaceContainerLowest = 100.n1,
        surfaceContainerLow = 96.n1,
        surfaceContainer = 94.n1,
        surfaceContainerHigh = 92.n1,
        surfaceContainerHighest = 90.n1,
        inverseSurface = 20.n1,
        inverseOnSurface = 95.n1,

        outline = 50.n2,
        outlineVariant = 80.n2,
    )
}

@Composable
fun darkColorSchemeFromSeed(): ColorScheme {
    return darkColorScheme(
        primary = 80.a1,
        primaryContainer = 30.a1,
        onPrimary = 20.a1,
        onPrimaryContainer = 90.a1,
        inversePrimary = 40.a1,

        secondary = 80.a2.harmonizeWithPrimary(0.1f),
        secondaryContainer = 30.a2.harmonizeWithPrimary(0.1f),
        onSecondary = 20.a2.harmonizeWithPrimary(0.1f),
        onSecondaryContainer = 90.a2.harmonizeWithPrimary(0.1f),

        tertiary = 80.a3.harmonizeWithPrimary(0.1f),
        tertiaryContainer = 30.a3.harmonizeWithPrimary(0.1f),
        onTertiary = 20.a3.harmonizeWithPrimary(0.1f),
        onTertiaryContainer = 90.a3.harmonizeWithPrimary(0.1f),

        background = 6.n1,
        onBackground = 90.n1,

        surface = 6.n1,
        onSurface = 90.n1,
        surfaceVariant = 30.n2,
        onSurfaceVariant = 80.n2,
        surfaceDim = 6.n1,
        surfaceBright = 24.n1,
        surfaceContainerLowest = 4.n1,
        surfaceContainerLow = 10.n1,
        surfaceContainer = 12.n1,
        surfaceContainerHigh = 17.n1,
        surfaceContainerHighest = 22.n1,
        inverseSurface = 90.n1,
        inverseOnSurface = 20.n1,

        outline = 60.n2,
        outlineVariant = 30.n2,
    )
}

@Composable
fun highContrastDarkColorSchemeFromSeed(): ColorScheme {
    return darkColorSchemeFromSeed().copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = 6.n1,
        surfaceContainer = 10.n1,
        surfaceContainerHigh = 12.n1,
        surfaceContainerHighest = 17.n1,
    )
}

@RequiresApi(Build.VERSION_CODES.S)
fun highContrastDynamicDarkColorScheme(context: Context): ColorScheme {
    return dynamicDarkColorScheme(context = context).copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = dynamicDarkColorScheme(context).surfaceContainerLowest,
        surfaceContainer = dynamicDarkColorScheme(context).surfaceContainerLow,
        surfaceContainerHigh = dynamicDarkColorScheme(context).surfaceContainer,
        surfaceContainerHighest = dynamicDarkColorScheme(context).surfaceContainerHigh,
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
