package `in`.hridayan.ashell.shell.file_browser.data.executor

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import `in`.hridayan.ashell.shell.file_browser.data.protocol.LibadbSyncTransport
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncSession
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncStat
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncTransport
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import io.github.muntashirakon.adb.AdbStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val SHELL_SERVICE_PREFIX = "shell:"
private const val SYNC_SERVICE = "sync:"
private const val COMMAND_BUFFER_SIZE = 4096
private const val COMMAND_END_MARKER = "__END__"

@Singleton
class WifiAdbCommandExecutor @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AdbCommandExecutor {

    companion object {
        const val TAG = "WifiAdbExecutor"
    }

    private fun getAdbManager(): AbsAdbConnectionManager =
        AdbConnectionManager.getInstance(context)

    override fun isConnected(): Boolean = try {
        getAdbManager().isConnected
    } catch (e: Exception) {
        Log.e(TAG, "Error checking ADB connection", e)
        false
    }

    override suspend fun executeCommand(command: String): String? = withContext(Dispatchers.IO) {
        var stream: AdbStream? = null
        try {
            val adbManager = getAdbManager()
            if (!adbManager.isConnected) return@withContext null

            stream = adbManager.openStream(SHELL_SERVICE_PREFIX + command)
            val inputStream = stream.openInputStream()

            val buffer = ByteArray(COMMAND_BUFFER_SIZE)
            val output = StringBuilder()
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.append(String(buffer, 0, bytesRead, Charsets.UTF_8))
                if (output.contains(COMMAND_END_MARKER)) break
            }

            runCatching { inputStream.close() }
            output.toString()
        } catch (e: Exception) {
            Log.e(TAG, "executeCommand failed: $command - ${e.message}")
            null
        } finally {
            runCatching { stream?.close() }
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

    private suspend fun <T> withSyncSession(block: suspend (SyncSession) -> T): T? = try {
        requireSyncSession(block)
    } catch (e: IOException) {
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
        val adbManager = getAdbManager()
        if (!adbManager.isConnected) throw IOException("No Wi-Fi ADB connection")
        return LibadbSyncTransport(adbManager.openStream(SYNC_SERVICE))
    }
}
