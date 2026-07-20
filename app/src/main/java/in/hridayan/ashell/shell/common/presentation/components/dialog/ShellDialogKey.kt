package `in`.hridayan.ashell.shell.common.presentation.components.dialog

import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey

sealed interface ShellDialogKey : DialogKey {
    object ClearOutput : ShellDialogKey
    object Bookmark : ShellDialogKey
    object DeleteBookmarks : ShellDialogKey
    object BookmarkSort : ShellDialogKey
    object FileSaved : ShellDialogKey
}
