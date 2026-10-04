@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.image

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.launch

private const val SCRIM_ALPHA = 0.8f
private const val BUSY_SCRIM_ALPHA = 0.9f
private val OVERLAY_ICON_SIZE = 72.dp
private const val POP_START_SCALE = 0.6f
private const val POP_FADE_MS = 180
private const val NOT_YET_SHOWN = 0

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

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = popIn.scale.value
                scaleY = popIn.scale.value
                alpha = popIn.alpha.value
            }
            .size(200.dp)
            .clip(MaterialShapes.Cookie9Sided.toShape())
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = qrImage,
            contentDescription = "QR Code",
            modifier = Modifier.padding(50.dp)
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
