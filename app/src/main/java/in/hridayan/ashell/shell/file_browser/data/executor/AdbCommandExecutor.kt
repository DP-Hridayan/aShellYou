package `in`.hridayan.ashell.shell.file_browser.data.executor

import java.io.InputStream

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
}

/**
 * Wrapper for a command stream that includes cleanup.
 */
data class CommandStream(
    val inputStream: InputStream,
    val close: () -> Unit
)
