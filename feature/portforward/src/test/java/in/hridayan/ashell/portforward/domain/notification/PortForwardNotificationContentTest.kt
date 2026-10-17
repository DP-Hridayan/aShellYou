package `in`.hridayan.ashell.portforward.domain.notification

import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelState
import `in`.hridayan.ashell.portforward.domain.model.WaitReason
import `in`.hridayan.ashell.portforward.domain.runtime
import `in`.hridayan.ashell.portforward.domain.spec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PortForwardNotificationContentTest {

    @Test
    fun `nothing running shows nothing`() {
        assertNull(PortForwardNotificationContent.from(emptyList()))
    }

    @Test
    fun `failed tunnels do not count as running`() {
        val failed = runtime(spec("a"), TunnelState.Failed(TunnelError.PortInUse(null)))
        assertNull(PortForwardNotificationContent.from(listOf(failed)))
    }

    @Test
    fun `one tunnel shows its addresses and browser link`() {
        val content = PortForwardNotificationContent.from(
            listOf(runtime(spec("a", name = "Vite", path = "/admin"), TunnelState.Active))
        )
        assertEquals(
            PortForwardNotificationContent.Single(
                tunnelId = "a",
                name = "Vite",
                localAddress = "localhost:5173",
                targetAddress = "192.168.1.10:5173",
                status = NotificationTunnelStatus.Active,
                openUrl = "http://localhost:5173/admin"
            ),
            content
        )
    }

    @Test
    fun `non http tunnel has no browser link`() {
        val content = PortForwardNotificationContent.from(listOf(runtime(spec("a", looksLikeHttp = false))))
        assertNull((content as PortForwardNotificationContent.Single).openUrl)
    }

    @Test
    fun `several tunnels are counted by status`() {
        val content = PortForwardNotificationContent.from(
            listOf(
                runtime(spec("a"), TunnelState.Active),
                runtime(spec("b"), TunnelState.Waiting(WaitReason.Network)),
                runtime(spec("c"), TunnelState.Warning(TunnelError.TargetRefused))
            )
        ) as PortForwardNotificationContent.Multiple
        assertEquals(3, content.runningCount)
        assertEquals(1, content.waitingCount)
        assertEquals(1, content.attentionCount)
        assertEquals(0, content.hiddenCount)
    }

    @Test
    fun `lines are capped with a hidden count`() {
        val content = PortForwardNotificationContent.from(
            (1..7).map { runtime(spec("$it")) }
        ) as PortForwardNotificationContent.Multiple
        assertEquals(PortForwardNotificationContent.MAX_LINES, content.lines.size)
        assertEquals(2, content.hiddenCount)
    }

    @Test
    fun `content ignores counters`() {
        val quiet = runtime(spec("a"), TunnelState.Active)
        val busy = quiet.copy(stats = quiet.stats.copy(bytesReceived = 1_000_000, totalConnections = 40))
        assertEquals(
            PortForwardNotificationContent.from(listOf(quiet)),
            PortForwardNotificationContent.from(listOf(busy))
        )
    }
}
