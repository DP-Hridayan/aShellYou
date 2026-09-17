package `in`.hridayan.ashell.shell.wifi_adb_shell.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundServiceGateTest {

    private val gate = ForegroundServiceGate()

    @Test
    fun `first start request goes through`() {
        assertTrue(gate.onStartRequested())
    }

    @Test
    fun `a second start while one is in flight does not go through`() {
        gate.onStartRequested()
        assertFalse(gate.onStartRequested())
    }

    @Test
    fun `a start while the service is running does not go through`() {
        gate.onStartRequested()
        gate.onForegroundStarted()
        assertFalse(gate.onStartRequested())
    }

    @Test
    fun `stopping with nothing started is ignored`() {
        assertEquals(ServiceStopAction.Ignore, gate.onStopRequested())
    }

    @Test
    fun `stopping before the foreground is reached waits for the start`() {
        gate.onStartRequested()
        assertEquals(ServiceStopAction.AwaitStart, gate.onStopRequested())
    }

    @Test
    fun `the deferred stop is consumed when the foreground is reached`() {
        gate.onStartRequested()
        gate.onStopRequested()
        assertTrue(gate.onForegroundStarted())
    }

    @Test
    fun `the deferred stop is consumed only once`() {
        gate.onStartRequested()
        gate.onStopRequested()
        gate.onForegroundStarted()

        gate.onStartRequested()
        assertFalse(gate.onForegroundStarted())
    }

    @Test
    fun `a start clears a stop that was never consumed`() {
        gate.onStartRequested()
        gate.onStopRequested()

        gate.onStartRequested()
        assertFalse(gate.onForegroundStarted())
    }

    @Test
    fun `stopping after the foreground is reached goes through`() {
        gate.onStartRequested()
        gate.onForegroundStarted()
        assertEquals(ServiceStopAction.StopService, gate.onStopRequested())
    }

    @Test
    fun `reaching the foreground without a pending stop keeps the service running`() {
        gate.onStartRequested()
        assertFalse(gate.onForegroundStarted())
        assertTrue(gate.isForegroundStarted)
    }

    @Test
    fun `a stopped service can be started again`() {
        gate.onStartRequested()
        gate.onForegroundStarted()
        gate.onStopRequested()

        assertTrue(gate.onStartRequested())
    }

    @Test
    fun `destruction clears the state so the next start goes through`() {
        gate.onStartRequested()
        gate.onForegroundStarted()
        gate.onDestroyed()

        assertFalse(gate.isForegroundStarted)
        assertTrue(gate.onStartRequested())
    }

    @Test
    fun `the crash sequence no longer stops the service from outside`() {
        assertTrue(gate.onStartRequested())
        assertEquals(ServiceStopAction.AwaitStart, gate.onStopRequested())
        assertTrue(gate.onForegroundStarted())
        assertFalse(gate.isForegroundStarted)
    }
}
