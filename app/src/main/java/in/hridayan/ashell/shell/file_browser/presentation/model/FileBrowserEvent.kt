package `in`.hridayan.ashell.shell.file_browser.presentation.model

sealed class FileBrowserEvent {
    data class ShowToast(val message: String) : FileBrowserEvent()
    data class FileDownloaded(val localPath: String) : FileBrowserEvent()
    data class FileUploaded(val remotePath: String) : FileBrowserEvent()
    data object FileDeleted : FileBrowserEvent()
    data object DirectoryCreated : FileBrowserEvent()
}
