package `in`.hridayan.ashell.core.presentation.components.shape

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.sin

enum class WaveEdge {
    Top,
    Bottom,
    Both
}

class SineWaveShape(
    private val amplitude: Float,
    private val frequency: Float,
    private val edge: WaveEdge,
    private val stepPx: Float = 4f
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {

        val width = size.width
        val height = size.height
        val path = Path()

        val omega = 2f * PI.toFloat() * frequency / width

        /* ───────────── TOP EDGE ───────────── */
        if (edge == WaveEdge.Top || edge == WaveEdge.Both) {
            path.moveTo(0f, amplitude)

            var x = 0f
            while (x <= width) {
                val y = amplitude + amplitude * sin(omega * x)
                path.lineTo(x, y)
                x += stepPx
            }
        } else {
            path.moveTo(0f, 0f)
            path.lineTo(width, 0f)
        }

        /* ───────────── RIGHT EDGE ───────────── */
        path.lineTo(width, height)

        /* ───────────── BOTTOM EDGE ───────────── */
        if (edge == WaveEdge.Bottom || edge == WaveEdge.Both) {
            var x = width
            while (x >= 0f) {
                val y = height - amplitude + amplitude * sin(omega * x)
                path.lineTo(x, y)
                x -= stepPx
            }
        } else {
            path.lineTo(0f, height)
        }

        /* ───────────── LEFT EDGE ───────────── */
        path.lineTo(0f, if (edge == WaveEdge.Top || edge == WaveEdge.Both) amplitude else 0f)

        path.close()
        return Outline.Generic(path)
    }
}
