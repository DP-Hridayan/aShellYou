package `in`.hridayan.ashell.shell.file_browser.domain.model

import java.util.UUID

/**
 * Represents an ongoing file operation (upload, download, copy, move)
 */
data class FileOperation(
    val id: String = UUID.randomUUID().toString(),
    val type: OperationType,
    val fileName: String,
    val bytesTransferred: Long = 0L,
    val totalBytes: Long = 0L,
    val message: String = ""
)
