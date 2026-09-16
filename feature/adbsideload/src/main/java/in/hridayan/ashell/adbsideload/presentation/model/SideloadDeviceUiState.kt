package `in`.hridayan.ashell.adbsideload.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class SideloadDeviceUiState(
    val isDetected: Boolean = false,
    val isConnecting: Boolean = false,
    val isReady: Boolean = false,
    val awaitingPermission: Boolean = false,
    val wrongMode: Boolean = false,
    val deviceName: String? = null,
    val errorText: String? = null,
)
