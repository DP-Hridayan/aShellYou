package `in`.hridayan.ashell.shell.otg_adb_shell.domain.model

sealed class OtgState {
    object Idle : OtgState()
    object Searching : OtgState()
    data class DeviceFound(val deviceName: String) : OtgState()
    data class Connected(val deviceName: String) : OtgState()
    object PermissionDenied : OtgState()
    object Connecting : OtgState()
    object Disconnected : OtgState()
    data class Error(val message: String) : OtgState()
}
