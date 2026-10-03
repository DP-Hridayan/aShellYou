package `in`.hridayan.ashell.logcat.presentation.model

import androidx.compose.runtime.Immutable
import `in`.hridayan.ashell.logcat.domain.model.LogFilter

/** What a row in the filter profile list can do. */
@Immutable
data class FilterProfileActions(
    val onToggle: (profileId: String) -> Unit,
    val onEdit: (profileId: String) -> Unit,
    val onDelete: (LogFilter) -> Unit,
)
