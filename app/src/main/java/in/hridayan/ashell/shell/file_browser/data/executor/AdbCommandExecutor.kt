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
