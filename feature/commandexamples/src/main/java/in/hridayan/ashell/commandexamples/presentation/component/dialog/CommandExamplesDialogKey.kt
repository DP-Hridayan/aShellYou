package `in`.hridayan.ashell.commandexamples.presentation.component.dialog

import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey

sealed interface CommandExamplesDialogKey : DialogKey {
    object SortCommands : CommandExamplesDialogKey
    object LoadDefaultCommands : CommandExamplesDialogKey
}
