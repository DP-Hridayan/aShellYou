package `in`.hridayan.ashell.logcat.presentation.model

import androidx.compose.runtime.Immutable
import `in`.hridayan.ashell.logcat.domain.model.LogEntry

@Immutable
data class LogListActions(
    val onPauseAutoScroll: () -> Unit,
    val onResumeAutoScroll: () -> Unit,
    val onToggleExpanded: (Long) -> Unit,
    val onLongClick: (LogEntry) -> Unit,
)
