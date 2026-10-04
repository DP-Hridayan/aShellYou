package `in`.hridayan.ashell.logcat.presentation.model

import androidx.compose.runtime.Immutable

/** What the filter profile list can do; see `LogFilterSelection` for tap and long press. */
@Immutable
data class FilterProfileActions(
    val onTap: (profileId: String) -> Unit,
    val onLongPress: (profileId: String) -> Unit,
    val onEdit: (profileId: String) -> Unit,
    val onClearChosen: () -> Unit,
    val onDeleteChosen: () -> Unit,
)
