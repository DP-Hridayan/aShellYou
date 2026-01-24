package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

const val UNKNOWN_DEVICE = "Unknown Device"

/**
 * Represents a device that can be connected via Wireless ADB.
 *
 * @property ip The IP address of the device.
 * @property port The port number used for the ADB connection.
 * @property deviceName The display name of the device.
 * @property lastConnected The timestamp of the last successful connection.
 * @property isPaired Whether the device has been successfully paired with this app.
 * @property serialNumber Device serial number retrieved after first connection via 'getprop ro.serialno'.
 * @property isOwnDevice Flag to identify if this is the device the app is running on (local ADB).
 * @property id Unique identifier for the device, using [serialNumber] if available, otherwise [ip].
 */
data class WifiAdbDevice(
    val ip: String,
    val port: Int,
    val deviceName: String = UNKNOWN_DEVICE,
    val lastConnected: Long = System.currentTimeMillis(),
    val isPaired: Boolean = false,
    val serialNumber: String? = null,
    val isOwnDevice: Boolean = false,
    val id: String = serialNumber ?: ip
)
