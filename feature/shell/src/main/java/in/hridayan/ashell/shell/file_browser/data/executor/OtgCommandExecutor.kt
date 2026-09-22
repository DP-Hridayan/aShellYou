package `in`.hridayan.ashell.shell.file_browser.data.executor

import android.util.Log
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
private const val COMMAND_END_MARKER = "__END__"

@Singleton
class OtgCommandExecutor @Inject constructor(
    private val otgRepository: OtgRepository
) : AdbCommandExecutor {

    companion object {
        const val TAG = "OtgCommandExecutor"
    }

    override fun isConnected(): Boolean = otgRepository.isConnected()

    override suspend fun executeCommand(command: String): String? = withContext(Dispatchers.IO) {
        if (!isConnected()) return@withContext null

        val adbConnection = otgRepository.getAdbConnection() ?: return@withContext null

        withTimeoutOrNull(COMMAND_TIMEOUT_MS) {
            val stream = adbConnection.open(SHELL_SERVICE_PREFIX + command)
            try {
                val output = StringBuilder()
                while (true) {
                    val data = stream.read()
                    if (data == null || data.isEmpty()) break
                    output.append(String(data, Charsets.UTF_8))
                    if (output.contains(COMMAND_END_MARKER)) break
                }
                output.toString()
            } finally {
                runCatching { stream.close() }
            }
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
     * Catches everything, not only [IOException]. A protocol surprise arrives as some other type, and
     * letting it escape turned a missing file size into a failed download.
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
