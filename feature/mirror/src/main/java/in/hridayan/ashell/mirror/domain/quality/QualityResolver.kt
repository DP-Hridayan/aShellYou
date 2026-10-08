package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.mirror.domain.model.MirrorOptions
import `in`.hridayan.ashell.mirror.domain.model.MirrorQualityPreset
import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import javax.inject.Inject
import kotlin.math.roundToInt

private val LONG_EDGE_STEPS = listOf(2560, 1920, 1600, 1280, 1024, 800)
private val FRAME_RATE_STEPS = listOf(90, 60, 30)
private const val SMOOTH_SIZE_FLOOR = 1280
private const val NATIVE_SIZE = 0
private const val DEFAULT_REFRESH_RATE = 60
private const val USB_FRAME_RATE_CAP = 120
private const val WIFI_FRAME_RATE_CAP = 60
private const val USB_MEGABITS_CAP = 24
private const val WIFI_MEGABITS_CAP = 8
private const val MIN_MEGABITS = 2
private const val MEGABIT = 1_000_000
private const val FULL_WEIGHT_FPS = 60
private const val EXTRA_FRAME_WEIGHT = 0.5
private const val H264_BITS_PER_PIXEL = 0.07
private const val H265_BITS_PER_PIXEL = 0.045
private const val AV1_BITS_PER_PIXEL = 0.04

/**
 * Turns a [QualityChoice] into the server options for one session.
 *
 * Auto streams at the lowest of the device's refresh rate, this phone's and the transport's cap,
 * at the largest size that fits on both screens. When this phone's decoder can't keep up, it keeps
 * the frame rate and gives up size, down to a 1280 px long edge, before it drops the frame rate.
 * Sizes step through the server's own fallback list, so a later server fallback lands on a size
 * this would also have chosen.
 */
class QualityResolver @Inject constructor() {

    fun resolve(request: QualityRequest): ResolvedQuality = when (val choice = request.choice) {
        QualityChoice.Auto -> resolveCustom(CustomQuality(), request)
        is QualityChoice.Custom -> resolveCustom(choice.quality, request)
        is QualityChoice.Preset -> resolvePreset(choice.preset, request)
    }

    private fun resolvePreset(preset: MirrorQualityPreset, request: QualityRequest): ResolvedQuality {
        val options = preset.toOptions(request.transport)
        val maxSize = capToEncoder(options.maxSize, request.encoderLimit)
        return ResolvedQuality(
            options = options.copy(maxSize = maxSize),
            expectedSize = request.target.screen?.let { VideoScaling.scaled(it, maxSize) },
            limit = QualityLimit.CHOSEN
        )
    }

    private fun resolveCustom(quality: CustomQuality, request: QualityRequest): ResolvedQuality {
        val bounds = Bounds.of(quality, request)
        val shape = request.target.screen ?: request.viewer.screen
        val fits = { longEdge: Int, fps: Int ->
            request.viewer.decoder.canDecode(quality.codec, VideoScaling.scaled(shape, longEdge), fps)
        }
        val pick = search(
            sizes = sizeCandidates(quality.resolution, bounds.fit),
            rates = frameRateCandidates(quality.frameRate, bounds.frameRateCeiling),
            fit = bounds.fit,
            fits = fits
        )
        val bitRate = bitRate(quality, VideoScaling.scaled(shape, pick.longEdge), pick.fps, request.transport)

        return ResolvedQuality(
            options = MirrorOptions(
                maxSize = pick.longEdge,
                videoBitRate = bitRate.bits,
                maxFps = pick.fps,
                videoCodec = quality.codec
            ),
            expectedSize = request.target.screen?.let { VideoScaling.scaled(it, pick.longEdge) },
            limit = limitOf(quality, request, bounds, pick, bitRate)
        )
    }

    private fun sizeCandidates(resolution: ResolutionChoice, fit: Int): List<Int> = when (resolution) {
        ResolutionChoice.Auto -> listOf(fit) + LONG_EDGE_STEPS.filter { it < fit }
        else -> listOf(fit)
    }

    private fun frameRateCandidates(frameRate: FrameRateChoice, ceiling: Int): List<Int> = when (frameRate) {
        FrameRateChoice.Auto -> listOf(ceiling) + FRAME_RATE_STEPS.filter { it < ceiling }
        else -> listOf(ceiling)
    }

    private fun search(sizes: List<Int>, rates: List<Int>, fit: Int, fits: (Int, Int) -> Boolean): Pick {
        val readable = sizes.filter { it >= minOf(SMOOTH_SIZE_FLOOR, fit) }
        val smooth = rates.firstNotNullOfOrNull { fps ->
            readable.firstOrNull { fits(it, fps) }?.let { Pick(it, fps) }
        }
        if (smooth != null) return smooth

        val slowest = rates.last()
        return Pick(sizes.firstOrNull { fits(it, slowest) } ?: sizes.last(), slowest)
    }

    private fun bitRate(
        quality: CustomQuality,
        size: VideoSize,
        fps: Int,
        transport: ExternalDeviceTransport
    ): BitRate = when (val bitrate = quality.bitrate) {
        is BitrateChoice.Fixed -> BitRate(bitrate.megabits * MEGABIT, isCapped = false)
        BitrateChoice.Auto -> autoBitRate(quality.codec, size, fps, transport)
    }

    /** Screen content gains less from frames above 60 than camera video does, so they count for half. */
    private fun autoBitRate(codec: VideoCodec, size: VideoSize, fps: Int, transport: ExternalDeviceTransport): BitRate {
        val weightedFps = if (fps <= FULL_WEIGHT_FPS) {
            fps.toDouble()
        } else {
            FULL_WEIGHT_FPS + (fps - FULL_WEIGHT_FPS) * EXTRA_FRAME_WEIGHT
        }
        val wanted = (size.width.toLong() * size.height * weightedFps * codec.bitsPerPixel()).roundToInt()
        val cap = transport.megabitsCap() * MEGABIT
        return BitRate(wanted.coerceIn(MIN_MEGABITS * MEGABIT, cap), isCapped = wanted > cap)
    }

    private fun limitOf(
        quality: CustomQuality,
        request: QualityRequest,
        bounds: Bounds,
        pick: Pick,
        bitRate: BitRate
    ): QualityLimit = when {
        pick.longEdge < bounds.fit || pick.fps < bounds.frameRateCeiling -> QualityLimit.VIEWER_DECODER
        bounds.isEncoderBound(request) -> QualityLimit.TARGET_ENCODER
        bitRate.isCapped || bounds.isConnectionBound(quality, request) -> QualityLimit.CONNECTION
        quality.hasFixedValue() -> QualityLimit.CHOSEN
        bounds.isViewerBound(quality, request) -> QualityLimit.VIEWER_SCREEN
        else -> QualityLimit.TARGET_SCREEN
    }

    /** A long edge and frame rate the search settled on. */
    private data class Pick(val longEdge: Int, val fps: Int)

    /** @property isCapped true when Auto wanted more than the transport allows. */
    private data class BitRate(val bits: Int, val isCapped: Boolean)

    /**
     * The most a session could ask for before this phone's decoder has a say.
     *
     * @property fit the long edge both screens and any encoder limit allow, or 0 for native when the
     * device's screen is unknown.
     */
    private data class Bounds(
        val fit: Int,
        val frameRateCeiling: Int,
        val targetRefreshRate: Int,
        val transportFrameRateCap: Int
    ) {
        fun isEncoderBound(request: QualityRequest): Boolean {
            val limit = request.encoderLimit ?: return false
            val native = request.target.screen?.longEdge
            return fit == limit && (native == null || limit < native)
        }

        fun isConnectionBound(quality: CustomQuality, request: QualityRequest): Boolean =
            quality.frameRate == FrameRateChoice.Auto &&
                frameRateCeiling == transportFrameRateCap &&
                transportFrameRateCap < minOf(targetRefreshRate, request.viewer.refreshRate)

        fun isViewerBound(quality: CustomQuality, request: QualityRequest): Boolean {
            val viewer = request.viewer
            val native = request.target.screen?.longEdge
            val sizeBound = quality.resolution == ResolutionChoice.Auto &&
                native != null && fit == viewer.screen.longEdge && viewer.screen.longEdge < native
            val rateBound = quality.frameRate == FrameRateChoice.Auto &&
                frameRateCeiling == viewer.refreshRate && viewer.refreshRate < targetRefreshRate
            return sizeBound || rateBound
        }

        companion object {
            fun of(quality: CustomQuality, request: QualityRequest): Bounds {
                val targetRefreshRate = request.target.refreshRate ?: DEFAULT_REFRESH_RATE
                val transportCap = request.transport.frameRateCap()
                val ceiling = when (val frameRate = quality.frameRate) {
                    is FrameRateChoice.Fixed -> frameRate.fps
                    FrameRateChoice.Auto -> minOf(targetRefreshRate, request.viewer.refreshRate, transportCap)
                }
                return Bounds(
                    fit = capToEncoder(fitLongEdge(quality.resolution, request), request.encoderLimit),
                    frameRateCeiling = ceiling,
                    targetRefreshRate = targetRefreshRate,
                    transportFrameRateCap = transportCap
                )
            }

            private fun fitLongEdge(resolution: ResolutionChoice, request: QualityRequest): Int {
                val native = request.target.screen?.longEdge
                val viewer = request.viewer.screen.longEdge
                return when (resolution) {
                    ResolutionChoice.Auto -> minOf(native ?: viewer, viewer)
                    ResolutionChoice.Native -> native ?: NATIVE_SIZE
                    is ResolutionChoice.LongEdge -> native?.let { minOf(resolution.pixels, it) } ?: resolution.pixels
                }
            }
        }
    }
}

private fun capToEncoder(maxSize: Int, encoderLimit: Int?): Int = when {
    encoderLimit == null -> maxSize
    maxSize <= NATIVE_SIZE -> encoderLimit
    else -> minOf(maxSize, encoderLimit)
}

private fun CustomQuality.hasFixedValue(): Boolean =
    resolution != ResolutionChoice.Auto || frameRate != FrameRateChoice.Auto || bitrate != BitrateChoice.Auto

private fun ExternalDeviceTransport.frameRateCap(): Int = when (this) {
    ExternalDeviceTransport.OTG -> USB_FRAME_RATE_CAP
    ExternalDeviceTransport.WIFI_ADB -> WIFI_FRAME_RATE_CAP
}

private fun ExternalDeviceTransport.megabitsCap(): Int = when (this) {
    ExternalDeviceTransport.OTG -> USB_MEGABITS_CAP
    ExternalDeviceTransport.WIFI_ADB -> WIFI_MEGABITS_CAP
}

private fun VideoCodec.bitsPerPixel(): Double = when (this) {
    VideoCodec.H264 -> H264_BITS_PER_PIXEL
    VideoCodec.H265 -> H265_BITS_PER_PIXEL
    VideoCodec.AV1 -> AV1_BITS_PER_PIXEL
}
