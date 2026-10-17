package `in`.hridayan.ashell.portforward.domain.model

/**
 * Why a tunnel could not start, or why one of its connections failed.
 *
 * Listener errors stop the tunnel. Connection errors only mark it with a warning, which the next
 * successful connection clears.
 */
sealed interface TunnelError {

    /** @property suggestedPort a nearby free port, or null when none was found. */
    data class PortInUse(val suggestedPort: Int?) : TunnelError
    data object PortPrivileged : TunnelError
    data object LocalNetworkBlocked : TunnelError
    data object BackgroundStartBlocked : TunnelError
    data object NoNetwork : TunnelError
    data object TargetRefused : TunnelError
    data object TargetTimeout : TunnelError
    data object HostUnresolved : TunnelError
    data object StreamCapReached : TunnelError
    data object NothingListeningOnDevice : TunnelError
    data object DeviceOpenTimeout : TunnelError
    data object DeviceNotConnected : TunnelError
    data object DeviceDidNotReturn : TunnelError
    data class Unexpected(val message: String?) : TunnelError
}
