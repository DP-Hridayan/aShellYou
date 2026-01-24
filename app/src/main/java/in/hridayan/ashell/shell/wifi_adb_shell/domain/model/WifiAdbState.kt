package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

/**
 * Represents the persistent connection state of WiFi ADB.
 * 
 * These are **persistent conditions** that represent the current status of the connection.
 * Unlike [WifiAdbEvent] which are one-shot occurrences, states persist until changed.
 * 
 * ## State Flow:
 * ```
 * Idle ──► Connecting ──► Connected
 *   │           │              │
 *   │           ▼              ▼
 *   │      (ConnectFailed) Disconnected
 *   │           │              │
 *   ▼           │              ▼
 * Pairing ◄─────┴────── Reconnecting
 * ```
 * 
 * ## Usage:
 * Observe via `StateFlow` for persistent UI updates (connection indicator, loading states).
 */
sealed class WifiAdbState {
    /**
     * No active connection or operation in progress.
     * This is the initial state.
     */
    data object Idle : WifiAdbState()

    /**
     * Attempting to connect to a device.
     * @param deviceId Device ID if reconnecting to a saved device, null for new connections
     * @param address The IP:port being connected to (for UI display)
     */
    data class Connecting(
        val deviceId: String? = null,
        val address: String? = null
    ) : WifiAdbState()

    /**
     * Successfully connected to a device.
     * @param deviceId Unique identifier for the connected device
     * @param address The IP:port address of the connection
     */
    data class Connected(
        val deviceId: String,
        val address: String
    ) : WifiAdbState()

    /**
     * Disconnected from a device.
     * @param deviceId The device that was disconnected, null if no recent connection
     */
    data class Disconnected(
        val deviceId: String? = null
    ) : WifiAdbState()

    /**
     * Attempting to reconnect to a previously saved device.
     * @param deviceId The device being reconnected to
     */
    data class Reconnecting(
        val deviceId: String
    ) : WifiAdbState()

    /**
     * Pairing operation is in progress.
     * @param info Status message for UI display
     */
    data class Pairing(
        val info: String = "Pairing..."
    ) : WifiAdbState()

    /**
     * mDNS discovery is in progress.
     * @param info Status message for UI display
     */
    data class Discovering(
        val info: String = "Discovering..."
    ) : WifiAdbState()

    /**
     * Returns true if currently connected to a device.
     */
    val isConnected: Boolean
        get() = this is Connected

    /**
     * Returns true if an operation is in progress (connecting, pairing, etc.)
     */
    val isLoading: Boolean
        get() = this is Connecting || this is Reconnecting || this is Pairing || this is Discovering

    /**
     * Returns the device ID if this state is device-specific, null otherwise.
     */
    open val currentDeviceId: String?
        get() = when (this) {
            is Connected -> this.deviceId
            is Connecting -> this.deviceId
            is Disconnected -> this.deviceId
            is Reconnecting -> this.deviceId
            else -> null
        }
}
