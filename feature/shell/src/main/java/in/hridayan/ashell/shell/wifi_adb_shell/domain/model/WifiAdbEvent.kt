package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

/**
 * One-shot events for WiFi ADB operations.
 * 
 * Unlike [WifiAdbState] which represents persistent conditions (Connected, Disconnected, etc.),
 * these events are consumed once and should not persist. Use `SharedFlow` with `replay=0`
 * to collect these events in the UI layer.
 * 
 * ## Usage in Compose:
 * ```kotlin
 * LaunchedEffect(Unit) {
 *     viewModel.events.collect { event ->
 *         when (event) {
 *             is WifiAdbEvent.ConnectSuccess -> showToast("Connected!")
 *             is WifiAdbEvent.ReconnectFailed -> showDialog = true
 *             // ...
 *         }
 *     }
 * }
 * ```
 */
sealed class WifiAdbEvent {
    /**
     * Connection to a device succeeded.
     * @param deviceId Unique identifier for the device (typically serial number)
     * @param address The IP:port address that was connected to
     */
    data class ConnectSuccess(
        val deviceId: String,
        val address: String
    ) : WifiAdbEvent()

    /**
     * Connection to a device failed.
     * @param deviceId Device ID if known, null for new device connections
     * @param error Human-readable error message
     */
    data class ConnectFailed(
        val deviceId: String?,
        val error: String
    ) : WifiAdbEvent()

    /**
     * Device was already connected when connection was attempted.
     * @param deviceId The already-connected device ID
     */
    data class AlreadyConnected(
        val deviceId: String
    ) : WifiAdbEvent()

    /**
     * Reconnection to a saved device succeeded.
     * @param deviceId The reconnected device ID
     */
    data class ReconnectSuccess(
        val deviceId: String
    ) : WifiAdbEvent()

    /**
     * Reconnection to a saved device failed.
     * @param deviceId The device that failed to reconnect
     * @param requiresPairing True if the device needs to be re-paired (keys expired)
     */
    data class ReconnectFailed(
        val deviceId: String,
        val requiresPairing: Boolean = false
    ) : WifiAdbEvent()


    /**
     * Pairing with a device succeeded.
     * @param ip The IP address of the paired device
     */
    data class PairingSuccess(
        val ip: String
    ) : WifiAdbEvent()

    /**
     * Pairing with a device failed.
     * @param error Human-readable error message
     */
    data class PairingFailed(
        val error: String
    ) : WifiAdbEvent()

    /**
     * Pairing succeeded but subsequent connection failed.
     * @param error Human-readable error message
     */
    data class PairConnectFailed(
        val error: String
    ) : WifiAdbEvent()

    /**
     * Wireless debugging is disabled on the target device.
     * @param deviceId The device ID if known (for own device reconnects)
     */
    data class WirelessDebuggingOff(
        val deviceId: String? = null
    ) : WifiAdbEvent()

    /**
     * Wireless debugging was successfully enabled programmatically.
     */
    data object WirelessDebuggingEnabled : WifiAdbEvent()

    /**
     * mDNS discovery found a pairing or connect service.
     * @param serviceName The discovered service name
     * @param ip IP address of the discovered service
     * @param port Port of the discovered service
     */
    data class DiscoveryFound(
        val serviceName: String,
        val ip: String,
        val port: Int
    ) : WifiAdbEvent()

    /**
     * mDNS discovery failed.
     * @param error Human-readable error message
     */
    data class DiscoveryFailed(
        val error: String
    ) : WifiAdbEvent()

    /**
     * Discovery session matched (for QR pairing flow).
     */
    data object DiscoverySessionMatched : WifiAdbEvent()
}
