package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.model

import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.TcpIpEnableResult

/** Progress of opening ADB's TCP port after the own device connects on the pairing screen. */
sealed interface TcpIpEnableUiState {

    data object Idle : TcpIpEnableUiState

    data object Running : TcpIpEnableUiState

    data class Finished(val result: TcpIpEnableResult) : TcpIpEnableUiState
}
