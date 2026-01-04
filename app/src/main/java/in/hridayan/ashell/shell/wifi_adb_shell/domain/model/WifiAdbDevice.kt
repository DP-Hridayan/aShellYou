package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

data class WifiAdbDevice(
    val ip: String,
    val port: Int,
    val deviceName: String = "Unknown Device",
    val lastConnected: Long = System.currentTimeMillis(),
    val isPaired: Boolean = false,
    // Device serial number retrieved after first connection via 'getprop ro.serialno'
    val serialNumber: String? = null,
    // Use serial as unique identifier if available, otherwise fall back to IP
    val id: String = serialNumber ?: ip
)
