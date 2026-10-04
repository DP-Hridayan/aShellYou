package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.mirror.domain.model.MirrorQualityPreset
import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import org.junit.Assert.assertEquals
import org.junit.Test

class QualityResolverTest {

    private val resolver = QualityResolver()
    private val usb = ExternalDeviceTransport.OTG
    private val wifi = ExternalDeviceTransport.WIFI_ADB

    private val unlimited = DecoderCapability { _, _, _ -> true }
    private val decoder1080p60 = pixelRateDecoder(1920L * 1080 * 60)

    private fun pixelRateDecoder(maxPixelsPerSecond: Long) = DecoderCapability { _, size, fps ->
        size.width.toLong() * size.height * fps <= maxPixelsPerSecond
    }

    private fun viewer(width: Int, height: Int, refreshRate: Int, decoder: DecoderCapability = unlimited) =
        ViewerProfile(VideoSize(width, height), refreshRate, decoder, setOf(VideoCodec.H264, VideoCodec.H265))

    private fun target(width: Int, height: Int, refreshRate: Int?) =
        TargetDevice(serial = "serial", screen = VideoSize(width, height), refreshRate = refreshRate)

    private fun auto(
        target: TargetDevice,
        viewer: ViewerProfile,
        transport: ExternalDeviceTransport = usb,
        encoderLimit: Int? = null
    ) = resolver.resolve(QualityRequest(QualityChoice.Auto, transport, target, viewer, encoderLimit))

    @Test
    fun `a capable viewer gets its own screen size at the shared refresh rate`() {
        val result = auto(target(1440, 3200, 120), viewer(1080, 2400, 120))

        assertEquals(2400, result.options.maxSize)
        assertEquals(120, result.options.maxFps)
        assertEquals(VideoSize(1080, 2400), result.expectedSize)
        assertEquals(QualityLimit.VIEWER_SCREEN, result.limit)
    }

    @Test
    fun `wifi caps the frame rate and the bitrate`() {
        val result = auto(target(1440, 3200, 120), viewer(1080, 2400, 120), transport = wifi)

        assertEquals(60, result.options.maxFps)
        assertEquals(8_000_000, result.options.videoBitRate)
        assertEquals(QualityLimit.CONNECTION, result.limit)
    }

    @Test
    fun `smoothness is kept and size gives way when the decoder is short`() {
        val result = auto(target(1344, 2992, 120), viewer(1080, 2340, 90, decoder1080p60))

        assertEquals(1600, result.options.maxSize)
        assertEquals(90, result.options.maxFps)
        assertEquals(QualityLimit.VIEWER_DECODER, result.limit)
    }

    @Test
    fun `the frame rate drops only when no readable size decodes`() {
        val decoder = pixelRateDecoder(1280L * 576 * 60)
        val result = auto(target(1080, 2400, 120), viewer(1080, 2400, 120, decoder))

        assertEquals(1280, result.options.maxSize)
        assertEquals(60, result.options.maxFps)
        assertEquals(QualityLimit.VIEWER_DECODER, result.limit)
    }

    @Test
    fun `a decoder that takes nothing still gets the smallest stream`() {
        val result = auto(target(1080, 2400, 60), viewer(1080, 2400, 60, DecoderCapability { _, _, _ -> false }))

        assertEquals(800, result.options.maxSize)
        assertEquals(30, result.options.maxFps)
    }

    @Test
    fun `a small target streams natively without upscaling`() {
        val result = auto(target(720, 1600, 60), viewer(1440, 3200, 120))

        assertEquals(1600, result.options.maxSize)
        assertEquals(60, result.options.maxFps)
        assertEquals(VideoSize(720, 1600), result.expectedSize)
        assertEquals(QualityLimit.TARGET_SCREEN, result.limit)
    }

    @Test
    fun `an unknown target is sized for the viewer at 60 fps`() {
        val result = auto(TargetDevice.UNKNOWN, viewer(1080, 2400, 120))

        assertEquals(2400, result.options.maxSize)
        assertEquals(60, result.options.maxFps)
    }

    @Test
    fun `a remembered encoder limit caps the size`() {
        val result = auto(target(1440, 3200, 60), viewer(1440, 3200, 60), encoderLimit = 1600)

        assertEquals(1600, result.options.maxSize)
        assertEquals(QualityLimit.TARGET_ENCODER, result.limit)
    }

    @Test
    fun `auto bitrate follows pixels and frame rate within the usb cap`() {
        val result = auto(target(1080, 2400, 60), viewer(1080, 2400, 60))

        assertEquals(10_886_400, result.options.videoBitRate)
    }

    @Test
    fun `frames above 60 count for half in the bitrate`() {
        val result = auto(target(1080, 2400, 120), viewer(1080, 2400, 120))

        assertEquals(16_329_600, result.options.videoBitRate)
    }

    @Test
    fun `custom fixed values are used as given`() {
        val custom = CustomQuality(
            resolution = ResolutionChoice.LongEdge(1280),
            frameRate = FrameRateChoice.Fixed(120),
            bitrate = BitrateChoice.Fixed(20),
            codec = VideoCodec.H265
        )
        val request = QualityRequest(
            QualityChoice.Custom(custom),
            wifi,
            target(1080, 2400, 60),
            viewer(1080, 2400, 60),
            null
        )
        val result = resolver.resolve(request)

        assertEquals(1280, result.options.maxSize)
        assertEquals(120, result.options.maxFps)
        assertEquals(20_000_000, result.options.videoBitRate)
        assertEquals(VideoCodec.H265, result.options.videoCodec)
        assertEquals(QualityLimit.CHOSEN, result.limit)
    }

    @Test
    fun `custom native sends the device size`() {
        val custom = CustomQuality(resolution = ResolutionChoice.Native)
        val request = QualityRequest(
            QualityChoice.Custom(custom),
            usb,
            target(1440, 3200, 60),
            viewer(1080, 2400, 60),
            null
        )

        assertEquals(3200, resolver.resolve(request).options.maxSize)
    }

    @Test
    fun `a preset keeps its own values`() {
        val request = QualityRequest(
            QualityChoice.Preset(MirrorQualityPreset.SAVER),
            usb,
            target(1080, 2400, 120),
            viewer(1080, 2400, 120),
            null
        )
        val result = resolver.resolve(request)

        assertEquals(MirrorQualityPreset.SAVER.toOptions(usb), result.options)
        assertEquals(QualityLimit.CHOSEN, result.limit)
    }

    @Test
    fun `a preset is still held to a remembered encoder limit`() {
        val request = QualityRequest(
            QualityChoice.Preset(MirrorQualityPreset.SHARP),
            usb,
            target(1440, 3200, 60),
            viewer(1080, 2400, 60),
            1920
        )

        assertEquals(1920, resolver.resolve(request).options.maxSize)
    }
}
