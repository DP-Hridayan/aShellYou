package `in`.hridayan.ashell.shell.file_browser.data.executor

import android.util.Log
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.repository.OtgRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OTG ADB command executor.
 * Uses OtgRepository's AdbConnection for transport.
 * 
 * File transfers use shell:cat commands with buffered reads/writes,
 * similar to WiFi ADB but adapted for cgutman's adblib AdbStream.
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
     * OTG does NOT use sync transfer - uses stream-based like WiFi.
     */
    override fun supportsSyncTransfer(): Boolean = false
    
    /**
     * Execute command using OTG AdbConnection.
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
            
            withTimeoutOrNull(10000L) {
                val stream = adbConnection.open("shell:$command")
                
                try {
                    val output = StringBuilder()
                    while (true) {
                        val data = stream.read()
                        if (data == null || data.isEmpty()) break
                        val text = String(data, Charsets.UTF_8)
                        output.append(text)
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
     */
    override fun openCommandStream(command: String): CommandStream? {
        return try {
            if (!isConnected()) return null
            
            val adbConnection = otgRepository.getAdbConnection() ?: return null
            val stream = adbConnection.open(command)
            
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
     * Uses shell:cat like WiFi, but wraps AdbStream.read() in a proper InputStream.
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
            val adbStream = adbConnection.open(command)
            Log.d(TAG, "openReadStream: Stream opened successfully")
            
            // Create a wrapper InputStream that reads from OTG AdbStream
            val inputStream = OtgInputStream(adbStream)
            
            FileTransferStream(
                inputStream = inputStream,
                close = {
                    Log.d(TAG, "openReadStream: Closing stream")
                    try { inputStream.close() } catch (_: Exception) {}
                    try { adbStream.close() } catch (_: Exception) {}
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error opening read stream: $command", e)
            null
        }
    }
    
    /**
     * Open a write stream for uploading files via OTG.
     * Uses shell:cat > file, wraps AdbStream.write() in a proper OutputStream.
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
            val adbStream = adbConnection.open(command)
            Log.d(TAG, "openWriteStream: Stream opened successfully")
            
            // Create a wrapper OutputStream that writes to OTG AdbStream
            val outputStream = OtgOutputStream(adbStream)
            
            FileTransferStream(
                outputStream = outputStream,
                close = {
                    Log.d(TAG, "openWriteStream: Closing stream (flushing ${outputStream.pendingBytes()} bytes)")
                    try { outputStream.flush() } catch (e: Exception) {
                        Log.e(TAG, "Error flushing on close", e)
                    }
                    try { outputStream.close() } catch (_: Exception) {}
                    try { adbStream.close() } catch (_: Exception) {}
                    Log.d(TAG, "openWriteStream: Stream closed")
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error opening write stream: $command", e)
            null
        }
    }
}

/**
 * InputStream wrapper for OTG AdbStream.
 * Buffers data from AdbStream.read() which returns byte arrays.
 */
private class OtgInputStream(
    private val adbStream: com.cgutman.adblib.AdbStream
) : InputStream() {
    
    private val TAG = "OtgInputStream"
    private var buffer: ByteArray? = null
    private var bufferPos = 0
    private var closed = false
    private var eofReached = false
    private var totalBytesRead = 0L
    
    override fun read(): Int {
        if (closed || eofReached) return -1
        
        // Refill buffer if needed
        if (buffer == null || bufferPos >= buffer!!.size) {
            if (!refillBuffer()) return -1
        }
        
        return buffer!![bufferPos++].toInt() and 0xFF
    }
    
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (closed || eofReached) return -1
        if (len == 0) return 0
        
        var totalRead = 0
        
        while (totalRead < len) {
            // Refill buffer if needed
            if (buffer == null || bufferPos >= buffer!!.size) {
                if (!refillBuffer()) break
            }
            
            // Copy from buffer
            val available = buffer!!.size - bufferPos
            val toCopy = minOf(available, len - totalRead)
            System.arraycopy(buffer!!, bufferPos, b, off + totalRead, toCopy)
            bufferPos += toCopy
            totalRead += toCopy
        }
        
        return if (totalRead > 0) totalRead else -1
    }
    
    private fun refillBuffer(): Boolean {
        if (eofReached) return false
        
        try {
            val data = adbStream.read()
            if (data == null || data.isEmpty()) {
                eofReached = true
                Log.d(TAG, "EOF reached after $totalBytesRead bytes")
                return false
            }
            buffer = data
            bufferPos = 0
            totalBytesRead += data.size
            return true
        } catch (e: java.io.IOException) {
            // Stream closed is normal EOF
            Log.d(TAG, "IOException (EOF): ${e.message}, total read: $totalBytesRead")
            eofReached = true
            return false
        } catch (e: Exception) {
            Log.w(TAG, "Read exception: ${e.message}, total read: $totalBytesRead")
            eofReached = true
            return false
        }
    }
    
    override fun available(): Int {
        return if (buffer != null && bufferPos < buffer!!.size) {
            buffer!!.size - bufferPos
        } else {
            0
        }
    }
    
    override fun close() {
        closed = true
        Log.d(TAG, "Closed after reading $totalBytesRead bytes")
    }
}

/**
 * OutputStream wrapper for OTG AdbStream.
 * Writes directly to ADB stream for each chunk.
 */
private class OtgOutputStream(
    private val adbStream: com.cgutman.adblib.AdbStream
) : OutputStream() {
    
    private val TAG = "OtgOutputStream"
    private val pendingData = ByteArrayOutputStream()
    private var closed = false
    private var totalBytesWritten = 0L
    
    // ADB max payload is around 64KB, but we use smaller chunks for safety
    private val maxChunkSize = 4096  // 4KB chunks for reliability
    
    fun pendingBytes(): Int = pendingData.size()
    
    override fun write(b: Int) {
        if (closed) throw java.io.IOException("Stream closed")
        pendingData.write(b)
        // Flush when we have enough data
        if (pendingData.size() >= maxChunkSize) {
            flushInternal()
        }
    }
    
    override fun write(b: ByteArray, off: Int, len: Int) {
        if (closed) throw java.io.IOException("Stream closed")
        if (len == 0) return
        
        Log.d(TAG, "write() called with $len bytes")
        pendingData.write(b, off, len)
        
        // Flush when we have enough data
        while (pendingData.size() >= maxChunkSize) {
            flushInternal()
        }
    }
    
    private fun flushInternal() {
        if (pendingData.size() == 0) return
        
        val data = pendingData.toByteArray()
        val toWrite = minOf(data.size, maxChunkSize)
        
        Log.d(TAG, "flushInternal: writing $toWrite bytes to ADB stream")
        
        try {
            adbStream.write(data.copyOf(toWrite))
            totalBytesWritten += toWrite
            Log.d(TAG, "Successfully wrote $toWrite bytes, total: $totalBytesWritten")
            
            // Keep remaining data
            pendingData.reset()
            if (toWrite < data.size) {
                pendingData.write(data, toWrite, data.size - toWrite)
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "Write interrupted after $totalBytesWritten bytes", e)
            throw java.io.IOException("Write interrupted", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to ADB stream after $totalBytesWritten bytes", e)
            throw java.io.IOException("Failed to write to ADB stream: ${e.message}", e)
        }
    }
    
    override fun flush() {
        if (closed) return
        Log.d(TAG, "flush() called, pending: ${pendingData.size()} bytes")
        
        // Write all remaining data
        while (pendingData.size() > 0) {
            flushInternal()
        }
    }
    
    override fun close() {
        if (closed) return
        Log.d(TAG, "close() called, pending: ${pendingData.size()} bytes, total written: $totalBytesWritten")
        flush()
        closed = true
        Log.d(TAG, "Stream closed after writing $totalBytesWritten bytes total")
    }
}
