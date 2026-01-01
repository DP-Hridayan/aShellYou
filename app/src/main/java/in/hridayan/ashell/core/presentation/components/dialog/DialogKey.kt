package `in`.hridayan.ashell.core.presentation.components.dialog

import androidx.compose.runtime.Composable
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.presentation.viewmodel.DialogViewModel

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
        object WifiAdbPairedDevices : DialogKey()
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
    // Exact match (works for objects + data classes with same data)
    if (this == other) return true

    // Match by type (for data classes when payload differs)
    return this::class == other::class
}