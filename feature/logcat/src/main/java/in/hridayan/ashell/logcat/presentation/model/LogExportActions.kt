package `in`.hridayan.ashell.logcat.presentation.model

import androidx.compose.runtime.Immutable

/** Overflow menu actions on the logs of the active tab; all disabled when [isEnabled] is false. */
@Immutable
data class LogExportActions(
    val isEnabled: Boolean,
    val onSelect: () -> Unit,
    val onSave: () -> Unit,
    val onShare: () -> Unit,
)
