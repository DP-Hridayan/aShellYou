package `in`.hridayan.ashell.mirror.data.quality

import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.quality.BitrateChoice
import `in`.hridayan.ashell.mirror.domain.quality.CustomQuality
import `in`.hridayan.ashell.mirror.domain.quality.FrameRateChoice
import `in`.hridayan.ashell.mirror.domain.quality.QualityMode
import `in`.hridayan.ashell.mirror.domain.quality.QualitySelection
import `in`.hridayan.ashell.mirror.domain.quality.ResolutionChoice

private const val RESOLUTION_AUTO = -1
private const val RESOLUTION_NATIVE = 0
private const val VALUE_AUTO = 0

/**
 * A [QualitySelection] as the settings store keeps it: one value per settings key. The defaults of
 * the mirror keys in `SettingsKeys` are these values for Auto.
 */
data class StoredQuality(
    val mode: String,

    /** -1 for Auto, 0 for native, otherwise a long-edge limit in pixels. */
    val resolution: Int,

    /** 0 for Auto, otherwise frames per second. */
    val frameRate: Int,

    /** 0 for Auto, otherwise megabits per second. */
    val bitrate: Int,

    /** The codec's server name. */
    val codec: String
)

/** Converts between [QualitySelection] and [StoredQuality]. Unknown stored values read as Auto. */
object QualitySettingsMapper {

    fun toStored(selection: QualitySelection): StoredQuality {
        val custom = selection.custom
        return StoredQuality(
            mode = selection.mode.name.lowercase(),
            resolution = when (val resolution = custom.resolution) {
                ResolutionChoice.Auto -> RESOLUTION_AUTO
                ResolutionChoice.Native -> RESOLUTION_NATIVE
                is ResolutionChoice.LongEdge -> resolution.pixels
            },
            frameRate = (custom.frameRate as? FrameRateChoice.Fixed)?.fps ?: VALUE_AUTO,
            bitrate = (custom.bitrate as? BitrateChoice.Fixed)?.megabits ?: VALUE_AUTO,
            codec = custom.codec.serverName
        )
    }

    fun fromStored(stored: StoredQuality): QualitySelection = QualitySelection(
        mode = QualityMode.entries.firstOrNull { it.name.equals(stored.mode, ignoreCase = true) } ?: QualityMode.AUTO,
        custom = CustomQuality(
            resolution = when {
                stored.resolution == RESOLUTION_NATIVE -> ResolutionChoice.Native
                stored.resolution > RESOLUTION_NATIVE -> ResolutionChoice.LongEdge(stored.resolution)
                else -> ResolutionChoice.Auto
            },
            frameRate = stored.frameRate.takeIf { it > VALUE_AUTO }
                ?.let(FrameRateChoice::Fixed)
                ?: FrameRateChoice.Auto,
            bitrate = stored.bitrate.takeIf { it > VALUE_AUTO }
                ?.let(BitrateChoice::Fixed)
                ?: BitrateChoice.Auto,
            codec = VideoCodec.entries.firstOrNull { it.serverName == stored.codec } ?: VideoCodec.H264
        )
    )
}
