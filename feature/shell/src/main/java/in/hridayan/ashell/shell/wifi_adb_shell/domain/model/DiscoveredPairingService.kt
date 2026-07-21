package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

/**
 * Represents a discovered pairing service broadcast by a device.
 * Used in "Pair Using Code" flow where we show cards for each discovered device.
 *
 * @property serviceName The name of the discovered mDNS service.
 * @property ip The IP address of the device providing the service.
 * @property port The port number where the pairing service is listening.
 * @property deviceName The display name of the device, defaults to [serviceName].
 */
data class DiscoveredPairingService(
    val serviceName: String,
    val ip: String,
    val port: Int,
    val deviceName: String = serviceName
) {
    val key: String get() = "$ip:$port"
}
