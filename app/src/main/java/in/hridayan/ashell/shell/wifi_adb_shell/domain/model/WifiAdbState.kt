package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

/**
 * Represents the connection state of WifiAdb.
 * States that are device-specific include a deviceId parameter.
 */
sealed class WifiAdbState(val message: String, open val deviceId: String? = null) {
    object None : WifiAdbState("none")
    
    // Device-specific connection states
    data class Reconnecting(val device: String) : WifiAdbState(device, device)
    data class Disconnected(val device: String? = null, val info: String = "Disconnected") : WifiAdbState(info, device)
    data class ConnectStarted(val info: String = "Connecting...", val device: String? = null) : WifiAdbState(info, device)
    data class ConnectSuccess(val info: String, val device: String? = null) : WifiAdbState(info, device)
    data class ConnectFailed(val error: String, val device: String? = null) : WifiAdbState(error, device)
    data class WirelessDebuggingOff(val device: String? = null, val info: String = "Enable wireless debugging") : WifiAdbState(info, device)
    
    // Session-based states (not device-specific)
    data class PairingStarted(val info: String = "Pairing started") : WifiAdbState(info)
    data class PairingSuccess(val info: String) : WifiAdbState(info)
    data class PairingFailed(val error: String) : WifiAdbState(error)
    data class DiscoveryStarted(val info: String) : WifiAdbState(message = info)
    data class DiscoveryFound(val info: String) : WifiAdbState(info)
    data class DiscoverySessionMatched(val info: String = "Session matched") : WifiAdbState(info)
    data class DiscoveryFailed(val info: String) : WifiAdbState(message = info)
    
    // Pairing flow connection failure (distinct from reconnect failure)
    data class PairConnectFailed(val error: String) : WifiAdbState(error)
}
