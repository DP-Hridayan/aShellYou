package `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.model

/**
 * Represents the result of a file operation.
 */
sealed class FileOperationResult {
    data class Success(val message: String = "") : FileOperationResult()
    data class Error(val message: String) : FileOperationResult()
    data class Progress(val current: Long, val total: Long) : FileOperationResult()
}

/**
 * Types of file operations.
 */
enum class FileOperation {
    PULL,
    PUSH,
    DELETE,
    CREATE_DIRECTORY,
    RENAME
}
