package `in`.hridayan.ashell.mirror.domain.protocol

import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import `in`.hridayan.ashell.mirror.domain.quality.TargetDevice
import kotlin.math.roundToInt

private const val PHYSICAL = "Physical"
private const val OVERRIDE = "Override"
private const val PEAK_PREFIX = "PEAK="
private const val SERIAL_PREFIX = "SERIAL="
private const val UNSET = "null"

/**
 * Reads what [COMMAND] prints into a [TargetDevice]. Output varies across Android versions and
 * vendors, so anything unrecognised is left unknown rather than guessed.
 */
object TargetProbeParser {

    /**
     * One shell command for the device's screen size, display modes, the user's refresh-rate cap and
     * serial. Each part prints on its own and may fail on its own; plain `grep` keeps it working on
     * old toolbox shells.
     */
    const val COMMAND = "wm size; dumpsys display | grep DisplayModeRecord; " +
        "echo $PEAK_PREFIX\$(settings get system peak_refresh_rate); echo $SERIAL_PREFIX\$(getprop ro.serialno)"

    private val SIZE = Regex("""(\w+) size: (\d+)x(\d+)""")
    private val MODE = Regex("""width=(\d+), height=(\d+), fps=([\d.]+)""")

    fun parse(lines: List<String>): TargetDevice {
        val trimmed = lines.map(String::trim)
        val physical = sizeOf(trimmed, PHYSICAL)
        return TargetDevice(
            serial = valueAfter(trimmed, SERIAL_PREFIX),
            screen = sizeOf(trimmed, OVERRIDE) ?: physical,
            refreshRate = refreshRate(trimmed, physical)
        )
    }

    private fun sizeOf(lines: List<String>, kind: String): VideoSize? = lines.firstNotNullOfOrNull { line ->
        SIZE.matchEntire(line)
            ?.takeIf { it.groupValues[1] == kind }
            ?.let { VideoSize(it.groupValues[2].toInt(), it.groupValues[3].toInt()) }
    }

    /**
     * Modes of other panels, such as a foldable's cover screen, are listed too, so only modes at the
     * screen's own size count when there are any.
     */
    private fun refreshRate(lines: List<String>, physical: VideoSize?): Int? {
        val modes = lines.flatMap { line -> MODE.findAll(line).map(::toMode).toList() }
        val own = modes.filter { physical != null && it.size.sameShapeAs(physical) }.ifEmpty { modes }
        val highest = own.maxOfOrNull { it.fps } ?: return null
        val peak = valueAfter(lines, PEAK_PREFIX)?.toFloatOrNull()?.takeIf { it > 0f && it.isFinite() }
        return minOf(highest, peak ?: highest).roundToInt()
    }

    private fun toMode(match: MatchResult): Mode = Mode(
        size = VideoSize(match.groupValues[1].toInt(), match.groupValues[2].toInt()),
        fps = match.groupValues[3].toFloat()
    )

    private fun valueAfter(lines: List<String>, prefix: String): String? = lines
        .firstOrNull { it.startsWith(prefix) }
        ?.removePrefix(prefix)
        ?.trim()
        ?.takeIf { it.isNotEmpty() && it != UNSET }

    private fun VideoSize.sameShapeAs(other: VideoSize) = longEdge == other.longEdge && shortEdge == other.shortEdge

    /** One display mode as `dumpsys display` lists it. */
    private data class Mode(val size: VideoSize, val fps: Float)
}
