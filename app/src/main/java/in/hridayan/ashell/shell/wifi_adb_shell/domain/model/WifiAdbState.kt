package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

sealed class WifiAdbState(val message: String) {
    object None : WifiAdbState("none")
    data class PairingStarted(val info: String = "Pairing started") : WifiAdbState(info)
    data class PairingSuccess(val info: String) : WifiAdbState(info)
    data class PairingFailed(val error: String) : WifiAdbState(error)
    data class ConnectStarted(val info: String = "Connecting...") : WifiAdbState(info)
    data class ConnectSuccess(val info: String) : WifiAdbState(info)
    data class ConnectFailed(val error: String) : WifiAdbState(error)
    data class DiscoveryStarted(val info: String) : WifiAdbState(message = info)
    data class DiscoveryFound(val info: String) : WifiAdbState(info)
    data class DiscoverySessionMatched(val info: String = "Session matched") : WifiAdbState(info)
    data class DiscoveryFailed(val info: String) : WifiAdbState(message = info)
}
