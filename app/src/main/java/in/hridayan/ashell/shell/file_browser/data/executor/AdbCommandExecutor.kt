package `in`.hridayan.ashell.shell.file_browser.data.executor

import java.io.InputStream
import java.io.OutputStream

/**
 * Abstraction layer for ADB command execution.
 * Allows FileBrowserRepository to work with both WiFi ADB and OTG connections.
 * 
 * The shell commands (ls, cp, mv, rm, cat) are identical - only the transport differs.
 */
interface AdbCommandExecutor {
    
    /**
     * Check if there is an active connection.
     */
    fun isConnected(): Boolean
    
    /**
     * Execute a shell command and return the output as a string.
     * @param command Shell command without "shell:" prefix
     * @return Command output, or null if execution failed
     */
    suspend fun executeCommand(command: String): String?
    
    /**
     * Open a stream for a shell command (for file transfers).
     * @param command Full command including "shell:" prefix
     * @return Pair of (inputStream, closeFunction)
     */
    fun openCommandStream(command: String): CommandStream?
    
    /**
     * Open a read stream for downloading files (cat remote file).
     * @param command Shell command like "shell:cat '/path/to/file'"
     * @return FileTransferStream with read capability
     */
    fun openReadStream(command: String): FileTransferStream?
    
    /**
     * Open a write stream for uploading files (cat > remote file).
     * @param command Shell command like "shell:cat > '/path/to/file'"
     * @return FileTransferStream with write capability
     */
    fun openWriteStream(command: String): FileTransferStream?
    
    /**
     * Pull (download) a file using sync protocol with progress reporting.
     * This is more reliable than shell-based cat for binary files.
     * 
     * @param remotePath Path to file on remote device
     * @param totalSize Known file size for progress reporting (0 if unknown)
     * @param onProgress Callback for progress updates (bytesReceived, totalSize)
     * @return InputStream with file data, or null on error
     */
    fun pullFileWithProgress(
        remotePath: String,
        totalSize: Long,
        onProgress: (Long, Long) -> Unit
    ): InputStream? = null  // Default: not supported, repository falls back to openReadStream
    
    /**
     * Push (upload) a file using sync protocol with progress reporting.
     * This is more reliable than shell-based cat for binary files.
     * 
     * @param remotePath Path where file should be saved on remote device
     * @param data File data to upload
     * @param onProgress Callback for progress updates (bytesWritten, totalSize)
     * @return true on success, false on failure
     */
    fun pushFileWithProgress(
        remotePath: String,
        data: ByteArray,
        onProgress: (Long, Long) -> Unit
    ): Boolean = false  // Default: not supported, repository falls back to openWriteStream
    
    /**
     * Whether this executor supports sync-based file transfers with progress.
     * If true, repository should use pullFileWithProgress/pushFileWithProgress.
     */
    fun supportsSyncTransfer(): Boolean = false
}

/**
 * Wrapper for a command stream that includes cleanup.
 */
data class CommandStream(
    val inputStream: InputStream,
    val close: () -> Unit
)

/**
 * Stream for file transfers supporting both read and write.
 */
data class FileTransferStream(
    val inputStream: InputStream? = null,
    val outputStream: OutputStream? = null,
    val close: () -> Unit
)
