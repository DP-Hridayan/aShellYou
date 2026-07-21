package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog

import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice

sealed interface PairDialogKey : DialogKey {
    object GrantNotificationAccess : PairDialogKey
    data class ForgetDeviceConfirmation(val device: WifiAdbDevice) : PairDialogKey {
        override fun matches(other: DialogKey?): Boolean = other is ForgetDeviceConfirmation
    }
    data class ReconnectFailed(val showDevOptionsButton: Boolean) : PairDialogKey {
        override fun matches(other: DialogKey?): Boolean = other is ReconnectFailed
    }
    object ConnectionSuccess : PairDialogKey
    object PairConnectFailed : PairDialogKey
}
