package `in`.hridayan.ashell.portforward.domain.usecase

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.provider.TunnelStatusSummary
import `in`.hridayan.ashell.portforward.domain.deviceSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelState
import `in`.hridayan.ashell.portforward.domain.model.WaitReason
import `in`.hridayan.ashell.portforward.domain.runtime
import `in`.hridayan.ashell.portforward.domain.spec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SummarizeTunnelStatusUseCaseTest {

    private val summarize = SummarizeTunnelStatusUseCase()

    @Test
    fun `nothing started`() {
        assertEquals(TunnelStatusSummary(), summarize(emptyMap(), emptySet()))
    }

    @Test
    fun `healthy tunnels are counted and named`() {
        val runtimes = mapOf(
            "a" to runtime(spec("a", name = "Vite")),
            "b" to runtime(spec("b", name = "Grafana"), TunnelState.Active)
        )
        val summary = summarize(runtimes, setOf("a", "b"))
        assertEquals(2, summary.runningCount)
        assertEquals(listOf("Vite", "Grafana"), summary.runningNames)
        assertFalse(summary.needsAttention)
    }

    @Test
    fun `a failed tunnel needs attention but is not running`() {
        val runtimes = mapOf("a" to runtime(spec("a"), TunnelState.Failed(TunnelError.PortPrivileged)))
        val summary = summarize(runtimes, emptySet())
        assertEquals(0, summary.runningCount)
        assertTrue(summary.needsAttention)
    }

    @Test
    fun `device tunnels are counted per transport`() {
        val runtimes = mapOf(
            "a" to runtime(deviceSpec("a", ExternalDeviceTransport.OTG)),
            "b" to runtime(deviceSpec("b", ExternalDeviceTransport.OTG), TunnelState.Waiting(WaitReason.Device)),
            "c" to runtime(
                deviceSpec("c", ExternalDeviceTransport.WIFI_ADB),
                TunnelState.Failed(TunnelError.DeviceDidNotReturn)
            ),
            "d" to runtime(spec("d"))
        )
        val summary = summarize(runtimes, emptySet())
        assertEquals(2, summary.runningOn(ExternalDeviceTransport.OTG))
        assertEquals(0, summary.runningOn(ExternalDeviceTransport.WIFI_ADB))
    }

    @Test
    fun `a tunnel stopped by the system needs attention`() {
        assertTrue(summarize(emptyMap(), setOf("a")).needsAttention)
    }
}
