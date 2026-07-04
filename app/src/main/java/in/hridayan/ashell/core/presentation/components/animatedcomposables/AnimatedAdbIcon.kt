package `in`.hridayan.ashell.core.presentation.components.animatedcomposables

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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Animated ADB robot icon with an idle living-character loop and a click-triggered startled sequence.
 *
 * **Idle** – head bobs, sways, and antenna tips wobble; eyes blink periodically; body breathes.
 * All idle animations cross-fade smoothly into the startled state and back.
 *
 * **Startled (on click)** – head snaps up and tilts, antennas whip from inertia, eyes go wide,
 * body squishes; head then looks left then right before everything springs back. Ignored if already playing.
 * @param headColor Color of the head shape and antennas.
 * @param eyeColor Color of both eyes.
 * @param bodyColor Color of the body shape.
 * @param idleHeadBobDuration Period in ms for the vertical head bob.
 * @param idleHeadSwayDuration Period in ms for the left/right head sway.
 * @param idleAntennaDuration Period in ms for the antenna tip wobble.
 * @param idleBlinkCycleDuration Full blink-cycle duration in ms (blink occurs near the end).
 * @param idleBodyBreatheDuration Period in ms for the body breathe scale.
 * @param startledBurstDuration Duration in ms of the initial startle burst phase.
 * @param startledLookDuration Duration in ms of each look-left / look-right swing.
 */
@Composable
fun AnimatedAdbIcon(
    modifier: Modifier = Modifier,
    headColor: Color = MaterialTheme.colorScheme.primary,
    eyeColor: Color = MaterialTheme.colorScheme.onPrimary,
    bodyColor: Color = MaterialTheme.colorScheme.primary,
    idleHeadBobDuration: Int = 1900,
    idleHeadSwayDuration: Int = 2300,
    idleAntennaDuration: Int = 1600,
    idleBlinkCycleDuration: Int = 5000,
    idleBodyBreatheDuration: Int = 1900,
    startledBurstDuration: Int = 160,
    startledLookDuration: Int = 280,
    enableClickHaptics: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var isStartled by remember { mutableStateOf(false) }

    val sHeadY = remember { Animatable(0f) }
    val sHeadRot = remember { Animatable(0f) }
    val sEyeScale = remember { Animatable(1f) }
    val sBodyScaleY = remember { Animatable(1f) }
    val sAntennaExtra = remember { Animatable(0f) }

    val idle = rememberInfiniteTransition(label = "idle")

    val iHeadY = idle.animateFloat(
        initialValue = 0f, targetValue = -1f,
        animationSpec = infiniteRepeatable(
            tween(idleHeadBobDuration, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "headBob",
    )
    val iHeadSway = idle.animateFloat(
        initialValue = -1.5f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            tween(idleHeadSwayDuration, easing = LinearEasing),
            RepeatMode.Reverse,
        ),
        label = "headSway",
    )
    val iAntenna = idle.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(
            tween(idleAntennaDuration, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "antennaSway",
    )
    val iEyeBlink = idle.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = idleBlinkCycleDuration
                1f at 0
                1f at (idleBlinkCycleDuration * 0.92f).toInt() using LinearEasing
                0f at (idleBlinkCycleDuration * 0.95f).toInt() using FastOutSlowInEasing
                1f at (idleBlinkCycleDuration * 0.98f).toInt()
                1f at idleBlinkCycleDuration
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "eyeBlink",
    )
    val iBodyScale = idle.animateFloat(
        initialValue = 1f, targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            tween(idleBodyBreatheDuration, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "bodyBreathe",
    )

    // Also kept as State<Float> — target only changes when isStartled flips (twice per sequence).
    val blend = animateFloatAsState(
        targetValue = if (isStartled) 0f else 1f,
        animationSpec = tween(200),
        label = "blend",
    )

    fun playStartled() {
        if (isStartled) return
        isStartled = true
        scope.launch {
            launch {
                sHeadY.animateTo(
                    -5f,
                    tween(startledBurstDuration, easing = FastOutSlowInEasing)
                )
            }
            launch {
                sHeadRot.animateTo(
                    13f,
                    tween((startledBurstDuration * 1.2f).toInt(), easing = FastOutSlowInEasing)
                )
            }
            launch { sEyeScale.animateTo(1.5f, tween((startledBurstDuration * 0.75f).toInt())) }
            launch {
                sBodyScaleY.animateTo(
                    0.82f,
                    tween((startledBurstDuration * 1.3f).toInt(), easing = FastOutSlowInEasing)
                )
            }
            launch {
                sAntennaExtra.animateTo(
                    -22f,
                    tween((startledBurstDuration * 0.9f).toInt(), easing = FastOutSlowInEasing)
                )
            }
            delay((startledBurstDuration * 1.5f).toLong().milliseconds)

            launch {
                sHeadRot.animateTo(
                    -26f,
                    tween(startledLookDuration, easing = FastOutSlowInEasing)
                )
            }
            launch { sAntennaExtra.animateTo(16f, tween((startledLookDuration * 1.05f).toInt())) }
            delay((startledLookDuration * 1.25f).toLong().milliseconds)

            launch { sHeadRot.animateTo(28f, tween(startledLookDuration, easing = LinearEasing)) }
            launch { sAntennaExtra.animateTo(-18f, tween(startledLookDuration)) }
            delay((startledLookDuration * 1.4f).toLong().milliseconds)

            val returnSpec = spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
            launch { sHeadY.animateTo(0f, returnSpec) }
            launch { sHeadRot.animateTo(0f, returnSpec) }
            launch { sEyeScale.animateTo(1f, tween(250)) }
            launch { sBodyScaleY.animateTo(1f, returnSpec) }
            launch { sAntennaExtra.animateTo(0f, returnSpec) }
            delay(700.milliseconds)

            isStartled = false
        }
    }

    val headVector = rememberAdbHeadVector(headColor)
    val bodyVector = rememberAdbBodyVector(bodyColor)
    val rightEyeVector = rememberAdbRightEyeVector(eyeColor)
    val leftEyeVector = rememberAdbLeftEyeVector(eyeColor)

    BoxWithConstraints(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    playStartled()
                    if (enableClickHaptics) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                },
            ),
    ) {
        val vpToPx = with(LocalDensity.current) { maxWidth.toPx() / 48f }

        Image(
            imageVector = bodyVector,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val b = blend.value
                    scaleY = iBodyScale.value * b + sBodyScaleY.value * (1f - b)
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val b = blend.value
                    translationY = (iHeadY.value * b + sHeadY.value) * vpToPx
                    rotationZ = iHeadSway.value * b + sHeadRot.value
                    transformOrigin = TransformOrigin(0.5f, 0.458f)
                },
        ) {
            Image(
                imageVector = headVector,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val b = blend.value
                        rotationZ = iAntenna.value * b + sAntennaExtra.value
                        transformOrigin = TransformOrigin(0.5f, 0.178f)
                    },
            )

            Image(
                imageVector = rightEyeVector,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val b = blend.value
                        val s = iEyeBlink.value * b + sEyeScale.value * (1f - b)
                        scaleX = s
                        scaleY = s
                        transformOrigin = TransformOrigin(0.625f, 0.332f)
                    },
            )

            Image(
                imageVector = leftEyeVector,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val b = blend.value
                        val s = iEyeBlink.value * b + sEyeScale.value * (1f - b)
                        scaleX = s
                        scaleY = s
                        transformOrigin = TransformOrigin(0.375f, 0.332f)
                    },
            )
        }
    }
}

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
