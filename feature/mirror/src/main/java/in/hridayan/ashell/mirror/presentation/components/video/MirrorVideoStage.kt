package `in`.hridayan.ashell.mirror.presentation.components.video

import androidx.compose.foundation.AndroidExternalSurface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.mirror.domain.geometry.VideoViewport
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import `in`.hridayan.ashell.mirror.presentation.input.TouchTranslator
import `in`.hridayan.ashell.mirror.presentation.model.MirrorActions

private val EdgeClamp = 24.dp

/**
 * The live video, aspect-fit inside the area left after the system's horizontal gesture zones, so
 * a swipe that starts at the device's screen edge is not taken by this phone's back gesture.
 *
 * Every touch on the stage goes to the device; the app claims no gesture of its own here. The
 * surface exists from the first frame of the screen, so the decoder can start on the server's first
 * config packet instead of waiting for a re-sent one.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MirrorVideoStage(
    videoSize: VideoSize?,
    isControlAvailable: Boolean,
    actions: MirrorActions,
    modifier: Modifier = Modifier
) {
    val edgeClampPx = with(LocalDensity.current) { EdgeClamp.toPx() }
    val translator = remember { TouchTranslator() }
    var stageSize by remember { mutableStateOf(IntSize.Zero) }
    val viewport = remember(stageSize, videoSize, edgeClampPx) {
        videoSize?.takeIf { stageSize != IntSize.Zero }?.let {
            VideoViewport(stageSize.width.toFloat(), stageSize.height.toFloat(), it, edgeClampPx)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemGestures.only(WindowInsetsSides.Horizontal))
            .onSizeChanged { stageSize = it }
            .pointerInteropFilter { event ->
                val activeViewport = viewport
                if (isControlAvailable && activeViewport != null) {
                    actions.onTouch(translator.translate(event, activeViewport))
                }
                true
            },
        contentAlignment = Alignment.Center
    ) {
        AndroidExternalSurface(
            modifier = videoSize
                ?.let { Modifier.aspectRatio(it.width.toFloat() / it.height) }
                ?: Modifier.fillMaxSize()
        ) {
            onSurface { surface, _, _ ->
                actions.onSurfaceAvailable(surface)
                surface.onDestroyed { actions.onSurfaceDestroyed() }
            }
        }
    }
}
