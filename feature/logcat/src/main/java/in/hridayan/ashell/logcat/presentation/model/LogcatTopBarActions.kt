package `in`.hridayan.ashell.logcat.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class LogcatTopBarActions(
    val onPlayPause: () -> Unit,
    val onModeClick: () -> Unit,
    val onOpenFilter: () -> Unit,
    val onClear: () -> Unit,
)
