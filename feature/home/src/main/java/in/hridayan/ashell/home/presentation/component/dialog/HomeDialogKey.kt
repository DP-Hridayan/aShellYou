package `in`.hridayan.ashell.home.presentation.component.dialog

import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey

sealed interface HomeDialogKey : DialogKey {
    object RebootOptions : HomeDialogKey
}