package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EncoderFallbackTest {

    private val screen = VideoSize(1440, 3200)

    @Test
    fun `a stream smaller than asked means the encoder downsized`() {
        assertEquals(
            1920,
            EncoderFallback.detect(requestedMaxSize = 3200, targetScreen = screen, streamed = VideoSize(864, 1920))
        )
    }

    @Test
    fun `native requests compare against the screen`() {
        assertEquals(
            2560,
            EncoderFallback.detect(requestedMaxSize = 0, targetScreen = screen, streamed = VideoSize(1152, 2560))
        )
    }

    @Test
    fun `alignment rounding is not a fallback`() {
        assertNull(
            EncoderFallback.detect(requestedMaxSize = 2400, targetScreen = screen, streamed = VideoSize(1072, 2392))
        )
    }

    @Test
    fun `a rotated stream of the requested size is not a fallback`() {
        assertNull(
            EncoderFallback.detect(requestedMaxSize = 1920, targetScreen = screen, streamed = VideoSize(1920, 864))
        )
    }

    @Test
    fun `nothing is learned for a native request on an unknown screen`() {
        assertNull(EncoderFallback.detect(requestedMaxSize = 0, targetScreen = null, streamed = VideoSize(864, 1920)))
    }
}
