package `in`.hridayan.ashell.logcat.presentation.model

import androidx.compose.runtime.Immutable

/** What the Other Device tab shows: its log list, and whether a device is connected and streaming. */
@Immutable
data class OtherDeviceUiState(
    val list: LogListUiState = LogListUiState(),
    val isConnected: Boolean = false,
    val isRunning: Boolean = false,
)
