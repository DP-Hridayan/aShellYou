package `in`.hridayan.ashell.core.presentation.components.divider

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WavyHorizontalDivider(
    modifier: Modifier = Modifier,
    waveLength: Dp = 20.dp,
    waveHeight: Dp = 6.dp,
    thickness: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(waveHeight)
    ) {
        val waveLengthPx = waveLength.toPx()
        val waveHeightPx = waveHeight.toPx()
        val thicknessPx = thickness.toPx()

        // Center the wave vertically within the allocated canvas height
        val centerY = size.height / 2

        val path = Path().apply {
            moveTo(0f, centerY)

            // Increment by 2 pixels for a smooth curve without killing performance
            val step = 2f
            var x = 0f

            while (x <= size.width) {
                // Sine wave formula: y = sin(x * frequency) * amplitude
                val radians = (x / waveLengthPx) * (2 * Math.PI)
                val y = centerY + (sin(radians).toFloat() * (waveHeightPx / 2))

                lineTo(x, y)
                x += step
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = thicknessPx)
        )
    }

}