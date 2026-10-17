package `in`.hridayan.ashell.portforward.domain.usecase

import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.BindException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MapTunnelErrorUseCaseTest {

    private val map = MapTunnelErrorUseCase()

    @Test
    fun `unknown host`() {
        assertEquals(TunnelError.HostUnresolved, map.forConnect(UnknownHostException("nas.local")))
    }

    @Test
    fun `connect timeout`() {
        assertEquals(TunnelError.TargetTimeout, map.forConnect(SocketTimeoutException("timed out")))
    }

    @Test
    fun `no route to host is treated as unreachable`() {
        assertEquals(TunnelError.TargetTimeout, map.forConnect(NoRouteToHostException("no route")))
    }

    @Test
    fun `refused connection`() {
        assertEquals(
            TunnelError.TargetRefused,
            map.forConnect(ConnectException("ECONNREFUSED (Connection refused)"))
        )
    }

    @Test
    fun `unreachable network`() {
        assertEquals(
            TunnelError.NoNetwork,
            map.forConnect(ConnectException("ENETUNREACH (Network is unreachable)"))
        )
    }

    @Test
    fun `unknown connect failure keeps its message`() {
        assertEquals(TunnelError.Unexpected("boom"), map.forConnect(IOException("boom")))
    }

    @Test
    fun `bind permission denied is privileged`() {
        assertEquals(
            TunnelError.PortPrivileged,
            map.forBind(BindException("bind failed: EACCES (Permission denied)"), null)
        )
    }

    @Test
    fun `bind address in use suggests the next port`() {
        assertEquals(
            TunnelError.PortInUse(8081),
            map.forBind(BindException("bind failed: EADDRINUSE (Address already in use)"), 8081)
        )
    }

    @Test
    fun `other bind failure is unexpected`() {
        assertEquals(TunnelError.Unexpected("odd"), map.forBind(IOException("odd"), null))
    }
}
