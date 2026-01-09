package `in`.hridayan.ashell.shell.file_browser.domain.model

/**
 * Represents a file/folder conflict during copy/move operations
 */
data class FileConflict(
    val sourcePath: String,
    val destPath: String,
    val operationType: OperationType,
    val isDirectory: Boolean = false,
    val fileName: String = sourcePath.substringAfterLast("/")
)

/**
 * Resolution options for file conflicts
 */
enum class ConflictResolution {
    /** Skip this file/folder, continue with next */
    SKIP,
    
    /** Delete existing item at destination, then copy/move source */
    REPLACE,
    
    /** For directories: recursively merge contents */
    MERGE,
    
    /** Keep both by renaming source with counter (e.g., file (1).txt) */
    KEEP_BOTH
}
