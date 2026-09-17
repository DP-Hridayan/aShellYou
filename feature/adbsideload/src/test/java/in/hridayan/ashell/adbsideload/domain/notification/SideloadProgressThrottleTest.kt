package `in`.hridayan.ashell.adbsideload.domain.notification

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SideloadProgressThrottleTest {

    private var now = 0L
    private val throttle = SideloadProgressThrottle(minIntervalMs = INTERVAL, clock = { now })

    @Test
    fun `the first update is always published`() {
        assertTrue(throttle.shouldPublish(determinate(0)))
    }

    @Test
    fun `an unchanged percent is not republished`() {
        throttle.shouldPublish(determinate(10))
        now += INTERVAL * 2

        assertFalse(throttle.shouldPublish(determinate(10)))
    }

    @Test
    fun `a change within the interval is held back`() {
        throttle.shouldPublish(determinate(10))
        now += INTERVAL / 2

        assertFalse(throttle.shouldPublish(determinate(11)))
    }

    @Test
    fun `a change after the interval is published`() {
        throttle.shouldPublish(determinate(10))
        now += INTERVAL

        assertTrue(throttle.shouldPublish(determinate(11)))
    }

    @Test
    fun `completion is published immediately`() {
        throttle.shouldPublish(determinate(98))

        assertTrue(throttle.shouldPublish(determinate(100)))
    }

    @Test
    fun `switching to a busy bar is published immediately`() {
        throttle.shouldPublish(determinate(40))

        assertTrue(throttle.shouldPublish(SideloadNotificationUpdate(isIndeterminate = true)))
    }

    @Test
    fun `leaving the busy bar is published immediately`() {
        throttle.shouldPublish(SideloadNotificationUpdate(isIndeterminate = true))

        assertTrue(throttle.shouldPublish(determinate(1)))
    }

    private fun determinate(percent: Int) =
        SideloadNotificationUpdate(percent = percent, isIndeterminate = false)

    private companion object {
        const val INTERVAL = 500L
    }
}
