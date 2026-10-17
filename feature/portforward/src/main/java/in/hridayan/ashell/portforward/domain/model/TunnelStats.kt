package `in`.hridayan.ashell.portforward.domain.model

/**
 * Connection counters for a running tunnel. They live only in memory and reset when the tunnel
 * starts.
 *
 * @property bytesReceived bytes delivered to clients on this phone.
 * @property bytesSent bytes sent from clients to the target.
 */
data class TunnelStats(
    val openConnections: Int = 0,
    val totalConnections: Int = 0,
    val failedConnections: Int = 0,
    val refusedConnections: Int = 0,
    val bytesReceived: Long = 0,
    val bytesSent: Long = 0,
    val startedAtMillis: Long = 0
)
