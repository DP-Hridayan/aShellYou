package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

/**
 * Represents the persistent connection state of WiFi ADB.
 */
sealed class WifiAdbState {
    /**
     * No active connection or operation in progress.
     */
    data object Idle : WifiAdbState()

    /**
     * Attempting to connect to a device.
     *
     * @property deviceId Device ID if reconnecting to a saved device, null for new connections.
     * @property address The IP:port being connected to.
     */
    data class Connecting(
        val deviceId: String? = null,
        val address: String? = null
    ) : WifiAdbState()

    /**
     * Successfully connected to a device.
     *
     * @property deviceId Unique identifier for the connected device.
     * @property address The IP:port address of the connection.
     */
    data class Connected(
        val deviceId: String,
        val address: String
    ) : WifiAdbState()

    /**
     * Disconnected from a device.
     *
     * @property deviceId The device that was disconnected, null if no recent connection.
     */
    data class Disconnected(
        val deviceId: String? = null
    ) : WifiAdbState()

    /**
     * Attempting to reconnect to a previously saved device.
     *
     * @property deviceId The device being reconnected to.
     */
    data class Reconnecting(
        val deviceId: String
    ) : WifiAdbState()

    /**
     * Pairing operation is in progress.
     *
     * @property info Status message for UI display.
     */
    data class Pairing(
        val info: String = "Pairing..."
    ) : WifiAdbState()

    /**
     * mDNS discovery is in progress.
     *
     * @property info Status message for UI display.
     */
    data class Discovering(
        val info: String = "Discovering..."
    ) : WifiAdbState()

    /**
     * Whether the state represents an active connection.
     */
    val isConnected: Boolean
        get() = this is Connected

    /**
     * Whether the state represents an operation in progress.
     */
    val isLoading: Boolean
        get() = this is Connecting || this is Reconnecting || this is Pairing || this is Discovering

    /**
     * The device ID associated with the current state, if applicable.
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
