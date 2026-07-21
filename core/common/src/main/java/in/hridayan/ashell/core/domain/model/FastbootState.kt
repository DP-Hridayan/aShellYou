package `in`.hridayan.ashell.core.domain.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class FastbootState {
    data object Idle : FastbootState()
    data object Searching : FastbootState()
    data class DeviceFound(val deviceName: String) : FastbootState()
    data class Connected(val deviceName: String, val deviceId: String) : FastbootState()
    data object PermissionDenied : FastbootState()
    data object Connecting : FastbootState()
    data object Disconnected : FastbootState()
    data object UsbManagerUnavailable : FastbootState()
    data class Error(val message: String) : FastbootState()
}

