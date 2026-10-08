package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.mirror.domain.model.MirrorQualityPreset
import `in`.hridayan.ashell.mirror.domain.model.VideoCodec

/** What the user picked in the video quality sheet. It is remembered per transport. */
sealed interface QualityChoice {

    /** Worked out for each session from both phones' screens and this phone's decoder. */
    data object Auto : QualityChoice

    data class Preset(val preset: MirrorQualityPreset) : QualityChoice

    data class Custom(val quality: CustomQuality) : QualityChoice
}

enum class QualityMode { AUTO, SAVER, BALANCED, SHARP, CUSTOM }

/**
 * What is remembered for one transport: the mode in use, plus the custom values. Those are kept
 * while a preset is in use, so switching back to Custom restores them.
 */
data class QualitySelection(
    val mode: QualityMode = QualityMode.AUTO,
    val custom: CustomQuality = CustomQuality()
) {
    val choice: QualityChoice
        get() = when (mode) {
            QualityMode.AUTO -> QualityChoice.Auto
            QualityMode.SAVER -> QualityChoice.Preset(MirrorQualityPreset.SAVER)
            QualityMode.BALANCED -> QualityChoice.Preset(MirrorQualityPreset.BALANCED)
            QualityMode.SHARP -> QualityChoice.Preset(MirrorQualityPreset.SHARP)
            QualityMode.CUSTOM -> QualityChoice.Custom(custom)
        }

    /** The selection for the same custom values with [preset] in use, as the one-tap fix applies it. */
    fun withPreset(preset: MirrorQualityPreset): QualitySelection = copy(
        mode = when (preset) {
            MirrorQualityPreset.SAVER -> QualityMode.SAVER
            MirrorQualityPreset.BALANCED -> QualityMode.BALANCED
            MirrorQualityPreset.SHARP -> QualityMode.SHARP
        }
    )
}

/** A custom quality. Each field is either fixed by the user or left to Auto on its own. */
data class CustomQuality(
    val resolution: ResolutionChoice = ResolutionChoice.Auto,
    val frameRate: FrameRateChoice = FrameRateChoice.Auto,
    val bitrate: BitrateChoice = BitrateChoice.Auto,
    val codec: VideoCodec = VideoCodec.H264
)

sealed interface ResolutionChoice {

    data object Auto : ResolutionChoice

    /** The device's own screen size, without scaling. */
    data object Native : ResolutionChoice

    /** A limit on the video's long edge, in pixels. The server never upscales past native. */
    data class LongEdge(val pixels: Int) : ResolutionChoice
}

sealed interface FrameRateChoice {

    data object Auto : FrameRateChoice

    /** An upper bound in frames per second. The stream is variable rate, so a still screen sends fewer. */
    data class Fixed(val fps: Int) : FrameRateChoice
}

sealed interface BitrateChoice {

    data object Auto : BitrateChoice

    data class Fixed(val megabits: Int) : BitrateChoice
}
