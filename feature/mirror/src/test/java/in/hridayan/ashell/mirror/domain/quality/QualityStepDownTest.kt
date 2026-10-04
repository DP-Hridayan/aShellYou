package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.mirror.domain.model.MirrorOptions
import `in`.hridayan.ashell.mirror.domain.model.MirrorQualityPreset
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QualityStepDownTest {

    private fun resolved(longEdge: Int, fps: Int) = ResolvedQuality(
        options = MirrorOptions(maxSize = longEdge, videoBitRate = 1, maxFps = fps),
        expectedSize = VideoSize(longEdge / 2, longEdge),
        limit = QualityLimit.NONE
    )

    @Test
    fun `above balanced steps down to balanced`() {
        assertEquals(QualityChoice.Preset(MirrorQualityPreset.BALANCED), QualityStepDown.lower(resolved(2400, 60)))
    }

    @Test
    fun `balanced sized but faster than saver steps down to saver`() {
        assertEquals(QualityChoice.Preset(MirrorQualityPreset.SAVER), QualityStepDown.lower(resolved(1920, 60)))
    }

    @Test
    fun `a high frame rate at balanced size steps to balanced`() {
        assertEquals(QualityChoice.Preset(MirrorQualityPreset.BALANCED), QualityStepDown.lower(resolved(1920, 120)))
    }

    @Test
    fun `nothing is lower than saver`() {
        assertNull(QualityStepDown.lower(resolved(1280, 30)))
    }
}
