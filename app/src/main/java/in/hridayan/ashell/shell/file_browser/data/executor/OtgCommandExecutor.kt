package `in`.hridayan.ashell.shell.file_browser.data.executor

import android.util.Log
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.repository.OtgRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OTG ADB command executor.
 * Uses OtgRepository's AdbConnection for transport.
 * 
 * EXISTING PARTS USED:
 * - OtgRepository.getAdbConnection() for access to OTG's AdbConnection
 * - Same command format as WiFi ("shell:command")
 */
@Singleton
class OtgCommandExecutor @Inject constructor(
    private val otgRepository: OtgRepository
) : AdbCommandExecutor {
    
    private val TAG = "OtgCommandExecutor"
    
    override fun isConnected(): Boolean {
        return otgRepository.isConnected()
    }
    
    /**
     * Execute command using OTG AdbConnection.
     * NO RETRY - repository handles retries.
     * Uses timeout since OTG stream.read() can block indefinitely.
     */
    override suspend fun executeCommand(command: String): String? = withContext(Dispatchers.IO) {
        try {
            if (!isConnected()) {
                Log.w(TAG, "executeCommand: OTG not connected")
                return@withContext null
            }
            
            val adbConnection = otgRepository.getAdbConnection()
            if (adbConnection == null) {
                Log.w(TAG, "executeCommand: getAdbConnection returned null")
                return@withContext null
            }
            
            // Use timeout since OTG stream.read() can block indefinitely
            withTimeoutOrNull(10000L) {
                val stream = adbConnection.open("shell:$command")
                
                try {
                    val output = StringBuilder()
                    // Read with a reasonable buffer
                    while (true) {
                        val data = stream.read()
                        if (data == null || data.isEmpty()) break
                        val text = String(data, Charsets.UTF_8)
                        output.append(text)
                        // Check for end marker if present
                        if (output.contains("__END__")) break
                    }
                    output.toString()
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
     * For OTG, we read all data into memory then wrap as InputStream.
     * This is simpler but may use more memory for large files.
     */
    override fun openCommandStream(command: String): CommandStream? {
        return try {
            if (!isConnected()) {
                return null
            }
            
            val adbConnection = otgRepository.getAdbConnection() ?: return null
            val stream = adbConnection.open(command)
            
            // Read all data - OTG AdbStream doesn't expose a direct InputStream
            val output = mutableListOf<Byte>()
            while (true) {
                val data = stream.read() ?: break
                output.addAll(data.toList())
            }
            
            val inputStream = ByteArrayInputStream(output.toByteArray())
            
            CommandStream(
                inputStream = inputStream,
                close = {
                    try { stream.close() } catch (_: Exception) {}
                    try { inputStream.close() } catch (_: Exception) {}
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error opening command stream: $command", e)
            null
        }
    }
}
