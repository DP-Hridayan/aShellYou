package `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing paired WiFi ADB devices.
 * 
 * @param id Unique identifier (serial number if available, otherwise IP)
 * @param ip IP address of the device
 * @param port ADB connect port
 * @param deviceName Human-readable device name
 * @param lastConnected Timestamp of last successful connection
 * @param isPaired Whether the device has been paired
 * @param serialNumber Device serial number (retrieved after first connection)
 * @param isOwnDevice Flag to identify if this is the device running the app
 */
@Entity(tableName = "wifi_adb_devices")
data class WifiAdbDeviceEntity(
    @PrimaryKey
    val id: String,
    val ip: String,
    val port: Int,
    val deviceName: String = "Unknown Device",
    val lastConnected: Long = System.currentTimeMillis(),
    val isPaired: Boolean = false,
    val serialNumber: String? = null,
    val isOwnDevice: Boolean = false
)
