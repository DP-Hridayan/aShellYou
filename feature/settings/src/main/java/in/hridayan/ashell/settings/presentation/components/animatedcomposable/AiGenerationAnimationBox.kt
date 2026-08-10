package `in`.hridayan.ashell.settings.presentation.components.animatedcomposable

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalUserGeneratedColorScheme
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.theme.AshellYouTheme
import `in`.hridayan.ashell.core.presentation.theme.color.darkColorSchemeFromSeed
import `in`.hridayan.ashell.core.presentation.theme.color.highContrastDarkColorSchemeFromSeed
import `in`.hridayan.ashell.core.presentation.theme.color.highContrastDynamicDarkColorScheme
import `in`.hridayan.ashell.core.presentation.theme.color.lightColorSchemeFromSeed
import `in`.hridayan.ashell.core.presentation.theme.color.toColorScheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
private fun getInverseColorScheme(darkTheme: Boolean): ColorScheme {
    val context = LocalContext.current
    val settings = LocalSettings.current
    val dynamicColor = settings[SettingsKeys.DynamicColors]
    val generatedColorScheme = LocalUserGeneratedColorScheme.current
    val isUserGeneratedColorSchemeApplied = settings[SettingsKeys.UserGeneratedColorSchemeApplied]
    val isHighContrastDarkTheme = settings[SettingsKeys.HighContrastDarkMode]

    return when {
        isUserGeneratedColorSchemeApplied && generatedColorScheme != null && !dynamicColor -> generatedColorScheme.toColorScheme()

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme && isHighContrastDarkTheme) {
                highContrastDynamicDarkColorScheme(context)
            } else if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> {
            if (isHighContrastDarkTheme) {
                highContrastDarkColorSchemeFromSeed()
            } else {
                darkColorSchemeFromSeed()
            }
        }

        else -> lightColorSchemeFromSeed()
    }
}

@Composable
fun AiGenerationAnimationBox(
    message: String,
    modifier: Modifier = Modifier
) {
    val darkTheme = LocalDarkMode.current
    val userGeneratedColorSchemeEnabled =
        LocalSettings.current[SettingsKeys.UserGeneratedColorSchemeApplied]
    val isUserGeneratedColorSchemeDark =
        LocalSettings.current[SettingsKeys.IsCustomColorSchemeDarkThemed]
    val effectiveDarkMode =
        if (userGeneratedColorSchemeEnabled) isUserGeneratedColorSchemeDark else darkTheme

    val infiniteTransition = rememberInfiniteTransition(label = "LiquidNebula")

    // A slow, continuous timer from 0 to 2*PI to drive the organic motion
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    // Breathing text animation
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TextAlpha"
    )

    // Colors - inverted from current theme for dramatic contrast
    val inverseScheme = getInverseColorScheme(!effectiveDarkMode)
    val color1 = inverseScheme.primary
    val color2 = inverseScheme.tertiary
    val color3 = inverseScheme.secondary
    val bgColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp) // Approximate height of the OutlinedTextField with minLines=3
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val baseRadius = maxOf(width, height)

            // Orb 1: Primary color
            val radius1 = baseRadius * 0.45f
            val c1 = Offset(
                x = width * (0.5f + 0.4f * cos(time) + 0.1f * cos(time * 3f)),
                y = height * (0.5f + 0.4f * sin(time * 1.2f) + 0.1f * sin(time * 1.5f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color1.copy(alpha = 0.7f), Color.Transparent),
                    center = c1,
                    radius = radius1
                ),
                center = c1,
                radius = radius1
            )

            // Orb 2: Tertiary color
            val radius2 = baseRadius * 0.4f
            val c2 = Offset(
                x = width * (0.5f + 0.35f * sin(time * 0.8f + 2f)),
                y = height * (0.5f + 0.45f * cos(time * 1.6f + 1f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color2.copy(alpha = 0.7f), Color.Transparent),
                    center = c2,
                    radius = radius2
                ),
                center = c2,
                radius = radius2
            )

            // Orb 3: Secondary color
            val radius3 = baseRadius * 0.4f
            val c3 = Offset(
                x = width * (0.5f + 0.45f * cos(time * 1.5f + 4f)),
                y = height * (0.5f + 0.3f * sin(time * 0.7f + 3f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color3.copy(alpha = 0.7f), Color.Transparent),
                    center = c3,
                    radius = radius3
                ),
                center = c3,
                radius = radius3
            )

            // Orb 4: Primary color (fast darting)
            val radius4 = baseRadius * 0.4f
            val c4 = Offset(
                x = width * (0.5f - 0.4f * sin(time * 1.8f)),
                y = height * (0.5f - 0.35f * cos(time * 1.4f + 2f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color1.copy(alpha = 0.8f), Color.Transparent),
                    center = c4,
                    radius = radius4
                ),
                center = c4,
                radius = radius4
            )

            // Draw a subtle darkening overlay so the white text always pops
            // drawRect(color = color1.copy(alpha = 0.25f))
        }

        Text(
            modifier = Modifier.graphicsLayer {
                alpha = textAlpha
            },
            text = message,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AiGenerationAnimationBoxPreview() {
    AshellYouTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            AiGenerationAnimationBox(
                message = "Mixing digital paint...",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
