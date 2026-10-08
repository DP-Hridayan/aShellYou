package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoScalingTest {

    @Test
    fun `the long edge is limited and the aspect kept`() {
        assertEquals(VideoSize(864, 1920), VideoScaling.scaled(VideoSize(1080, 2400), 1920))
    }

    @Test
    fun `landscape screens keep their orientation`() {
        assertEquals(VideoSize(1920, 1200), VideoScaling.scaled(VideoSize(2560, 1600), 1920))
    }

    @Test
    fun `the short edge is rounded down to an even number`() {
        assertEquals(VideoSize(718, 1600), VideoScaling.scaled(VideoSize(1344, 2992), 1600))
    }

    @Test
    fun `no limit or a limit above the screen leaves it alone`() {
        assertEquals(VideoSize(720, 1600), VideoScaling.scaled(VideoSize(720, 1600), 0))
        assertEquals(VideoSize(720, 1600), VideoScaling.scaled(VideoSize(720, 1600), 2400))
    }
}
