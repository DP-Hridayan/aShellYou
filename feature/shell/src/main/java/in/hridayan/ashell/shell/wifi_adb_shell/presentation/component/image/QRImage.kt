@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.image

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.shape.MorphingPolygonShape
import `in`.hridayan.ashell.core.presentation.components.shape.RotatingShape
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.launch

private const val SCRIM_ALPHA = 0.8f
private const val BUSY_SCRIM_ALPHA = 0.9f
private val OVERLAY_ICON_SIZE = 72.dp
private val QR_CONTAINER_SIZE = 200.dp
private val QR_IMAGE_PADDING = 50.dp
private const val POP_START_SCALE = 0.6f
private const val POP_FADE_MS = 180
private const val NOT_YET_SHOWN = 0
private const val MORPH_PROGRESS_CIRCLE = 0f
private const val MORPH_PROGRESS_COOKIE = 1f
private const val MORPH_DURATION_MS = 800
private const val ROTATION_TARGET_SPEED_DPS = 18f
private const val SPEED_RAMP_DURATION_MS = 800
private const val NANOS_PER_SECOND = 1_000_000_000f
private const val FULL_ROTATION_DEGREES = 360f
private const val STOPPED_SPEED = 0f
private const val INITIAL_ROTATION_DEGREES = 0f

@Composable
fun QRImage(
    modifier: Modifier = Modifier,
    qrBitmap: Bitmap,
    isWifiConnected: Boolean = false,
    isExpired: Boolean = false,
    onRetry: () -> Unit = {},
    wifiAdbState: WifiAdbState = WifiAdbState.Idle
) {
    val qrImage = qrBitmap.asImageBitmap()
    val popIn = rememberPopIn(qrBitmap)
    val wifiMorphRotation = rememberWifiMorphRotation(isWifiConnected)
    val morph = remember {
        Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = popIn.scale.value
                scaleY = popIn.scale.value
                alpha = popIn.alpha.value
            }
            .size(QR_CONTAINER_SIZE)
            .graphicsLayer {
                clip = true
                shape = RotatingShape(
                    baseShape = MorphingPolygonShape(
                        morph = morph,
                        progress = wifiMorphRotation.morphProgress(),
                    ),
                    rotationDegrees = wifiMorphRotation.rotationDegrees(),
                )
            }
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = qrImage,
            contentDescription = "QR Code",
            modifier = Modifier.padding(QR_IMAGE_PADDING)
        )

        if (!isWifiConnected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = SCRIM_ALPHA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_no_wifi),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(OVERLAY_ICON_SIZE)
                )
            }

            return@Box
        }

        if (isExpired) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = SCRIM_ALPHA))
                    .clickable(onClick = withHaptic { onRetry() }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = stringResource(R.string.retry),
                    tint = Color.Black,
                    modifier = Modifier.size(OVERLAY_ICON_SIZE)
                )
            }

            return@Box
        }

        if (wifiAdbState is WifiAdbState.Pairing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = SCRIM_ALPHA)),
                contentAlignment = Alignment.Center
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.pairing),
                    color = Color.Black
                )
            }
        }

        if (wifiAdbState is WifiAdbState.Connecting || wifiAdbState is WifiAdbState.Discovering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = BUSY_SCRIM_ALPHA)),
                contentAlignment = Alignment.Center
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.connecting),
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLargeEmphasized,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun rememberWifiMorphRotation(isWifiConnected: Boolean): WifiMorphRotationState {
    val morphProgress = remember {
        Animatable(if (isWifiConnected) MORPH_PROGRESS_COOKIE else MORPH_PROGRESS_CIRCLE)
    }
    val currentSpeed = remember {
        Animatable(if (isWifiConnected) ROTATION_TARGET_SPEED_DPS else STOPPED_SPEED)
    }
    var rotationDegrees by rememberSaveable { mutableFloatStateOf(INITIAL_ROTATION_DEGREES) }

    LaunchedEffect(isWifiConnected) {
        launch {
            morphProgress.animateTo(
                targetValue = if (isWifiConnected) MORPH_PROGRESS_COOKIE else MORPH_PROGRESS_CIRCLE,
                animationSpec = tween(
                    durationMillis = MORPH_DURATION_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
        launch {
            currentSpeed.animateTo(
                targetValue = if (isWifiConnected) ROTATION_TARGET_SPEED_DPS else STOPPED_SPEED,
                animationSpec = tween(
                    durationMillis = SPEED_RAMP_DURATION_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
        var lastFrameNanos = 0L
        while (isWifiConnected || currentSpeed.value > STOPPED_SPEED) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameNanos != 0L) {
                    val dtSeconds = (frameTimeNanos - lastFrameNanos) / NANOS_PER_SECOND
                    val speed = currentSpeed.value
                    if (speed > STOPPED_SPEED) {
                        rotationDegrees = (rotationDegrees + speed * dtSeconds) % FULL_ROTATION_DEGREES
                    }
                }
                lastFrameNanos = frameTimeNanos
            }
        }
    }

    return remember(morphProgress, currentSpeed) {
        WifiMorphRotationState(
            morphProgress = { morphProgress.value.coerceIn(MORPH_PROGRESS_CIRCLE, MORPH_PROGRESS_COOKIE) },
            rotationDegrees = { rotationDegrees },
        )
    }
}

private data class WifiMorphRotationState(
    val morphProgress: () -> Float,
    val rotationDegrees: () -> Float,
)

/**
 * Pops the QR in each time a new image arrives, keyed on the bitmap rather than the pairing code,
 * because a retry publishes the new code before its image is ready.
 *
 * The last image that popped is saved, so returning to the tab after the pager disposed it does not
 * replay the animation for a QR the user has already seen.
 */
@Composable
private fun rememberPopIn(image: Bitmap): PopIn {
    val imageId = System.identityHashCode(image)
    var lastPoppedId by rememberSaveable { mutableIntStateOf(NOT_YET_SHOWN) }
    val startHidden = lastPoppedId != imageId
    val scale = remember(imageId) { Animatable(if (startHidden) POP_START_SCALE else 1f) }
    val alpha = remember(imageId) { Animatable(if (startHidden) 0f else 1f) }

    LaunchedEffect(imageId) {
        if (lastPoppedId == imageId) return@LaunchedEffect
        lastPoppedId = imageId
        launch { alpha.animateTo(1f, tween(POP_FADE_MS)) }
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
    }

    return remember(scale, alpha) { PopIn(scale, alpha) }
}

/** The QR's current scale and opacity while it pops in, both 1 once it has settled. */
private data class PopIn(
    val scale: Animatable<Float, AnimationVector1D>,
    val alpha: Animatable<Float, AnimationVector1D>
)
