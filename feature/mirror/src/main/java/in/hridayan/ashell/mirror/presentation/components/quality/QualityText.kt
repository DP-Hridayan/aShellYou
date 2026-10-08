package `in`.hridayan.ashell.mirror.presentation.components.quality

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import `in`.hridayan.ashell.mirror.domain.quality.QualityLimit
import `in`.hridayan.ashell.mirror.domain.quality.ResolvedQuality
import java.text.DecimalFormat

private const val BITS_PER_MEGABIT = 1_000_000.0
private const val MEGABITS_PATTERN = "0.#"

/** For example "1080 × 2400 · up to 60 fps · 8 Mbps · H.264". */
@Composable
internal fun qualitySummary(resolved: ResolvedQuality): String = stringResource(
    R.string.quality_summary,
    videoSizeText(resolved.expectedSize, resolved.options.maxSize),
    stringResource(R.string.frames_per_second, resolved.options.maxFps),
    megabitsText(resolved.options.videoBitRate.toLong()),
    resolved.options.videoCodec.displayName
)

/** The exact size when the device's screen is known, otherwise the long-edge limit or "Native". */
@Composable
internal fun videoSizeText(size: VideoSize?, maxLongEdge: Int): String = when {
    size != null -> stringResource(R.string.video_dimensions, size.width, size.height)
    maxLongEdge <= 0 -> stringResource(R.string.native_resolution)
    else -> stringResource(R.string.long_edge_pixels, maxLongEdge)
}

@Composable
internal fun megabitsText(bitsPerSecond: Long): String {
    val format = remember { DecimalFormat(MEGABITS_PATTERN) }
    return stringResource(R.string.megabits_per_second, format.format(bitsPerSecond / BITS_PER_MEGABIT))
}

@StringRes
internal fun QualityLimit.explanationRes(): Int? = when (this) {
    QualityLimit.NONE -> null
    QualityLimit.TARGET_SCREEN -> R.string.quality_limit_target_screen
    QualityLimit.TARGET_ENCODER -> R.string.quality_limit_target_encoder
    QualityLimit.VIEWER_SCREEN -> R.string.quality_limit_viewer_screen
    QualityLimit.VIEWER_DECODER -> R.string.quality_limit_viewer_decoder
    QualityLimit.CONNECTION -> R.string.quality_limit_connection
    QualityLimit.CHOSEN -> R.string.quality_limit_chosen
}
