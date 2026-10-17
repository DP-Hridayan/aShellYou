package `in`.hridayan.ashell.portforward.domain.usecase

import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import java.net.BindException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Turns socket exceptions into [TunnelError]s.
 *
 * Android reports the errno only in the exception message, so refused and unreachable connections,
 * and denied binds, are told apart by the errno names it contains.
 */
class MapTunnelErrorUseCase @Inject constructor() {

    fun forConnect(error: Throwable): TunnelError = when (error) {
        is UnknownHostException -> TunnelError.HostUnresolved
        is SocketTimeoutException, is NoRouteToHostException -> TunnelError.TargetTimeout
        is ConnectException -> connectError(error)
        else -> TunnelError.Unexpected(error.message)
    }

    /** @param suggestedPort a free port to offer when the requested one is taken. */
    fun forBind(error: Throwable, suggestedPort: Int?): TunnelError = when {
        error !is BindException -> TunnelError.Unexpected(error.message)
        error.mentions(ERRNO_ACCESS) -> TunnelError.PortPrivileged
        else -> TunnelError.PortInUse(suggestedPort)
    }

    private fun connectError(error: ConnectException): TunnelError = when {
        error.mentions(ERRNO_NETWORK_UNREACHABLE) -> TunnelError.NoNetwork
        error.mentions(ERRNO_HOST_UNREACHABLE) || error.mentions(ERRNO_TIMED_OUT) -> TunnelError.TargetTimeout
        else -> TunnelError.TargetRefused
    }

    private fun Throwable.mentions(errno: String): Boolean = message?.contains(errno) == true

    private companion object {
        const val ERRNO_ACCESS = "EACCES"
        const val ERRNO_NETWORK_UNREACHABLE = "ENETUNREACH"
        const val ERRNO_HOST_UNREACHABLE = "EHOSTUNREACH"
        const val ERRNO_TIMED_OUT = "ETIMEDOUT"
    }
}
