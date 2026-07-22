package `in`.hridayan.ashell.commandexamples.presentation.component.dialog

import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey

sealed interface CommandExamplesDialogKey : DialogKey {
    object Add : CommandExamplesDialogKey
    data class Edit(val commandId: Int) : CommandExamplesDialogKey {
        override fun matches(other: DialogKey?): Boolean = other is Edit
    }

    object SortCommands : CommandExamplesDialogKey
    object LoadDefaultCommands : CommandExamplesDialogKey
}
