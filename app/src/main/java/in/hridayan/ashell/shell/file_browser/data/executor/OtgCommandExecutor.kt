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
 * Uses shell-based file transfers with improved buffering and base64 encoding
 * for reliable binary file transfers.
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
     * OTG supports progress-aware transfers via base64.
     */
    override fun supportsSyncTransfer(): Boolean = true
    
    /**
     * Pull file using base64 encoding for safe binary transfer.
     * This is more reliable than raw cat for binary files over OTG.
     */
    override fun pullFileWithProgress(
        remotePath: String,
        totalSize: Long,
        onProgress: (Long, Long) -> Unit
    ): InputStream? {
        Log.d(TAG, "pullFileWithProgress: $remotePath (size: $totalSize)")
        return try {
            if (!isConnected()) {
                Log.w(TAG, "pullFileWithProgress: OTG not connected")
                return null
            }
            
            val adbConnection = otgRepository.getAdbConnection()
            if (adbConnection == null) {
                Log.w(TAG, "pullFileWithProgress: getAdbConnection returned null")
                return null
            }
            
            val escapedPath = remotePath.replace("'", "'\\''")
            // Use base64 encoding for safe binary transfer
            val command = "base64 '$escapedPath'"
            
            Log.d(TAG, "pullFileWithProgress: Executing base64 read command")
            
            val stream = adbConnection.open("shell:$command")
            
            // Read all base64 data with timeout
            val base64Data = StringBuilder()
            var bytesRead = 0L
            var lastProgressUpdate = 0L
            
            try {
                while (true) {
                    val data = stream.read()
                    if (data == null || data.isEmpty()) break
                    
                    val text = String(data, Charsets.UTF_8)
                    base64Data.append(text)
                    bytesRead += data.size
                    
                    // Report progress based on expected base64 size (roughly 4/3 of original)
                    val estimatedOriginalBytes = (bytesRead * 3 / 4)
                    if (estimatedOriginalBytes - lastProgressUpdate >= 65536) {
                        onProgress(estimatedOriginalBytes, totalSize)
                        lastProgressUpdate = estimatedOriginalBytes
                    }
                }
            } finally {
                try { stream.close() } catch (_: Exception) {}
            }
            
            // Decode base64 to binary
            val base64String = base64Data.toString()
                .replace("\r", "")
                .replace("\n", "")
                .trim()
            
            if (base64String.isEmpty()) {
                Log.e(TAG, "pullFileWithProgress: Empty base64 data")
                return null
            }
            
            Log.d(TAG, "pullFileWithProgress: Decoding ${base64String.length} base64 chars")
            
            val binaryData = try {
                android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e(TAG, "pullFileWithProgress: Base64 decode failed", e)
                return null
            }
            
            Log.d(TAG, "pullFileWithProgress: Decoded ${binaryData.size} bytes")
            onProgress(binaryData.size.toLong(), totalSize)
            
            ByteArrayInputStream(binaryData)
        } catch (e: Exception) {
            Log.e(TAG, "pullFileWithProgress failed for $remotePath", e)
            null
        }
    }
    
    /**
     * Push file using base64 encoding for safe binary transfer.
     */
    override fun pushFileWithProgress(
        remotePath: String,
        data: ByteArray,
        onProgress: (Long, Long) -> Unit
    ): Boolean {
        Log.d(TAG, "pushFileWithProgress: $remotePath (size: ${data.size})")
        return try {
            if (!isConnected()) {
                Log.w(TAG, "pushFileWithProgress: OTG not connected")
                return false
            }
            
            val adbConnection = otgRepository.getAdbConnection()
            if (adbConnection == null) {
                Log.w(TAG, "pushFileWithProgress: getAdbConnection returned null")
                return false
            }
            
            val escapedPath = remotePath.replace("'", "'\\''")
            
            // Encode to base64
            val base64String = android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP)
            Log.d(TAG, "pushFileWithProgress: Encoded to ${base64String.length} base64 chars")
            
            // Write via echo and base64 decode
            // Split into chunks to avoid command line length limits
            val chunkSize = 32768 // 32KB chunks of base64 data
            val totalChunks = (base64String.length + chunkSize - 1) / chunkSize
            
            // First chunk creates the file
            var offset = 0
            var chunkNum = 0
            
            while (offset < base64String.length) {
                val endOffset = minOf(offset + chunkSize, base64String.length)
                val chunk = base64String.substring(offset, endOffset)
                
                val operator = if (offset == 0) ">" else ">>"
                val command = "echo -n '$chunk' | base64 -d $operator '$escapedPath'"
                
                val stream = adbConnection.open("shell:$command")
                try {
                    // Wait for command to complete by reading any output
                    while (true) {
                        val response = stream.read() ?: break
                        if (response.isEmpty()) break
                    }
                } finally {
                    try { stream.close() } catch (_: Exception) {}
                }
                
                offset = endOffset
                chunkNum++
                
                // Report progress
                val bytesWritten = (offset.toLong() * data.size / base64String.length)
                onProgress(bytesWritten, data.size.toLong())
            }
            
            Log.d(TAG, "pushFileWithProgress: Wrote $totalChunks chunks successfully")
            onProgress(data.size.toLong(), data.size.toLong())
            true
        } catch (e: Exception) {
            Log.e(TAG, "pushFileWithProgress failed for $remotePath", e)
            false
        }
    }
    
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
     * Falls back to non-progress version.
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
            
            // Extract path from command
            val remotePath = extractPathFromCatCommand(command)
            if (remotePath == null) {
                Log.e(TAG, "Could not extract path from command: $command")
                return null
            }
            
            // Use pullFileWithProgress but without progress callback
            val inputStream = pullFileWithProgress(remotePath, 0) { _, _ -> }
            
            if (inputStream == null) {
                Log.e(TAG, "Failed to pull file: $remotePath")
                return null
            }
            
            FileTransferStream(
                inputStream = inputStream,
                close = { try { inputStream.close() } catch (_: Exception) {} }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error opening read stream: $command", e)
            null
        }
    }
    
    /**
     * Open a write stream for uploading files via OTG.
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
            
            val remotePath = extractPathFromCatWriteCommand(command)
            if (remotePath == null) {
                Log.e(TAG, "Could not extract path from write command: $command")
                return null
            }
            
            val outputStream = OtgBufferedOutputStream(this, remotePath)
            
            FileTransferStream(
                outputStream = outputStream,
                close = { try { outputStream.close() } catch (_: Exception) {} }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error opening write stream: $command", e)
            null
        }
    }
    
    private fun extractPathFromCatCommand(command: String): String? {
        val patterns = listOf(
            Regex("""shell:cat\s+'([^']+)'"""),
            Regex("""shell:cat\s+"([^"]+)""""),
            Regex("""shell:cat\s+(\S+)""")
        )
        for (pattern in patterns) {
            val match = pattern.find(command)
            if (match != null) return match.groupValues[1].replace("\\'", "'")
        }
        return null
    }
    
    private fun extractPathFromCatWriteCommand(command: String): String? {
        val patterns = listOf(
            Regex("""shell:cat\s*>\s*'([^']+)'"""),
            Regex("""shell:cat\s*>\s*"([^"]+)""""),
            Regex("""shell:cat\s*>\s*(\S+)""")
        )
        for (pattern in patterns) {
            val match = pattern.find(command)
            if (match != null) return match.groupValues[1].replace("\\'", "'")
        }
        return null
    }
}

/**
 * OutputStream that buffers data and uploads via base64 when closed.
 */
private class OtgBufferedOutputStream(
    private val executor: OtgCommandExecutor,
    private val remotePath: String
) : OutputStream() {
    
    private val buffer = ByteArrayOutputStream()
    private var closed = false
    
    override fun write(b: Int) {
        if (closed) throw java.io.IOException("Stream closed")
        buffer.write(b)
    }
    
    override fun write(b: ByteArray, off: Int, len: Int) {
        if (closed) throw java.io.IOException("Stream closed")
        if (len > 0) buffer.write(b, off, len)
    }
    
    override fun flush() {}
    
    override fun close() {
        if (closed) return
        closed = true
        
        val data = buffer.toByteArray()
        val success = executor.pushFileWithProgress(remotePath, data) { _, _ -> }
        if (!success) {
            throw java.io.IOException("Failed to upload file")
        }
    }
}
