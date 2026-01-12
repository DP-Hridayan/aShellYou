package `in`.hridayan.ashell.shell.file_browser.data.executor

import android.util.Log
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.repository.OtgRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
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
    
    /**
     * Open a read stream for downloading files via OTG.
     * Wraps OTG AdbStream.read() as an InputStream.
     */
    override fun openReadStream(command: String): FileTransferStream? {
        Log.d(TAG, "openReadStream called: $command")
        return try {
            if (!isConnected()) {
                Log.w(TAG, "openReadStream: OTG not connected")
                return null
            }
            
            val adbConnection = otgRepository.getAdbConnection()
            if (adbConnection == null) {
                Log.w(TAG, "openReadStream: getAdbConnection returned null")
                return null
            }
            
            Log.d(TAG, "openReadStream: Opening stream...")
            val stream = adbConnection.open(command)
            Log.d(TAG, "openReadStream: Stream opened successfully")
            
            // Create a wrapper InputStream that reads from OTG AdbStream
            val inputStream = OtgInputStream(stream)
            
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
     * Open a write stream for uploading files via OTG.
     * Wraps OTG AdbStream.write() as an OutputStream.
     */
    override fun openWriteStream(command: String): FileTransferStream? {
        Log.d(TAG, "openWriteStream called: $command")
        return try {
            if (!isConnected()) {
                Log.w(TAG, "openWriteStream: OTG not connected")
                return null
            }
            
            val adbConnection = otgRepository.getAdbConnection()
            if (adbConnection == null) {
                Log.w(TAG, "openWriteStream: getAdbConnection returned null")
                return null
            }
            
            Log.d(TAG, "openWriteStream: Opening stream...")
            val stream = adbConnection.open(command)
            Log.d(TAG, "openWriteStream: Stream opened successfully")
            
            // Create a wrapper OutputStream that writes to OTG AdbStream
            val outputStream = OtgOutputStream(stream)
            
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

/**
 * Wrapper to convert OTG AdbStream.read() to standard InputStream.
 */
private class OtgInputStream(
    private val adbStream: com.cgutman.adblib.AdbStream
) : InputStream() {
    private val TAG = "OtgInputStream"
    private var buffer: ByteArray? = null
    private var bufferPos = 0
    private var closed = false
    private var eofReached = false
    
    override fun read(): Int {
        if (closed || eofReached) return -1
        
        // Refill buffer if needed
        if (buffer == null || bufferPos >= buffer!!.size) {
            buffer = try {
                val data = adbStream.read()
                if (data == null || data.isEmpty()) {
                    eofReached = true
                    return -1
                }
                data
            } catch (e: Exception) {
                Log.d(TAG, "read() exception: ${e.message}")
                eofReached = true
                return -1
            }
            bufferPos = 0
        }
        
        return buffer!![bufferPos++].toInt() and 0xFF
    }
    
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (closed || eofReached) return -1
        if (len == 0) return 0
        
        // Refill buffer if needed - only do ONE read, don't loop
        if (buffer == null || bufferPos >= buffer!!.size) {
            buffer = try {
                val data = adbStream.read()
                if (data == null || data.isEmpty()) {
                    Log.d(TAG, "read(b,off,len) got null/empty data - EOF")
                    eofReached = true
                    return -1
                }
                Log.d(TAG, "read(b,off,len) got ${data.size} bytes")
                data
            } catch (e: Exception) {
                Log.e(TAG, "read(b,off,len) exception: ${e.message}")
                eofReached = true
                return -1
            }
            bufferPos = 0
        }
        
        // Return what we have (don't loop for more)
        val remaining = buffer!!.size - bufferPos
        val toCopy = minOf(remaining, len)
        System.arraycopy(buffer!!, bufferPos, b, off, toCopy)
        bufferPos += toCopy
        return toCopy
    }
    
    override fun close() {
        closed = true
    }
}

/**
 * Wrapper to convert OTG AdbStream.write() to standard OutputStream.
 */
private class OtgOutputStream(
    private val adbStream: com.cgutman.adblib.AdbStream
) : OutputStream() {
    private var closed = false
    
    override fun write(b: Int) {
        if (closed) throw java.io.IOException("Stream closed")
        adbStream.write(byteArrayOf(b.toByte()))
    }
    
    override fun write(b: ByteArray, off: Int, len: Int) {
        if (closed) throw java.io.IOException("Stream closed")
        if (len == 0) return
        
        val data = if (off == 0 && len == b.size) {
            b
        } else {
            b.copyOfRange(off, off + len)
        }
        adbStream.write(data)
    }
    
    override fun flush() {
        // OTG AdbStream doesn't have explicit flush
    }
    
    override fun close() {
        closed = true
    }
}
