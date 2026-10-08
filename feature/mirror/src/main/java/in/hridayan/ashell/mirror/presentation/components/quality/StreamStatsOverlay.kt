package `in`.hridayan.ashell.mirror.presentation.components.quality

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.mirror.domain.model.StreamStats
import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.model.VideoSize

private const val OVERLAY_ALPHA = 0.85f
private val OverlayPadding = 8.dp
private val OverlayVerticalPadding = 4.dp

/** One line of measured stream statistics, over a corner of the video. */
@Composable
fun StreamStatsOverlay(stats: StreamStats, size: VideoSize, codec: VideoCodec, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = OVERLAY_ALPHA),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = stringResource(
                R.string.stream_statistics,
                stringResource(R.string.video_dimensions, size.width, size.height),
                stringResource(R.string.frames_per_second, stats.framesRendered),
                stats.framesDropped,
                megabitsText(stats.bitsPerSecond),
                codec.displayName
            ),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = OverlayPadding, vertical = OverlayVerticalPadding)
        )
    }
}
