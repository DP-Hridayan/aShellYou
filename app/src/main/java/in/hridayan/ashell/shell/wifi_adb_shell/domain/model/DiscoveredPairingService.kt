package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

/**
 * Represents a discovered pairing service broadcast by a device.
 * Used in "Pair Using Code" flow where we show cards for each discovered device.
 */
data class DiscoveredPairingService(
    val serviceName: String,
    val ip: String,
    val port: Int,
    val deviceName: String = serviceName
) {
    val key: String get() = "$ip:$port"
}
