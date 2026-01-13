package `in`.hridayan.ashell.shell.file_browser.presentation.model

import androidx.annotation.StringRes

sealed class FileBrowserEvent {
    data class ShowToast(
        @StringRes val messageResId: Int,
        val formatArgs: List<Any> = emptyList()
    ) : FileBrowserEvent()

    data class FileDownloaded(val localPath: String) : FileBrowserEvent()
    data class FileUploaded(val remotePath: String) : FileBrowserEvent()
    data object FileDeleted : FileBrowserEvent()
    data object DirectoryCreated : FileBrowserEvent()
}
