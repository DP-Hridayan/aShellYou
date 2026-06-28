package `in`.hridayan.ashell.core.presentation.components.svg.vectors

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * An animated ADB icon composable where the robot behaves as a living character.
 *
 * **Idle** (looping, all blended together):
 * - Head bobs gently up/down
 * - Head sways slightly left/right at a different period for organic feel
 * - Antenna tips wobble independently around their base pivot
 * - Eyes blink periodically
 * - Body breathes with a subtle scaleY oscillation in sync with the head bob
 *
 * **On click** (while not already startled):
 * - Head snaps up and tilts right; antennas whip left from inertia; body squishes; eyes go wide
 * - Head swings left (look left), antennas trail opposite
 * - Head swings right (look right), antennas trail opposite
 * - Everything springs back to center with a bouncy overshoot
 * - Clicking during a startled animation is ignored
 *
 * Idle → startled and startled → idle transitions are cross-faded via a [blend] float so
 * there is no abrupt cut between the two states.
 *
 * Layer hierarchy (transforms cascade parent → child via [graphicsLayer]):
 * ```
 * body  Image          ← independent scaleY (breathe / squish)
 * head  Box            ← translateY + rotationZ  (bob, sway, jump, look left/right)
 *   antenna  Image     ←   extra rotationZ around tip pivot  (wobble / inertia lag)
 *   rightEye Image     ←   uniform scale  (blink / wide)
 *   leftEye  Image     ←   uniform scale  (blink / wide)
 * ```
 */
@Composable
fun AnimatedAdbIcon(
    modifier: Modifier = Modifier,
    iconSize: Dp = 96.dp,
) {
    val scope = rememberCoroutineScope()
    var isStartled by remember { mutableStateOf(false) }

    // Convert 1 viewport unit (0–48 scale) to pixels at the displayed size.
    val vpToPx = with(LocalDensity.current) { iconSize.toPx() / 48f }

    // ── Startled-override animatables ──────────────────────────────────────────────────────
    // All start at neutral; only driven when a startle plays.
    val sHeadY        = remember { Animatable(0f) }
    val sHeadRot      = remember { Animatable(0f) }
    val sEyeScale     = remember { Animatable(1f)  }
    val sBodyScaleY   = remember { Animatable(1f)  }
    val sAntennaExtra = remember { Animatable(0f)  }

    // ── Idle infinite animations ───────────────────────────────────────────────────────────
    val idle = rememberInfiniteTransition(label = "idle")

    // Head bob: gentle up/down
    val iHeadY by idle.animateFloat(
        initialValue = 0f, targetValue = -2f,
        animationSpec = infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "headBob",
    )
    // Head sway: slow left/right tilt at a different period for organic feel
    val iHeadSway by idle.animateFloat(
        initialValue = -1.5f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(2300, easing = LinearEasing), RepeatMode.Reverse),
        label = "headSway",
    )
    // Antenna tip wobble: extra rotation around the antenna-base pivot on the head image
    val iAntenna by idle.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "antennaSway",
    )
    // Eye blink: spends most of each 5 s cycle fully open; blinks quickly then reopens
    val iEyeBlink by idle.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                1f at 0
                1f at 4600 using LinearEasing
                0f at 4750 using FastOutSlowInEasing
                1f at 4900
                1f at 5000
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "eyeBlink",
    )
    // Body breathe: in sync with head bob so it reads as one breathing motion
    val iBodyScale by idle.animateFloat(
        initialValue = 1f, targetValue = 1.025f,
        animationSpec = infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bodyBreathe",
    )

    // ── Cross-fade: 1 = full idle, 0 = full startled ──────────────────────────────────────
    val blend by animateFloatAsState(
        targetValue = if (isStartled) 0f else 1f,
        animationSpec = tween(200),
        label = "blend",
    )

    // ── Effective (blended) transform values ───────────────────────────────────────────────
    //
    // For additive values (translateY, rotation, antennaExtra) the idle and startled
    // contributions are simply weighted by blend so they fade through zero cleanly.
    //
    // For multiplicative values (scale) we interpolate between the idle value and the
    // startled value using blend as the mix weight.
    val effHeadY        = iHeadY    * blend + sHeadY.value
    val effHeadRot      = iHeadSway * blend + sHeadRot.value
    val effAntennaExtra = iAntenna  * blend + sAntennaExtra.value
    val effEyeScale     = iEyeBlink * blend + sEyeScale.value   * (1f - blend)
    val effBodyScaleY   = iBodyScale * blend + sBodyScaleY.value * (1f - blend)

    // ── Startled click handler ─────────────────────────────────────────────────────────────
    fun playStartled() {
        if (isStartled) return
        isStartled = true

        scope.launch {

            // Phase 1 — sudden startle burst (0 ms)
            // Head snaps up and tilts right; antennas whip back-left from inertia;
            // body squishes; eyes go wide.
            launch { sHeadY.animateTo(-9f,   tween(130, easing = FastOutSlowInEasing)) }
            launch { sHeadRot.animateTo(13f,  tween(160, easing = FastOutSlowInEasing)) }
            launch { sEyeScale.animateTo(1.5f, tween(100)) }
            launch { sBodyScaleY.animateTo(0.82f, tween(180, easing = FastOutSlowInEasing)) }
            launch { sAntennaExtra.animateTo(-22f, tween(140, easing = FastOutSlowInEasing)) }
            delay(220)

            // Phase 2 — look left (220 ms)
            // Head swings left; antennas trail right (inertia lag in opposite direction).
            launch { sHeadRot.animateTo(-26f, tween(270, easing = FastOutSlowInEasing)) }
            launch { sAntennaExtra.animateTo(16f, tween(290)) }
            delay(330)

            // Phase 3 — look right (550 ms)
            // Head swings right; antennas trail left.
            launch { sHeadRot.animateTo(28f,  tween(300, easing = LinearEasing)) }
            launch { sAntennaExtra.animateTo(-18f, tween(300)) }
            delay(380)

            // Phase 4 — springy return (930 ms)
            // Everything bounces back to neutral with medium overshoot so it feels alive.
            launch { sHeadY.animateTo(0f,
                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
            launch { sHeadRot.animateTo(0f,
                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
            launch { sEyeScale.animateTo(1f, tween(250)) }
            launch { sBodyScaleY.animateTo(1f,
                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
            launch { sAntennaExtra.animateTo(0f,
                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }

            // Wait for springs to fully settle before re-enabling clicks.
            delay(700)
            isStartled = false
        }
    }

    // ── Part vectors ───────────────────────────────────────────────────────────────────────
    val foreground = MaterialTheme.colorScheme.primary
    val eyeColor   = MaterialTheme.colorScheme.onPrimary

    val headVector     = rememberAdbHeadVector(foreground)
    val bodyVector     = rememberAdbBodyVector(foreground)
    val rightEyeVector = rememberAdbRightEyeVector(eyeColor)
    val leftEyeVector  = rememberAdbLeftEyeVector(eyeColor)

    // ── Render ─────────────────────────────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .size(iconSize)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { playStartled() },
            ),
    ) {
        // Body — independent from head; pivots at the top edge of the body shape (y = 24 = 0.5f).
        Image(
            imageVector = bodyVector,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleY = effBodyScaleY
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                },
        )

        // Head Box — all children inherit this layer's translateY + rotationZ so
        // antenna, right eye, and left eye all move with the head as one assembly.
        // Pivot at (24, 22) in viewport = (0.5f, 0.458f) in normalised space.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = effHeadY * vpToPx
                    rotationZ    = effHeadRot
                    transformOrigin = TransformOrigin(0.5f, 0.458f)
                },
        ) {

            // Head + antenna shape — receives an additional rotationZ for antenna wobble /
            // inertia lag. Pivot at (24, 8.55) ≈ the antenna base = (0.5f, 0.178f).
            // This rotation compounds on top of the head Box rotation, so during the startle
            // sequence the antenna appears to physically lag behind the head's swing.
            Image(
                imageVector = headVector,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = effAntennaExtra
                        transformOrigin = TransformOrigin(0.5f, 0.178f)
                    },
            )

            // Right eye — inherits head Box transform; adds its own uniform scale for
            // blink / wide-eye effect. Pivot at (30, 15.95) = (0.625f, 0.332f).
            Image(
                imageVector = rightEyeVector,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = effEyeScale
                        scaleY = effEyeScale
                        transformOrigin = TransformOrigin(0.625f, 0.332f)
                    },
            )

            // Left eye — same as right eye. Pivot at (18, 15.95) = (0.375f, 0.332f).
            Image(
                imageVector = leftEyeVector,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = effEyeScale
                        scaleY = effEyeScale
                        transformOrigin = TransformOrigin(0.375f, 0.332f)
                    },
            )
        }
    }
}

// ── Private part-vector builders ──────────────────────────────────────────────────────────
//
// Each vector shares the same 48 × 48 viewport as the full icon so all layers align
// perfectly when stacked in the same-sized Box. Areas outside a layer's own paths
// are fully transparent.

@Composable
private fun rememberAdbHeadVector(color: Color): ImageVector =
    remember(color) {
        Builder("adbHead", 48.dp, 48.dp, 48f, 48f).apply {
            addPath(
                pathData = PathParser().parsePathString(
                    "M10 21.95v-2" +
                        "q0-3.6 1.65-6.55Q13.3 10.45 16 8.55" +
                        "L12.25 4.8l1.8-1.8 4.25 4.2" +
                        "q1.25-0.6 2.725-0.925Q22.5 5.95 24 5.95" +
                        "t2.975 0.325Q28.45 6.6 29.75 7.2" +
                        "l4.2-4.2 1.8 1.8L32 8.55" +
                        "q2.7 1.9 4.35 4.85Q38 16.35 38 19.95v2z"
                ).toNodes(),
                fill = SolidColor(color),
            )
        }.build()
    }

@Composable
private fun rememberAdbBodyVector(color: Color): ImageVector =
    remember(color) {
        Builder("adbBody", 48.dp, 48.dp, 48f, 48f).apply {
            addPath(
                pathData = PathParser().parsePathString(
                    "M24 46q-5.85 0-9.925-4.075Q10 37.85 10 32v-8.05h28V32" +
                        "q0 5.85-4.075 9.925Q29.85 46 24 46z"
                ).toNodes(),
                fill = SolidColor(color),
            )
        }.build()
    }

@Composable
private fun rememberAdbRightEyeVector(color: Color): ImageVector =
    remember(color) {
        Builder("adbRightEye", 48.dp, 48.dp, 48f, 48f).apply {
            addPath(
                pathData = PathParser().parsePathString(
                    "M30 17.95q0.85 0 1.425-0.575Q32 16.8 32 15.95" +
                        "q0-0.85-0.575-1.425Q30.85 13.95 30 13.95" +
                        "q-0.85 0-1.425 0.575Q28 15.1 28 15.95" +
                        "q0 0.85 0.575 1.425Q29.15 17.95 30 17.95z"
                ).toNodes(),
                fill = SolidColor(color),
            )
        }.build()
    }

@Composable
private fun rememberAdbLeftEyeVector(color: Color): ImageVector =
    remember(color) {
        Builder("adbLeftEye", 48.dp, 48.dp, 48f, 48f).apply {
            addPath(
                pathData = PathParser().parsePathString(
                    "M18 17.95q0.85 0 1.425-0.575Q20 16.8 20 15.95" +
                        "q0-0.85-0.575-1.425Q18.85 13.95 18 13.95" +
                        "q-0.85 0-1.425 0.575Q16 15.1 16 15.95" +
                        "q0 0.85 0.575 1.425Q17.15 17.95 18 17.95z"
                ).toNodes(),
                fill = SolidColor(color),
            )
        }.build()
    }
