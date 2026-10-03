package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TcpIpEnableResultTest {

    @Test
    fun `an opened port is the one to reach the device on`() {
        assertEquals(PORT, TcpIpEnableResult.Opened(PORT).reachablePort(OTHER_PORT))
    }

    @Test
    fun `an unverified restart falls back to the requested port`() {
        assertEquals(PORT, TcpIpEnableResult.Unverified.reachablePort(PORT))
    }

    @Test
    fun `a refusal leaves the device where it was`() {
        assertNull(TcpIpEnableResult.Refused("no").reachablePort(PORT))
    }

    @Test
    fun `no connection leaves the device where it was`() {
        assertNull(TcpIpEnableResult.NotConnected.reachablePort(PORT))
    }

    private companion object {
        const val PORT = 5555
        const val OTHER_PORT = 5037
    }
}
