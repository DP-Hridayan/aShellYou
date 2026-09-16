package `in`.hridayan.ashell.adbsideload.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class SideloadScreenActions(
    val onPickFile: () -> Unit,
    val onClearFile: () -> Unit,
    val onSideload: () -> Unit,
    val onCancelSideload: () -> Unit,
    val onDone: () -> Unit,
)
