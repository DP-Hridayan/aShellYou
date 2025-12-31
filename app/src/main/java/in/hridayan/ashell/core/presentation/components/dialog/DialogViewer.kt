package `in`.hridayan.ashell.core.presentation.components.dialog

import androidx.compose.runtime.Composable
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.presentation.utils.DialogKey
import `in`.hridayan.ashell.core.presentation.viewmodel.DialogViewModel

@Composable
fun WithDialog(key: DialogKey, content: @Composable (DialogViewModel) -> Unit) {
    val dialogManager = LocalDialogManager.current
    val active = dialogManager.activeDialog

    if (active.matches(key)) {
        content(dialogManager)
    }
}

private fun DialogKey.matches(other: DialogKey): Boolean {
    // Exact match (works for objects + data classes with same data)
    if (this == other) return true

    // Match by type (for data classes when payload differs)
    return this::class == other::class
}
