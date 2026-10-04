package `in`.hridayan.ashell.mirror.presentation.viewmodel

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.mirror.domain.model.MirrorError
import `in`.hridayan.ashell.mirror.domain.model.MirrorState
import `in`.hridayan.ashell.mirror.domain.model.StreamStats
import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import `in`.hridayan.ashell.mirror.domain.quality.CustomQuality
import `in`.hridayan.ashell.mirror.domain.quality.DecoderCapability
import `in`.hridayan.ashell.mirror.domain.quality.QualityMode
import `in`.hridayan.ashell.mirror.domain.quality.QualityResolver
import `in`.hridayan.ashell.mirror.domain.quality.QualitySelection
import `in`.hridayan.ashell.mirror.domain.quality.TargetDevice
import `in`.hridayan.ashell.mirror.domain.quality.ViewerProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QualityControllerTest {

    private val viewer = ViewerProfile(
        screen = VideoSize(1080, 2400),
        refreshRate = 120,
        decoder = DecoderCapability { _, _, _ -> true },
        supportedCodecs = setOf(VideoCodec.H264, VideoCodec.H265)
    )
    private val repository = FakeMirrorRepository()
    private val qualityRepository = FakeQualityRepository(viewer)
    private val device = TargetDevice("serial", VideoSize(1440, 3200), 120)
    private var restarts = 0

    private fun TestScope.controller() = QualityController(
        transport = ExternalDeviceTransport.OTG,
        repository = repository,
        qualityRepository = qualityRepository,
        resolver = QualityResolver(),
        scope = backgroundScope,
        restartSession = { restarts++ }
    )

    private fun test(body: suspend TestScope.() -> Unit) = runTest(UnconfinedTestDispatcher(), testBody = body)

    @Test
    fun `auto options come from the reported device and this phone`() = test {
        val controller = controller()
        val options = controller.optionsForNextSession(device)

        assertEquals(2400, options.maxSize)
        assertEquals(120, options.maxFps)
        assertEquals(options, controller.state.value.resolved?.options)
    }

    @Test
    fun `an empty report keeps the last known screen`() = test {
        val controller = controller()
        val first = controller.optionsForNextSession(device)

        assertEquals(first, controller.optionsForNextSession(TargetDevice.UNKNOWN))
    }

    @Test
    fun `applying a new selection saves it and restarts once`() = test {
        val controller = controller()
        val saver = QualitySelection(QualityMode.SAVER)
        controller.onApplyQuality(saver)
        controller.onApplyQuality(saver)

        assertEquals(saver, qualityRepository.selections.value)
        assertEquals(1, restarts)
    }

    @Test
    fun `an encoder fallback is remembered for the device`() = test {
        val controller = controller()
        controller.optionsForNextSession(device)
        controller.onSession(MirrorState.Streaming("Pixel", VideoSize(864, 1920), isControlAvailable = true))

        assertEquals(1920, qualityRepository.limits["serial"])
        assertEquals(1920, controller.optionsForNextSession(device).maxSize)
    }

    @Test
    fun `a codec the device can't encode falls back to H264 once`() = test {
        qualityRepository.selections.value =
            QualitySelection(QualityMode.CUSTOM, CustomQuality(codec = VideoCodec.H265))
        val controller = controller()
        controller.optionsForNextSession(device)
        controller.onSession(MirrorState.Failed(MirrorError.EncoderFailed(emptyList())))

        assertEquals(1, restarts)
        assertEquals(VideoCodec.H265, controller.state.value.codecFallback)
        assertEquals(VideoCodec.H264, controller.optionsForNextSession(device).videoCodec)

        controller.onSession(MirrorState.Failed(MirrorError.EncoderFailed(emptyList())))
        assertEquals(1, restarts)
    }

    @Test
    fun `a codec this phone can't decode is never asked for`() = test {
        qualityRepository.selections.value =
            QualitySelection(QualityMode.CUSTOM, CustomQuality(codec = VideoCodec.AV1))

        assertEquals(VideoCodec.H264, controller().optionsForNextSession(device).videoCodec)
    }

    @Test
    fun `sustained drops offer a lower quality that one tap applies`() = test {
        val controller = controller()
        controller.optionsForNextSession(device)
        repeat(5) { second -> repository.stats.value = struggling(second) }

        assertTrue(controller.state.value.isStruggleHintPending)
        controller.onStruggleHintShown()
        controller.onLowerQuality()

        assertEquals(QualityMode.BALANCED, qualityRepository.selections.value.mode)
        assertEquals(1, restarts)
    }

    private fun struggling(second: Int) =
        StreamStats(framesRendered = 40, framesDropped = 20, bitsPerSecond = second.toLong())

    @Test
    fun `stats are cleared with the stream`() = test {
        val controller = controller()
        repository.stats.value = StreamStats(1, 0, 1)
        repository.stats.value = null

        assertNull(controller.state.value.stats)
    }
}
