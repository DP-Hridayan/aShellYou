package `in`.hridayan.ashell.mirror.domain.geometry

import `in`.hridayan.ashell.mirror.domain.model.DevicePosition
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VideoViewportTest {

    private val portraitVideo = VideoSize(1080, 2400)

    @Test
    fun `a tall video in a wide view is pillarboxed and centred`() {
        val viewport = VideoViewport(1000f, 1000f, portraitVideo, edgeClampPx = 0f)

        assertEquals(450f, viewport.contentWidth, 0.01f)
        assertEquals(1000f, viewport.contentHeight, 0.01f)
        assertEquals(275f, viewport.contentLeft, 0.01f)
        assertEquals(0f, viewport.contentTop, 0.01f)
    }

    @Test
    fun `a wide video in a tall view is letterboxed and centred`() {
        val viewport = VideoViewport(1000f, 1000f, VideoSize(2000, 1000), edgeClampPx = 0f)

        assertEquals(500f, viewport.contentHeight, 0.01f)
        assertEquals(250f, viewport.contentTop, 0.01f)
    }

    @Test
    fun `the centre of the view maps to the centre of the device`() {
        val viewport = VideoViewport(1000f, 1000f, portraitVideo, edgeClampPx = 0f)

        assertEquals(DevicePosition(540, 1200, portraitVideo), viewport.mapStart(500f, 500f))
    }

    @Test
    fun `a start just outside the video is clamped onto its edge`() {
        val viewport = VideoViewport(1000f, 1000f, portraitVideo, edgeClampPx = 24f)

        val position = viewport.mapStart(260f, 500f)

        assertEquals(DevicePosition(0, 1200, portraitVideo), position)
    }

    @Test
    fun `a start far outside the video is ignored`() {
        val viewport = VideoViewport(1000f, 1000f, portraitVideo, edgeClampPx = 24f)

        assertNull(viewport.mapStart(100f, 500f))
    }

    @Test
    fun `a drag that leaves the video keeps reporting its edge`() {
        val viewport = VideoViewport(1000f, 1000f, portraitVideo, edgeClampPx = 24f)

        assertEquals(DevicePosition(1079, 0, portraitVideo), viewport.mapContinuation(990f, -50f))
    }
}
