package `in`.hridayan.ashell.mirror.domain.protocol

import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import `in`.hridayan.ashell.mirror.domain.quality.TargetDevice
import org.junit.Assert.assertEquals
import org.junit.Test

class TargetProbeParserTest {

    private val android8 = listOf(
        "Physical size: 1080x1920",
        "  mDisplayModes=[DisplayModeRecord{mMode={id=1, width=1080, height=1920, fps=60.0}}]",
        "PEAK=null",
        "SERIAL=ZY2234"
    )

    private val android14 = listOf(
        "Physical size: 1344x2992",
        "    DisplayModeRecord{mMode={id=1, width=1344, height=2992, fps=60.000004, vsync=60.000004, " +
            "synthetic=false, alternativeRefreshRates=[120.00001], supportedHdrTypes=[2, 3, 4]}}",
        "    DisplayModeRecord{mMode={id=2, width=1344, height=2992, fps=120.00001, vsync=120.00001, " +
            "synthetic=false, alternativeRefreshRates=[60.000004], supportedHdrTypes=[2, 3, 4]}}",
        "    DisplayModeRecord{mMode={id=3, width=1008, height=2244, fps=120.00001, vsync=120.00001}}",
        "PEAK=Infinity",
        "SERIAL=3A281FDH"
    )

    @Test
    fun `an old single-mode display`() {
        assertEquals(
            TargetDevice(serial = "ZY2234", screen = VideoSize(1080, 1920), refreshRate = 60),
            TargetProbeParser.parse(android8)
        )
    }

    @Test
    fun `the highest refresh rate at the screen's own size`() {
        assertEquals(
            TargetDevice(serial = "3A281FDH", screen = VideoSize(1344, 2992), refreshRate = 120),
            TargetProbeParser.parse(android14)
        )
    }

    @Test
    fun `a peak refresh setting caps the rate`() {
        val capped = android14.map { if (it.startsWith("PEAK=")) "PEAK=60.0" else it }

        assertEquals(60, TargetProbeParser.parse(capped).refreshRate)
    }

    @Test
    fun `an override size wins over the physical size`() {
        val overridden = listOf("Physical size: 1440x3200", "Override size: 1080x2400") + android8.drop(1)

        assertEquals(VideoSize(1080, 2400), TargetProbeParser.parse(overridden).screen)
    }

    @Test
    fun `modes of another panel are ignored when the screen's own are listed`() {
        val foldable = listOf(
            "Physical size: 1080x2092",
            "DisplayModeRecord{mMode={id=1, width=1080, height=2092, fps=120.0}}",
            "DisplayModeRecord{mMode={id=2, width=2208, height=1840, fps=144.0}}"
        )

        assertEquals(120, TargetProbeParser.parse(foldable).refreshRate)
    }

    @Test
    fun `missing or empty values are left unknown`() {
        assertEquals(TargetDevice.UNKNOWN, TargetProbeParser.parse(listOf("PEAK=", "SERIAL=", "garbage")))
    }
}
