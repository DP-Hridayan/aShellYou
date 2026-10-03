package `in`.hridayan.ashell.mirror.data.connection

import `in`.hridayan.ashell.mirror.data.FakeExternalDeviceChannel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrcpySocketConnectorTest {

    private val channel = FakeExternalDeviceChannel()
    private val connector = ScrcpySocketConnector()

    @Test
    fun `retries until the server listens, then opens video before control`() = runTest {
        channel.socketRejectionsBeforeAccept = 5

        val sockets = connector.connect(channel, scid = 0x2a)

        assertTrue(sockets.isSuccess)
        assertEquals(7, channel.openedServices.size)
        assertTrue(channel.openedServices.all { it == "localabstract:scrcpy_0000002a" })
    }

    @Test
    fun `gives up when the server never listens`() = runTest {
        channel.socketRejectionsBeforeAccept = Int.MAX_VALUE

        val sockets = connector.connect(channel, scid = 1)

        assertTrue(sockets.isFailure)
    }
}
