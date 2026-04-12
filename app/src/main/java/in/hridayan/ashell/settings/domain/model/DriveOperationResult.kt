package `in`.hridayan.ashell.settings.domain.model

sealed class DriveOperationResult<out T> {
    data class Success<T>(val data: T) : DriveOperationResult<T>()
    object ConsentRequired : DriveOperationResult<Nothing>()
    data class Error(val exception: Throwable) : DriveOperationResult<Nothing>()
}