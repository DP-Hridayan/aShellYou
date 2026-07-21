package `in`.hridayan.ashell.core.presentation.components.floaters

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private object PhysicsConstants {
    const val MIN_SPEED_DP = 10f
    const val MAX_SPEED_DP = 30f
    const val REPEL_RADIUS_DP = 150f
    const val REPEL_FORCE_DP = 450f
    const val DAMPING = 0.999f
    const val RESTITUTION = 0.95f
}

private class FloatingIconState(
    val painter: Painter,
    val size: Float,
    val alpha: Float,
    var pos: Offset,
    var vel: Offset,
    val radius: Float
)

@Composable
fun FloatingIconsBackground(
    modifier: Modifier = Modifier,
    iconCount: Int = 12,
    iconResIds: List<Int>
) {
    if (iconResIds.isEmpty()) return

    val density = LocalDensity.current
    val tintColor = MaterialTheme.colorScheme.onSurface

    val painters = List(iconCount) { 
        painterResource(iconResIds[it % iconResIds.size]) 
    }
    
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val icons = remember { mutableStateOf<List<FloatingIconState>>(emptyList()) }
    
    var lastFrameTime by remember { mutableLongStateOf(0L) }
    var frameTrigger by remember { mutableLongStateOf(0L) }
    var touchPos by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(containerSize, iconCount) {
        if (containerSize.width > 0 && containerSize.height > 0 && icons.value.isEmpty()) {
            val width = containerSize.width.toFloat()
            val height = containerSize.height.toFloat()
            val minSpeed = with(density) { PhysicsConstants.MIN_SPEED_DP.dp.toPx() }
            val maxSpeed = with(density) { PhysicsConstants.MAX_SPEED_DP.dp.toPx() }

            icons.value = List(iconCount) { index ->
                val sizeDp = (16..36).random().dp
                val sizePx = with(density) { sizeDp.toPx() }
                val radius = sizePx / 2f
                
                // Ensure initial position is fully within bounds
                val x = (radius..(width - radius)).random()
                val y = (radius..(height - radius)).random()
                
                val angle = (0f..(2f * Math.PI.toFloat())).random()
                val speed = (minSpeed..maxSpeed).random()
                
                FloatingIconState(
                    painter = painters[index],
                    size = sizePx,
                    alpha = (0.25f..0.35f).random(),
                    pos = Offset(x, y),
                    vel = Offset(cos(angle) * speed, sin(angle) * speed),
                    radius = radius
                )
            }
        }
    }

    LaunchedEffect(containerSize) {
        if (containerSize.width == 0) return@LaunchedEffect

        while (true) {
            withFrameNanos { frameTime ->
                if (lastFrameTime != 0L && icons.value.isNotEmpty()) {
                    val dt =
                        ((frameTime - lastFrameTime) / 1e9f).coerceAtMost(0.032f)

                    updatePhysics(
                        icons = icons.value,
                        width = containerSize.width.toFloat(),
                        height = containerSize.height.toFloat(),
                        dt = dt,
                        touchPos = touchPos,
                        repelRadius = with(density) { PhysicsConstants.REPEL_RADIUS_DP.dp.toPx() },
                        repelForce = with(density) { PhysicsConstants.REPEL_FORCE_DP.dp.toPx() }
                    )
                    frameTrigger = frameTime
                }
                lastFrameTime = frameTime
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    touchPos = down.position

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            touchPos = change.position
                        } else {
                            touchPos = null
                            break
                        }
                    }
                }
            }
    ) {
        if (icons.value.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                frameTrigger.let { }

                drawIcons(icons.value, tintColor)
            }
        }
    }
}

private fun DrawScope.drawIcons(icons: List<FloatingIconState>, tintColor: Color) {
    icons.forEach { icon ->
        val colorFilter = ColorFilter.tint(tintColor.copy(alpha = icon.alpha))
        val offset = icon.pos - Offset(icon.radius, icon.radius)
        translate(left = offset.x, top = offset.y) {
            with(icon.painter) {
                draw(
                    size = Size(icon.size, icon.size),
                    alpha = icon.alpha,
                    colorFilter = colorFilter
                )
            }
        }
    }
}

private fun updatePhysics(
    icons: List<FloatingIconState>,
    width: Float,
    height: Float,
    dt: Float,
    touchPos: Offset?,
    repelRadius: Float,
    repelForce: Float
) {
    icons.forEach { icon ->
        // Touch interaction
        touchPos?.let { touch ->
            val dx = icon.pos.x - touch.x
            val dy = icon.pos.y - touch.y
            val distSq = dx * dx + dy * dy
            val rSq = repelRadius * repelRadius

            if (distSq < rSq) {
                val dist = sqrt(distSq).coerceAtLeast(10f)
                val force = (1f - dist / repelRadius) * repelForce
                icon.vel += Offset((dx / dist) * force * dt, (dy / dist) * force * dt)
            }
        }

        //  Motion update
        icon.pos += icon.vel * dt
        icon.vel *= PhysicsConstants.DAMPING

        // Wall bounce
        if (icon.pos.x < icon.radius) {
            icon.pos = Offset(icon.radius, icon.pos.y)
            icon.vel = Offset(-icon.vel.x * PhysicsConstants.RESTITUTION, icon.vel.y)
        } else if (icon.pos.x > width - icon.radius) {
            icon.pos = Offset(width - icon.radius, icon.pos.y)
            icon.vel = Offset(-icon.vel.x * PhysicsConstants.RESTITUTION, icon.vel.y)
        }

        if (icon.pos.y < icon.radius) {
            icon.pos = Offset(icon.pos.x, icon.radius)
            icon.vel = Offset(icon.vel.x, -icon.vel.y * PhysicsConstants.RESTITUTION)
        } else if (icon.pos.y > height - icon.radius) {
            icon.pos = Offset(icon.pos.x, height - icon.radius)
            icon.vel = Offset(icon.vel.x, -icon.vel.y * PhysicsConstants.RESTITUTION)
        }
    }

    //  Inter-icon collision
    for (i in icons.indices) {
        for (j in i + 1 until icons.size) {
            val a = icons[i]
            val b = icons[j]
            val dx = b.pos.x - a.pos.x
            val dy = b.pos.y - a.pos.y
            val distSq = dx * dx + dy * dy
            val minDist = a.radius + b.radius

            if (distSq < minDist * minDist) {
                val dist = sqrt(distSq).coerceAtLeast(1f)
                val nx = dx / dist
                val ny = dy / dist

                // Solve overlap
                val overlap = minDist - dist
                a.pos -= Offset(nx * overlap / 2, ny * overlap / 2)
                b.pos += Offset(nx * overlap / 2, ny * overlap / 2)

                // Elastic impulse
                val rvx = b.vel.x - a.vel.x
                val rvy = b.vel.y - a.vel.y
                val vn = rvx * nx + rvy * ny

                if (vn < 0) {
                    val jImpulse = -(1 + PhysicsConstants.RESTITUTION) * vn
                    val impulseX = jImpulse * nx / 2
                    val impulseY = jImpulse * ny / 2
                    a.vel -= Offset(impulseX, impulseY)
                    b.vel += Offset(impulseX, impulseY)
                }
            }
        }
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float {
    return (start + (endInclusive - start) * Random.nextFloat())
}