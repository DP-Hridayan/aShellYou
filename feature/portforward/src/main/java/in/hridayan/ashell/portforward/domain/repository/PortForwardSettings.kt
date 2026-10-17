package `in`.hridayan.ashell.portforward.domain.repository

/**
 * The port-forwarding settings, read when a tunnel or connection needs them.
 *
 * @property waitTimeoutMillis how long a device tunnel waits for its device, or null for no limit.
 */
data class PortForwardSettings(
    val connectTimeoutMillis: Int,
    val maxConnections: Int,
    val waitTimeoutMillis: Long?,
    val resumeOnReconnect: Boolean,
    val allowLanSharing: Boolean,
    val autoStart: Boolean,
    val stopOnTaskRemoved: Boolean,
    val unexpectedStopAlerts: Boolean
)

interface PortForwardSettingsSource {
    suspend fun current(): PortForwardSettings
}
