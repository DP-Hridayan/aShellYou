package `in`.hridayan.ashell.adbsideload.domain.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class SideloadState {
    data object Idle : SideloadState()
    data object Searching : SideloadState()
    data class DeviceFound(val deviceName: String) : SideloadState()
    data class Connected(val deviceName: String) : SideloadState()
    data object PermissionDenied : SideloadState()
    data object Connecting : SideloadState()
    data object Disconnected : SideloadState()
    data object UsbManagerUnavailable : SideloadState()
    data class Error(val message: String) : SideloadState()
}
