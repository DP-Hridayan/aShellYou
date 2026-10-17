package `in`.hridayan.ashell.portforward.domain.model

/** One line of a tunnel's activity log. */
data class TunnelEvent(val atMillis: Long, val kind: TunnelEventKind)

sealed interface TunnelEventKind {
    data object Started : TunnelEventKind
    data object ConnectionOpened : TunnelEventKind
    data class ConnectionClosed(val bytesReceived: Long, val bytesSent: Long) : TunnelEventKind
    data class ConnectionFailed(val error: TunnelError) : TunnelEventKind
    data class ConnectionRefused(val error: TunnelError) : TunnelEventKind
    data object WaitingForNetwork : TunnelEventKind
    data object NetworkRestored : TunnelEventKind
    data object WaitingForDevice : TunnelEventKind
    data object DeviceReconnected : TunnelEventKind
}
