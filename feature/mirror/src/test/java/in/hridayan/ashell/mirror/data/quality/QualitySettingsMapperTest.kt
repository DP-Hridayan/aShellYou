package `in`.hridayan.ashell.mirror.data.quality

import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.quality.BitrateChoice
import `in`.hridayan.ashell.mirror.domain.quality.CustomQuality
import `in`.hridayan.ashell.mirror.domain.quality.FrameRateChoice
import `in`.hridayan.ashell.mirror.domain.quality.QualityMode
import `in`.hridayan.ashell.mirror.domain.quality.QualitySelection
import `in`.hridayan.ashell.mirror.domain.quality.ResolutionChoice
import org.junit.Assert.assertEquals
import org.junit.Test

class QualitySettingsMapperTest {

    @Test
    fun `every field survives a round trip`() {
        val selections = listOf(
            QualitySelection(),
            QualitySelection(mode = QualityMode.SHARP),
            QualitySelection(
                mode = QualityMode.CUSTOM,
                custom = CustomQuality(
                    resolution = ResolutionChoice.LongEdge(1600),
                    frameRate = FrameRateChoice.Fixed(90),
                    bitrate = BitrateChoice.Fixed(16),
                    codec = VideoCodec.H265
                )
            ),
            QualitySelection(mode = QualityMode.CUSTOM, custom = CustomQuality(resolution = ResolutionChoice.Native))
        )

        selections.forEach { assertEquals(it, QualitySettingsMapper.fromStored(QualitySettingsMapper.toStored(it))) }
    }

    @Test
    fun `presets keep the custom fields for later`() {
        val custom = CustomQuality(frameRate = FrameRateChoice.Fixed(30))
        val stored = QualitySettingsMapper.toStored(QualitySelection(QualityMode.SAVER, custom))

        assertEquals(custom, QualitySettingsMapper.fromStored(stored).custom)
    }

    @Test
    fun `unknown stored values fall back to auto`() {
        val stored = StoredQuality(mode = "turbo", resolution = -7, frameRate = -1, bitrate = -1, codec = "vp9")

        assertEquals(QualitySelection(), QualitySettingsMapper.fromStored(stored))
    }
}
