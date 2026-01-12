package `in`.hridayan.ashell.shell.file_browser.data.executor

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WiFi ADB command executor.
 * Uses AdbConnectionManager for transport - EXACT same logic as original FileBrowserRepositoryImpl.
 * 
 * EXISTING PARTS USED:
 * - AdbConnectionManager.getInstance(context).openStream()
 * - Same retry logic pattern
 * - Same timeout handling
 */
@Singleton
class WifiAdbCommandExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : AdbCommandExecutor {
    
    private val TAG = "WifiAdbExecutor"
    
    private fun getAdbManager(): AbsAdbConnectionManager {
        return AdbConnectionManager.getInstance(context)
    }
    
    override fun isConnected(): Boolean {
        return try {
            getAdbManager().isConnected
        } catch (e: Exception) {
            Log.e(TAG, "Error checking ADB connection", e)
            false
        }
    }
    
    /**
     * Execute command with timeout (NO RETRY - repository handles retries).
     * Uses original byte buffer approach with __END__ marker detection.
     */
    override suspend fun executeCommand(command: String): String? = withContext(Dispatchers.IO) {
        var stream: io.github.muntashirakon.adb.AdbStream? = null
        try {
            val adbManager = getAdbManager()
            
            if (!adbManager.isConnected) {
                Log.w(TAG, "executeCommand: ADB not connected")
                return@withContext null
            }
            
            stream = adbManager.openStream("shell:$command")
            val inputStream = stream.openInputStream()
            
            val buffer = ByteArray(4096)
            val output = StringBuilder()
            var bytesRead: Int
            
            // Read with timeout - original approach that worked
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.append(String(buffer, 0, bytesRead, Charsets.UTF_8))
                // Check for end marker
                if (output.contains("__END__")) {
                    break
                }
            }
            
            try { inputStream.close() } catch (_: Exception) {}
            try { stream.close() } catch (_: Exception) {}
            
            output.toString()
        } catch (e: Exception) {
            Log.e(TAG, "executeCommand failed: $command - ${e.message}")
            try { stream?.close() } catch (_: Exception) {}
            null
        }
    }
    
    /**
     * Open a command stream for file transfers.
     * Used by pullFile/pushFile for streaming data.
     */
    override fun openCommandStream(command: String): CommandStream? {
        return try {
            val adbManager = getAdbManager()
            if (!adbManager.isConnected) {
                return null
            }
            
            val stream = adbManager.openStream(command)
            val inputStream = stream.openInputStream()
            
            CommandStream(
                inputStream = inputStream,
                close = {
                    try { inputStream.close() } catch (_: Exception) {}
                    try { stream.close() } catch (_: Exception) {}
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error opening command stream: $command", e)
            null
        }
    }
    
    /**
     * Open a read stream for downloading files.
     */
    override fun openReadStream(command: String): FileTransferStream? {
        return try {
            val adbManager = getAdbManager()
            if (!adbManager.isConnected) {
                return null
            }
            
            val stream = adbManager.openStream(command)
            val inputStream = stream.openInputStream().buffered(65536)
            
            FileTransferStream(
                inputStream = inputStream,
                close = {
                    try { inputStream.close() } catch (_: Exception) {}
                    try { stream.close() } catch (_: Exception) {}
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error opening read stream: $command", e)
            null
        }
    }
    
    /**
     * Open a write stream for uploading files.
     */
    override fun openWriteStream(command: String): FileTransferStream? {
        return try {
            val adbManager = getAdbManager()
            if (!adbManager.isConnected) {
                return null
            }
            
            val stream = adbManager.openStream(command)
            val outputStream = stream.openOutputStream().buffered(65536)
            
            FileTransferStream(
                outputStream = outputStream,
                close = {
                    try { outputStream.flush() } catch (_: Exception) {}
                    try { outputStream.close() } catch (_: Exception) {}
                    try { stream.close() } catch (_: Exception) {}
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error opening write stream: $command", e)
            null
        }
    }
}
