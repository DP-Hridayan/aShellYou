package `in`.hridayan.ashell.portforward.data.probe

import `in`.hridayan.ashell.portforward.di.PortForwardDispatcher
import `in`.hridayan.ashell.portforward.domain.repository.PortAvailability
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import javax.inject.Inject

/** Tries a bind on loopback, which also fails while another app listens on every interface. */
class PortAvailabilityImpl @Inject constructor(
    @param:PortForwardDispatcher private val dispatcher: CoroutineDispatcher
) : PortAvailability {

    override suspend fun isFree(port: Int): Boolean = withContext(dispatcher) { canBind(port) }

    override suspend fun nextFree(port: Int): Int? = withContext(dispatcher) {
        val last = minOf(port + SEARCH_RANGE, MAX_PORT)
        ((port + 1)..last).firstOrNull { canBind(it) }
    }

    private fun canBind(port: Int): Boolean = try {
        ServerSocket().use { it.bind(InetSocketAddress(InetAddress.getLoopbackAddress(), port)) }
        true
    } catch (_: IOException) {
        false
    }

    private companion object {
        const val SEARCH_RANGE = 100
        const val MAX_PORT = 65535
    }
}
