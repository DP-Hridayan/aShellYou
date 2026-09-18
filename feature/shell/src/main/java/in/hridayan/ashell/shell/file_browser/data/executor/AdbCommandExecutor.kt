package `in`.hridayan.ashell.shell.file_browser.data.executor

import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncStat
import java.io.InputStream
import java.io.OutputStream

/**
 * Runs file browser work against one ADB transport.
 *
 * Metadata and mutation stay on the shell, where they belong. File contents move over the ADB `sync:`
 * protocol instead, which frames its messages, acknowledges uploads and carries a real failure
 * message, none of which piping bytes through `cat` could do.
 */
interface AdbCommandExecutor {

    fun isConnected(): Boolean

    suspend fun executeCommand(command: String): String?

    /** Metadata straight from the transfer protocol, avoiding a separate shell round trip. */
    suspend fun stat(remotePath: String): SyncStat?

    /**
     * @param onProgress receives the running total of bytes written.
     */
    suspend fun pull(remotePath: String, sink: OutputStream, onProgress: suspend (Long) -> Unit)

    /**
     * @param onProgress receives the running total of bytes sent.
     */
    suspend fun push(source: InputStream, remotePath: String, onProgress: suspend (Long) -> Unit)
}
