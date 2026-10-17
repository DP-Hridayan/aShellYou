package `in`.hridayan.ashell.logcat.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class LogSelectionActions(
    val onClose: () -> Unit,
    val onDetails: () -> Unit,
    val onToggleSelectAll: () -> Unit,
    val onCopy: () -> Unit,
    val onShare: () -> Unit,
    val onSave: () -> Unit,
)
