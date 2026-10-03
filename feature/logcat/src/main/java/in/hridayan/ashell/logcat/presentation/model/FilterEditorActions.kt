package `in`.hridayan.ashell.logcat.presentation.model

import androidx.compose.runtime.Immutable
import `in`.hridayan.ashell.logcat.domain.model.FilterMode
import `in`.hridayan.ashell.logcat.domain.model.LogLevel

/** Everything the filter profile editor's form can report back. */
@Immutable
data class FilterEditorActions(
    val onNameChange: (String) -> Unit,
    val onModeChange: (FilterMode) -> Unit,
    val onLevelToggle: (LogLevel) -> Unit,
    val onTagsChange: (String) -> Unit,
    val onPidsChange: (String) -> Unit,
    val onTidsChange: (String) -> Unit,
    val onPackagesChange: (String) -> Unit,
    val onPickApps: () -> Unit,
    val onCancel: () -> Unit,
    val onSave: () -> Unit,
)
