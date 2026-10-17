package `in`.hridayan.ashell.portforward.data.engine

import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import java.io.Closeable
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

/** A loopback TCP server that echoes every byte back, for relay tests. */
class EchoServer : Closeable {

    private val server = ServerSocket().apply { bind(InetSocketAddress(InetAddress.getLoopbackAddress(), 0)) }

    val port: Int = server.localPort

    init {
        thread(isDaemon = true) { acceptLoop() }
    }

    private fun acceptLoop() {
        while (!server.isClosed) {
            val client = try {
                server.accept()
            } catch (_: IOException) {
                return
            }
            thread(isDaemon = true) { echo(client) }
        }
    }

    private fun echo(client: Socket) = client.use {
        val buffer = ByteArray(BUFFER_BYTES)
        try {
            while (true) {
                val count = it.getInputStream().read(buffer)
                if (count < 0) return
                it.getOutputStream().write(buffer, 0, count)
            }
        } catch (_: IOException) {
        }
    }

    override fun close() = server.close()

    private companion object {
        const val BUFFER_BYTES = 1024
    }
}

/** Connects to [port] on loopback, failing every connect when [failWith] is set. */
class FakeConnector(private val port: () -> Int, var failWith: Throwable? = null) : TargetConnector {

    override suspend fun connect(
        target: TunnelTarget,
        connectTimeoutMillis: Int
    ): Result<PipeEndpoint> {
        failWith?.let { return Result.failure(it) }
        val socket = Socket(InetAddress.getLoopbackAddress(), port())
        return Result.success(SocketEndpoint(socket))
    }
}

fun freeLoopbackPort(): Int = ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { it.localPort }
