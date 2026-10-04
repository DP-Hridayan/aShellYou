package `in`.hridayan.ashell.mirror.presentation.model

import `in`.hridayan.ashell.mirror.domain.model.StreamStats
import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.quality.QualitySelection
import `in`.hridayan.ashell.mirror.domain.quality.ResolvedQuality

/**
 * The video quality as the sheet, the overlay and the hints show it.
 *
 * @property selection the choice saved for this transport.
 * @property resolved what the current session asked the server for, or null before it starts.
 * @property viewerRefreshRate this phone's highest refresh rate, once known.
 * @property supportedCodecs the codecs this phone can decode.
 * @property stats the last second of the stream, or null while nothing streams.
 * @property isStruggleHintPending the can't-keep-up hint should be shown.
 * @property codecFallback the codec the device failed to encode, until its message has been shown.
 */
data class QualityUiState(
    val selection: QualitySelection = QualitySelection(),
    val resolved: ResolvedQuality? = null,
    val viewerRefreshRate: Int? = null,
    val supportedCodecs: Set<VideoCodec> = setOf(VideoCodec.H264),
    val isStatsOverlayOn: Boolean = false,
    val stats: StreamStats? = null,
    val isStruggleHintPending: Boolean = false,
    val codecFallback: VideoCodec? = null
)
