package `in`.hridayan.ashell.core.presentation.components.dialog

import androidx.compose.runtime.Composable
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.presentation.viewmodel.DialogViewModel

interface DialogKey {
    fun matches(other: DialogKey?): Boolean = this == other
}

@Composable
fun DialogKey.createDialog(content: @Composable (DialogViewModel) -> Unit) {
    val dialogManager = LocalDialogManager.current
    val active = dialogManager.activeDialog

    if (this.matches(active)) {
        content(dialogManager)
    }
}