package `in`.hridayan.ashell.logcat.presentation.event

sealed class LogcatUiEvent {
    data object PermissionStillMissing : LogcatUiEvent()
}
