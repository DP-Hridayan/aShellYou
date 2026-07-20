package `in`.hridayan.ashell.home.presentation.components.dialog

import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey

sealed interface HomeDialogKey : DialogKey {
    object OtgDeviceWaiting : HomeDialogKey
    object ChooseWifiAdbPairMode : HomeDialogKey
    object RebootOptions : HomeDialogKey
    object FastbootDeviceWaiting : HomeDialogKey
}
