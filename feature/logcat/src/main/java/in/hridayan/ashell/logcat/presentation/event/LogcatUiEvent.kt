package `in`.hridayan.ashell.logcat.presentation.event

sealed class LogcatUiEvent {
    data object PermissionStillMissing : LogcatUiEvent()

    data class CopyLogs(val text: String, val count: Int) : LogcatUiEvent()

    data object CopyTooLarge : LogcatUiEvent()

    data class PickSaveLocation(val fileName: String) : LogcatUiEvent()

    data object LogsSaved : LogcatUiEvent()

    data class ShareLogs(val uriString: String) : LogcatUiEvent()

    data object ExportFailed : LogcatUiEvent()
}
