package `in`.hridayan.ashell.core.presentation.utils

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
}