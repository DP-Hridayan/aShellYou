package `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository

import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.TcpIpRequestOutcome
import org.junit.Assert.assertEquals
import org.junit.Test

class TcpIpOutcomeTest {

    @Test
    fun `the daemon restart reply is accepted`() {
        assertEquals(
            TcpIpRequestOutcome.Accepted,
            tcpIpOutcomeOf(OutputLine("restarting in TCP mode port: 5555"))
        )
    }

    @Test
    fun `the restart reply is matched regardless of case and leading space`() {
        assertEquals(
            TcpIpRequestOutcome.Accepted,
            tcpIpOutcomeOf(OutputLine("  Restarting in TCP mode port: 5555"))
        )
    }

    @Test
    fun `no reply is accepted and left to the port check`() {
        assertEquals(TcpIpRequestOutcome.Accepted, tcpIpOutcomeOf(null))
    }

    @Test
    fun `an error line is refused with its text`() {
        assertEquals(
            TcpIpRequestOutcome.Refused("ADB is not connected"),
            tcpIpOutcomeOf(OutputLine("ADB is not connected", isError = true))
        )
    }

    @Test
    fun `any other daemon reply is refused with its text`() {
        assertEquals(
            TcpIpRequestOutcome.Refused("invalid port"),
            tcpIpOutcomeOf(OutputLine("invalid port"))
        )
    }
}
