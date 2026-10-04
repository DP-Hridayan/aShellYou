package `in`.hridayan.ashell.mirror.data.quality

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import `in`.hridayan.ashell.mirror.domain.quality.DecoderCapability
import `in`.hridayan.ashell.mirror.domain.quality.ViewerProfile
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Reads this phone's screen and decoders.
 *
 * The default display is used, which is the screen the mirror is shown on everywhere except
 * desktop-style external display setups.
 */
class ViewerProfileReader @Inject constructor(@param:ApplicationContext private val context: Context) {

    fun read(): ViewerProfile {
        val display = context.getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)
        val mode = display.mode
        val decoders = VideoCodec.entries.mapNotNull { codec ->
            PlatformDecoder.forMimeType(codec.mimeType)?.let { codec to it }
        }.toMap()

        return ViewerProfile(
            screen = VideoSize(mode.physicalWidth, mode.physicalHeight),
            refreshRate = highestRefreshRate(display, mode).roundToInt(),
            decoder = DecoderCapability { codec, size, fps -> decoders[codec]?.canDecode(size, fps) == true },
            supportedCodecs = decoders.keys
        )
    }

    /** Only modes at the current resolution count: switching resolution is not this app's call. */
    private fun highestRefreshRate(display: Display, current: Display.Mode): Float = display.supportedModes
        .filter { it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight }
        .maxOfOrNull { it.refreshRate }
        ?: current.refreshRate
}
