package `in`.hridayan.ashell.shell.file_browser.domain.model

import androidx.annotation.Keep

/**
 * A paste target that already exists on the device, waiting for the user to decide what happens.
 */
data class FileConflict(
    val sourcePath: String,
    val destPath: String,
    val operationType: OperationType,
    val destIsDirectory: Boolean = false,
    val sourceIsDirectory: Boolean = false,
    val fileName: String = sourcePath.substringAfterLast("/"),

    /** Conflicts already known to follow this one in the same folder, for "apply to all". */
    val remainingConflicts: Int = 0,

    /**
     * False when the existing item is a folder that contains the source, where replacing would
     * delete the source together with everything next to it.
     */
    val canReplace: Boolean = true
) {
    val canMerge: Boolean
        get() = destIsDirectory && sourceIsDirectory

    fun allows(resolution: ConflictResolution): Boolean = when (resolution) {
        ConflictResolution.REPLACE -> canReplace
        ConflictResolution.MERGE -> canMerge
        ConflictResolution.SKIP, ConflictResolution.KEEP_BOTH -> true
    }
}

/**
 * Resolution options for file conflicts
 */
@Keep
enum class ConflictResolution {
    /** Skip this file/folder, continue with next */
    SKIP,

    /** Swap the existing item for the source, restoring it if the swap fails part way. */
    REPLACE,

    /** For directories: recursively merge contents */
    MERGE,

    /** Keep both by renaming source with counter (e.g., file (1).txt) */
    KEEP_BOTH
}

/**
 * The user's answer to one [FileConflict].
 *
 * With [applyToAll], later conflicts reuse [resolution] only where [FileConflict.allows] it, so a
 * folder-only choice like merge never gets forced onto a file.
 */
data class ConflictDecision(
    val resolution: ConflictResolution,
    val applyToAll: Boolean = false
)
