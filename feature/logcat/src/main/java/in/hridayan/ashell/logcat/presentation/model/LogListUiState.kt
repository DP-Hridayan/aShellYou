package `in`.hridayan.ashell.logcat.presentation.model

import androidx.compose.runtime.Immutable
import `in`.hridayan.ashell.logcat.domain.model.LogEntry

@Immutable
data class LogListUiState(
    val logs: List<LogEntry> = emptyList(),
    val isAutoScrolling: Boolean = true,
)
