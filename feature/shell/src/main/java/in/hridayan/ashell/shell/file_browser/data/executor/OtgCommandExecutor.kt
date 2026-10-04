package `in`.hridayan.ashell.shell.file_browser.data.executor

import android.util.Log
import com.cgutman.adblib.AdbStream
import `in`.hridayan.ashell.core.common.domain.repository.OtgRepository
import `in`.hridayan.ashell.shell.file_browser.data.protocol.AdblibSyncTransport
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncSession
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncStat
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncTransport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val SHELL_SERVICE_PREFIX = "shell:"
private const val SYNC_SERVICE = "sync:"
private const val COMMAND_TIMEOUT_MS = 15_000L

@Singleton
class OtgCommandExecutor @Inject constructor(
    private val otgRepository: OtgRepository
) : AdbCommandExecutor {

    companion object {
        const val TAG = "OtgCommandExecutor"
    }

    override fun isConnected(): Boolean = otgRepository.isConnected()

    override suspend fun executeCommand(command: String): String? = withContext(Dispatchers.IO) {
        withTimeoutOrNull(COMMAND_TIMEOUT_MS) { readCommandOutput(command) }
    }

    override suspend fun executeLongRunningCommand(command: String): String? =
        withContext(Dispatchers.IO) { readCommandOutput(command) }

    private fun readCommandOutput(command: String): String? {
        if (!isConnected()) return null

        val adbConnection = otgRepository.getAdbConnection() ?: return null
        val marker = CommandEndMarker.next()
        val stream =
            adbConnection.open(SHELL_SERVICE_PREFIX + CommandEndMarker.appendTo(command, marker))
        try {
            val output = StringBuilder()
            while (true) {
                val data = stream.readUntilClosed()
                if (data == null || data.isEmpty()) break
                output.append(String(data, Charsets.UTF_8))
                if (output.contains(marker)) break
            }
            return output.toString().substringBefore(marker)
        } finally {
            runCatching { stream.close() }
        }
    }

    override suspend fun stat(remotePath: String): SyncStat? =
        withSyncSession { it.stat(remotePath) }

    override suspend fun pull(
        remotePath: String,
        sink: OutputStream,
        onProgress: suspend (Long) -> Unit
    ) {
        requireSyncSession { it.pull(remotePath, sink, onProgress) }
    }

    override suspend fun push(
        source: InputStream,
        remotePath: String,
        onProgress: suspend (Long) -> Unit
    ) {
        requireSyncSession { it.push(source, remotePath, onProgress = onProgress) }
    }

    /**
     * @return the next payload, or null once the peer closed the stream, which is how a finished
     * command ends on this transport. A failure while the stream is still open is a real one and is
     * left to the caller.
     */
    private fun AdbStream.readUntilClosed(): ByteArray? = try {
        read()
    } catch (e: IOException) {
        if (isClosed) null else throw e
    }

    /**
     * Catches everything, not only [IOException], because a protocol surprise arrives as some other
     * type and must not surface as a failed transfer.
     */
    private suspend fun <T> withSyncSession(block: suspend (SyncSession) -> T): T? = try {
        requireSyncSession(block)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Sync session failed", e)
        null
    }

    private suspend fun <T> requireSyncSession(block: suspend (SyncSession) -> T): T {
        val transport = openSyncTransport()
        val session = SyncSession(transport)
        return try {
            block(session).also { runCatching { session.quit() } }
        } finally {
            transport.close()
        }
    }

    private fun openSyncTransport(): SyncTransport {
        val connection = otgRepository.getAdbConnection()
            ?: throw IOException("No OTG ADB connection")
        return AdblibSyncTransport(connection.open(SYNC_SERVICE))
    }
}
