package `in`.hridayan.ashell.logcat.presentation.model

import androidx.compose.runtime.Immutable

/**
 * Rows picked in the log list for copying or exporting.
 *
 * [anchorId] is the row a range selection extends from: the last row toggled or the end of the
 * last range.
 */
@Immutable
data class LogSelectionState(
    val isActive: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val anchorId: Long? = null,
)
