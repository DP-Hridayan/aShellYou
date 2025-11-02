package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

data class WifiAdbDevice(
    val ip: String,
    val port: Int,
    val name: String? = null,
    val lastConnected: Long = System.currentTimeMillis(),
    val isPaired: Boolean = false
)
