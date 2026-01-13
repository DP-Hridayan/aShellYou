package `in`.hridayan.ashell.shell.file_browser.data.executor

import java.io.InputStream
import java.io.OutputStream

interface AdbCommandExecutor {

    fun isConnected(): Boolean

    suspend fun executeCommand(command: String): String?

    fun openCommandStream(command: String): CommandStream?

    fun openReadStream(command: String): FileTransferStream?

    fun openWriteStream(command: String): FileTransferStream?

    fun pullFileWithProgress(
        remotePath: String,
        totalSize: Long,
        onProgress: (Long, Long) -> Unit
    ): InputStream? = null

    fun pushFileWithProgress(
        remotePath: String,
        data: ByteArray,
        onProgress: (Long, Long) -> Unit
    ): Boolean = false

    fun supportsSyncTransfer(): Boolean = false
}

data class CommandStream(
    val inputStream: InputStream,
    val close: () -> Unit
)

data class FileTransferStream(
    val inputStream: InputStream? = null,
    val outputStream: OutputStream? = null,
    val close: () -> Unit
)
