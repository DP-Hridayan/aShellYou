package `in`.hridayan.ashell.shell.file_browser.domain.model

/**
 * Represents a file/folder conflict during copy/move operations
 */
data class FileConflict(
    val sourcePath: String,
    val destPath: String,
    val operationType: OperationType,
    val isDirectory: Boolean = false,  // Destination is directory
    val sourceIsDirectory: Boolean = false,  // Source is directory
    val fileName: String = sourcePath.substringAfterLast("/"),
    val remainingCount: Int = 0  // How many more conflicts after this one
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

/**
 * Represents a pending paste operation item
 */
data class PendingPasteItem(
    val sourcePath: String,
    val destPath: String,
    val isDirectory: Boolean
)

/**
 * Tracks the state of a batch paste operation
 */
data class PendingPasteOperation(
    val operationType: OperationType,
    val destDir: String,
    val items: List<PendingPasteItem>,
    val currentIndex: Int = 0,
    val processedCount: Int = 0,
    val skippedCount: Int = 0,
    val failedCount: Int = 0
) {
    val currentItem: PendingPasteItem?
        get() = items.getOrNull(currentIndex)
    
    val isComplete: Boolean
        get() = currentIndex >= items.size
    
    val remainingCount: Int
        get() = (items.size - currentIndex - 1).coerceAtLeast(0)
}
