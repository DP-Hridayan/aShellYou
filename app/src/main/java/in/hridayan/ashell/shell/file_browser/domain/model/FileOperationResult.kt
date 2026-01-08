package `in`.hridayan.ashell.shell.file_browser.domain.model

/**
 * Represents the result of a file operation.
 */
sealed class FileOperationResult {
    data class Success(val message: String = "") : FileOperationResult()
    data class Error(val message: String) : FileOperationResult()
    data class Progress(val current: Long, val total: Long) : FileOperationResult()
}