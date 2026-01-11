package `in`.hridayan.ashell.shell.file_browser.presentation.model

import `in`.hridayan.ashell.shell.file_browser.domain.model.ConflictResolution
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileConflict
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperation
import `in`.hridayan.ashell.shell.file_browser.domain.model.PendingPasteOperation
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile

data class FileBrowserState(
    val currentPath: String = "/storage/emulated/0",
    val files: List<RemoteFile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFile: RemoteFile? = null,
    val operations: List<FileOperation> = emptyList(),
    val isVirtualEmptyFolder: Boolean = false,
    val lastSuccessfulPath: String = "/storage/emulated/0",
    // Conflict handling state
    val pendingConflict: FileConflict? = null,
    val pendingPasteOperation: PendingPasteOperation? = null,
    val applyToAllResolution: ConflictResolution? = null,
    val isPasting: Boolean = false, // True when paste is actively executing
    // Selection mode
    val selectedFiles: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false
)