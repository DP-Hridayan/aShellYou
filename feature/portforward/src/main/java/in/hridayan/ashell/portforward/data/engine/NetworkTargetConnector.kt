package `in`.hridayan.ashell.portforward.data.engine

import `in`.hridayan.ashell.portforward.di.PortForwardDispatcher
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelException
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.repository.LocalNetworkGate
import `in`.hridayan.ashell.portforward.domain.usecase.MapTunnelErrorUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runInterruptible
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject

/** Connects to a host on the phone's network over plain TCP. */
class NetworkTargetConnector @Inject constructor(
    private val localNetwork: LocalNetworkGate,
    private val mapError: MapTunnelErrorUseCase,
    @param:PortForwardDispatcher private val dispatcher: CoroutineDispatcher
) {

    suspend fun connect(target: TunnelTarget.NetworkHost, timeoutMillis: Int): Result<PipeEndpoint> {
        if (localNetwork.isBlocked(target.host)) {
            return Result.failure(TunnelException(TunnelError.LocalNetworkBlocked))
        }
        return runInterruptible(dispatcher) { openSocket(target, timeoutMillis) }
    }

    private fun openSocket(target: TunnelTarget.NetworkHost, timeoutMillis: Int): Result<PipeEndpoint> {
        val socket = Socket()
        return try {
            socket.tcpNoDelay = true
            socket.connect(InetSocketAddress(target.host, target.port), timeoutMillis)
            Result.success(SocketEndpoint(socket, dispatcher))
        } catch (e: IOException) {
            socket.closeQuietly()
            Result.failure(TunnelException(mapError.forConnect(e)))
        }
    }
}
