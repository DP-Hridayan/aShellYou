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
     * Retry was removed because FileBrowserRepositoryImpl.listFiles already has retry logic.
     * Having retry in both places caused 3x3=9 attempts with long cumulative delays.
     */
    override suspend fun executeCommand(command: String): String? = withContext(Dispatchers.IO) {
        try {
            val adbManager = getAdbManager()
            
            if (!adbManager.isConnected) {
                Log.w(TAG, "executeCommand: ADB not connected")
                return@withContext null
            }
            
            // Use timeout to prevent infinite hanging
            withTimeoutOrNull(10000L) {
                val stream = adbManager.openStream("shell:$command")
                try {
                    val inputStream = stream.openInputStream()
                    inputStream.bufferedReader().use { it.readText() }
                } finally {
                    try { stream.close() } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "executeCommand failed: $command - ${e.message}")
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
}
