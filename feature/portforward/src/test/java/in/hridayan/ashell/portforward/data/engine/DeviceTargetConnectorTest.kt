package `in`.hridayan.ashell.portforward.data.engine

import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceChannel
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.model.RemoteFileStat
import `in`.hridayan.ashell.portforward.data.device.DeviceChannels
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpoint
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.toTunnelError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException

class DeviceTargetConnectorTest {

    private val otg = ScriptedChannel()
    private val wifi = ScriptedChannel()
    private val connector = DeviceTargetConnector(DeviceChannels(otg, wifi), Dispatchers.IO)

    private val target = TunnelTarget.Device(
        transport = ExternalDeviceTransport.OTG,
        deviceId = "serial-1",
        deviceName = "Pixel 7",
        endpoint = DeviceEndpoint(DeviceEndpointType.ABSTRACT, "chrome_devtools_remote")
    )

    private fun errorOf(result: Result<*>): TunnelError = result.exceptionOrNull()!!.toTunnelError()

    @Test
    fun `opens the endpoint's service`() = runBlocking {
        otg.connect("serial-1")

        assertTrue(connector.connect(target).isSuccess)
        assertEquals(listOf("localabstract:chrome_devtools_remote"), otg.opened)
    }

    @Test
    fun `no device is not connected`() = runBlocking {
        assertEquals(TunnelError.DeviceNotConnected, errorOf(connector.connect(target)))
        assertTrue(otg.opened.isEmpty())
    }

    @Test
    fun `another device is not this tunnel's device`() = runBlocking {
        otg.connect("serial-2")

        assertEquals(TunnelError.DeviceNotConnected, errorOf(connector.connect(target)))
    }

    @Test
    fun `a device without a serial is accepted`() = runBlocking {
        otg.connect(serial = null)

        assertTrue(connector.connect(target).isSuccess)
    }

    @Test
    fun `a refused open on a live connection means nothing listens`() = runBlocking {
        otg.connect("serial-1")
        otg.openFailure = ConnectException("Stream open actively rejected by remote peer")

        assertEquals(TunnelError.NothingListeningOnDevice, errorOf(connector.connect(target)))
    }

    @Test
    fun `a refused open on a dropped connection means the device left`() = runBlocking {
        otg.connect("serial-1")
        otg.openFailure = ConnectException("Stream open actively rejected by remote peer")
        otg.linkAlive = false

        assertEquals(TunnelError.DeviceNotConnected, errorOf(connector.connect(target)))
    }

    @Test
    fun `an adb payload larger than the buffer is read in pieces`() = runBlocking {
        val endpoint = AdbStreamEndpoint(ScriptedStream(listOf(byteArrayOf(1, 2, 3, 4, 5))))
        val buffer = ByteArray(2)

        assertEquals(2, endpoint.read(buffer))
        assertArrayEquals(byteArrayOf(1, 2), buffer)
        assertEquals(2, endpoint.read(buffer))
        assertArrayEquals(byteArrayOf(3, 4), buffer)
        assertEquals(1, endpoint.read(buffer))
        assertEquals(5.toByte(), buffer[0])
        assertEquals(-1, endpoint.read(buffer))
    }

    private class ScriptedChannel : ExternalDeviceChannel {
        val opened = mutableListOf<String>()
        var openFailure: Exception? = null
        var linkAlive = true

        override val connectionState = MutableStateFlow<ExternalDeviceState>(ExternalDeviceState.Disconnected)

        override val isConnected: Boolean
            get() = linkAlive && connectionState.value is ExternalDeviceState.Connected

        fun connect(serial: String?) {
            connectionState.value = ExternalDeviceState.Connected(serial, "Pixel 7")
        }

        override suspend fun openStream(service: String, ioDispatcher: CoroutineDispatcher): Result<AdbDuplexStream> {
            opened += service
            openFailure?.let { return Result.failure(it) }
            return Result.success(ScriptedStream(emptyList()))
        }

        override suspend fun stat(remotePath: String): Result<RemoteFileStat> = Result.failure(IOException())

        override suspend fun push(source: InputStream, remotePath: String, mode: Int): Result<Unit> =
            Result.failure(IOException())
    }

    private class ScriptedStream(payloads: List<ByteArray>) : AdbDuplexStream {
        private val remaining = ArrayDeque(payloads)

        override suspend fun read(): ByteArray = remaining.removeFirstOrNull() ?: throw IOException("closed")

        override suspend fun write(data: ByteArray) = Unit

        override fun close() = Unit
    }
}
