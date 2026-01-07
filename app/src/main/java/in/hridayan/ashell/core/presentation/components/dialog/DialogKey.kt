package `in`.hridayan.ashell.core.presentation.components.dialog

import androidx.compose.runtime.Composable
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.presentation.viewmodel.DialogViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice

sealed class DialogKey {
    object None : DialogKey()

    object Shell {
        object None : DialogKey()
        object ClearOutput : DialogKey()
        object Bookmark : DialogKey()
        object DeleteBookmarks : DialogKey()
        object BookmarkSort : DialogKey()
        object FileSaved : DialogKey()
    }

    sealed class CommandExamples {
        object Add : DialogKey()
        data class Edit(val commandId: Int) : DialogKey()
        object SortCommands : DialogKey()
        object LoadDefaultCommands : DialogKey()
    }

    sealed class Home {
        object OtgDeviceWaiting : DialogKey()
        object ChooseWifiAdbPairMode : DialogKey()
        object RebootOptions : DialogKey()
    }

    sealed class Settings {
        object ConfigureSaveDir : DialogKey()
        object LatestVersion : DialogKey()
        object ResetSettings : DialogKey()
        object RestoreBackup : DialogKey()
    }

    sealed class Pair {
        object GrantNotificationAccess : DialogKey()
        data class ForgetDeviceConfirmation(val device: WifiAdbDevice) : DialogKey()
        data class ReconnectFailed(val showDevOptionsButton: Boolean) : DialogKey()
        object ConnectionSuccess : DialogKey()
        object PairConnectFailed : DialogKey()
    }

    sealed class WifiAdbScreen {
        object DeviceDisconnected : DialogKey()
    }
}

@Composable
fun DialogKey.createDialog(content: @Composable (DialogViewModel) -> Unit) {
    val dialogManager = LocalDialogManager.current
    val active = dialogManager.activeDialog

    if (this.matches(active)) {
        content(dialogManager)
    }
}

private fun DialogKey.matches(other: DialogKey): Boolean {
    // For data classes, match by type (class) to allow different parameters
    return when (this) {
        is DialogKey.Pair.ReconnectFailed if other is DialogKey.Pair.ReconnectFailed -> true
        is DialogKey.Pair.ForgetDeviceConfirmation if other is DialogKey.Pair.ForgetDeviceConfirmation -> true
        is DialogKey.CommandExamples.Edit if other is DialogKey.CommandExamples.Edit -> true
        else -> this == other
    }
}