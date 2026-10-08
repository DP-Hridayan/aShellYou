package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.mirror.domain.model.StreamStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StruggleDetectorTest {

    private val struggling = StreamStats(framesRendered = 40, framesDropped = 20, bitsPerSecond = 1)
    private val healthy = StreamStats(framesRendered = 60, framesDropped = 1, bitsPerSecond = 1)
    private val still = StreamStats(framesRendered = 2, framesDropped = 3, bitsPerSecond = 1)

    @Test
    fun `five struggling seconds in a row fire once`() {
        val detector = StruggleDetector()
        val fired = List(8) { detector.onSample(struggling) }

        assertEquals(listOf(false, false, false, false, true, false, false, false), fired)
    }

    @Test
    fun `a healthy second resets the streak`() {
        val detector = StruggleDetector()
        repeat(4) { detector.onSample(struggling) }
        detector.onSample(healthy)

        assertFalse((1..4).map { detector.onSample(struggling) }.any { it })
        assertTrue(detector.onSample(struggling))
    }

    @Test
    fun `a still screen is not struggling however its few frames go`() {
        val detector = StruggleDetector()

        assertFalse(List(10) { detector.onSample(still) }.any { it })
    }
}
