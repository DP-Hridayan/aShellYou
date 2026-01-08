package `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.model.FileOperationResult
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.model.RemoteFile
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.repository.FileBrowserRepository
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileBrowserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileBrowserRepository {

    private val TAG = "FileBrowser"

    private fun getAdbManager(): AbsAdbConnectionManager {
        return AdbConnectionManager.getInstance(context)
    }

    override suspend fun listFiles(path: String): Result<List<RemoteFile>> =
        withContext(Dispatchers.IO) {
            var lastException: Exception? = null
            
            // Retry up to 3 times for transient stream errors
            repeat(3) { attempt ->
                try {
                    val result = listFilesInternal(path)
                    if (result.isSuccess) {
                        return@withContext result
                    }
                    lastException = result.exceptionOrNull() as? Exception
                    Log.w(TAG, "Attempt ${attempt + 1} failed for path: $path", lastException)
                    
                    // Small delay before retry
                    if (attempt < 2) {
                        kotlinx.coroutines.delay(100)
                    }
                } catch (e: Exception) {
                    lastException = e
                    Log.w(TAG, "Attempt ${attempt + 1} exception for path: $path", e)
                    if (attempt < 2) {
                        kotlinx.coroutines.delay(100)
                    }
                }
            }
            
            Result.failure(lastException ?: Exception("Failed to list files after 3 attempts"))
        }
    
    private fun listFilesInternal(path: String): Result<List<RemoteFile>> {
        var stream: io.github.muntashirakon.adb.AdbStream? = null
        var reader: BufferedReader? = null
        
        try {
            val adbManager = getAdbManager()
            
            // Check if connected first
            if (!adbManager.isConnected) {
                return Result.failure(Exception("Not connected to device"))
            }
            
            // Normalize path - ensure trailing slash for directories to get contents not symlink info
            val normalizedPath = if (path.endsWith("/")) path else "$path/"
            val escapedPath = normalizedPath.replace("'", "'\\''")
            val command = "shell:ls -la '$escapedPath'"
            
            Log.d(TAG, "Listing files with command: $command")

            stream = adbManager.openStream(command)
            reader = BufferedReader(InputStreamReader(stream.openInputStream()))
            val files = mutableListOf<RemoteFile>()
            val rawLines = mutableListOf<String>()

            // Read all lines
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let { rawLines.add(it) }
            }
            
            // Close resources
            try { reader.close() } catch (e: Exception) { /* ignore */ }
            try { stream.close() } catch (e: Exception) { /* ignore */ }
            
            Log.d(TAG, "Got ${rawLines.size} lines from ls output")

            // Add parent directory navigation if not at root
            val cleanPath = path.trimEnd('/')
            if (cleanPath != "" && cleanPath != "/") {
                val parentPath = File(cleanPath).parent ?: "/"
                files.add(
                    RemoteFile(
                        name = "..",
                        path = parentPath,
                        isDirectory = true
                    )
                )
            }

            // Parse lines
            rawLines.forEach { l ->
                parseListLine(l, cleanPath.ifEmpty { "/" })?.let { file ->
                    if (file.name != "." && file.name != "..") {
                        files.add(file)
                    }
                }
            }

            // Sort: directories first, then alphabetically
            val sorted = files.sortedWith(
                compareByDescending<RemoteFile> { it.isParentDirectory }
                    .thenByDescending { it.isDirectory }
                    .thenBy { it.name.lowercase() }
            )
            
            Log.d(TAG, "Returning ${sorted.size} files for path: $path")

            return Result.success(sorted)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing files at $path", e)
            // Ensure resources are closed
            try { reader?.close() } catch (ex: Exception) { /* ignore */ }
            try { stream?.close() } catch (ex: Exception) { /* ignore */ }
            return Result.failure(e)
        }
    }

    /**
     * Parse a line from `ls -la` output.
     * Android ls -la format varies but typically:
     * drwxrwx--x   5 root   sdcard_rw     4096 2024-01-08 10:30 DCIM
     * -rw-rw----   1 u0_a123 u0_a123      1234 2024-01-08 10:30 file.txt
     * lrwxrwxrwx   1 root   root           13 2024-01-08 10:30 link -> target
     */
    private fun parseListLine(line: String, basePath: String): RemoteFile? {
        if (line.isBlank() || line.startsWith("total ")) return null
        
        // Skip lines that are clearly not file listings
        if (!line.matches(Regex("^[dlcbsp-].*"))) return null

        try {
            // Parse permissions (first field)
            val permissions = line.substring(0, minOf(10, line.length)).trim()
            if (permissions.length < 10) return null
            
            val isDirectory = permissions.startsWith("d")
            val isLink = permissions.startsWith("l")
            
            // Use regex to find the filename at the end, after the date/time pattern
            // Look for date pattern like "2024-01-08 10:30" or "Jan  8 10:30" or "Jan  8  2024"
            val dateTimeRegex = Regex("""(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}|\w{3}\s+\d{1,2}\s+(\d{2}:\d{2}|\d{4}))\s+(.+)$""")
            val match = dateTimeRegex.find(line)
            
            if (match != null) {
                val dateTime = match.groupValues[1]
                var name = match.groupValues[3].trim()
                
                // Skip . and ..
                if (name == "." || name == "..") return null
                
                // Handle symlinks - extract actual name from "linkname -> target"
                if (isLink && name.contains(" -> ")) {
                    name = name.substringBefore(" -> ")
                }
                
                // Parse size - find the number before the date
                val beforeDate = line.substring(0, match.range.first).trim()
                val sizeParts = beforeDate.split(Regex("\\s+"))
                val size = sizeParts.lastOrNull()?.toLongOrNull() ?: 0L
                
                val fullPath = if (basePath.endsWith("/")) "$basePath$name" else "$basePath/$name"
                
                return RemoteFile(
                    name = name,
                    path = fullPath,
                    isDirectory = isDirectory,
                    size = size,
                    permissions = permissions,
                    lastModified = dateTime,
                    owner = sizeParts.getOrElse(1) { "" },
                    group = sizeParts.getOrElse(2) { "" }
                )
            }
            
            // Fallback: Simple space-split parsing
            val parts = line.split(Regex("\\s+"))
            if (parts.size >= 7) {
                val name = parts.drop(6).joinToString(" ").let { rawName ->
                    if (isLink && rawName.contains(" -> ")) {
                        rawName.substringBefore(" -> ")
                    } else rawName
                }
                
                if (name == "." || name == "..") return null
                
                val fullPath = if (basePath.endsWith("/")) "$basePath$name" else "$basePath/$name"
                val size = parts.getOrNull(4)?.toLongOrNull() ?: 0L
                
                return RemoteFile(
                    name = name,
                    path = fullPath,
                    isDirectory = isDirectory,
                    size = size,
                    permissions = permissions,
                    lastModified = "${parts.getOrElse(5) { "" }} ${parts.getOrElse(6) { "" }}".trim(),
                    owner = parts.getOrElse(2) { "" },
                    group = parts.getOrElse(3) { "" }
                )
            }
            
            return null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse line: $line", e)
            return null
        }
    }

    override fun pullFile(remotePath: String, localPath: String): Flow<FileOperationResult> = flow {
        try {
            val adbManager = getAdbManager()
            val escapedPath = remotePath.replace("'", "'\\''")
            
            // First get file size for progress
            val sizeResult = executeCommand("stat -c%s '$escapedPath'")
            val totalSize = sizeResult?.trim()?.toLongOrNull() ?: 0L
            
            emit(FileOperationResult.Progress(0, totalSize))

            val localFile = File(localPath)
            localFile.parentFile?.mkdirs()
            
            // Use shell cat command with larger buffer for better throughput
            val stream = adbManager.openStream("shell:cat '$escapedPath'")
            val inputStream = stream.openInputStream().buffered(65536)
            val outputStream = localFile.outputStream().buffered(65536)
            
            var bytesRead: Long = 0
            val buffer = ByteArray(65536) // 64KB buffer
            var len: Int
            var lastProgressUpdate = 0L
            
            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
                bytesRead += len
                // Only emit progress every 256KB to reduce overhead
                if (bytesRead - lastProgressUpdate >= 262144 || bytesRead == totalSize) {
                    emit(FileOperationResult.Progress(bytesRead, totalSize))
                    lastProgressUpdate = bytesRead
                }
            }
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            stream.close()
            
            emit(FileOperationResult.Success("File downloaded successfully"))
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling file $remotePath", e)
            emit(FileOperationResult.Error(e.message ?: "Failed to download file"))
        }
    }.flowOn(Dispatchers.IO)

    override fun pushFile(localPath: String, remotePath: String): Flow<FileOperationResult> = flow {
        try {
            val localFile = File(localPath)
            if (!localFile.exists()) {
                emit(FileOperationResult.Error("Local file does not exist"))
                return@flow
            }

            val totalSize = localFile.length()
            emit(FileOperationResult.Progress(0, totalSize))

            val adbManager = getAdbManager()
            val escapedPath = remotePath.replace("'", "'\\''")
            
            // Use larger buffer for better throughput
            val inputStream = localFile.inputStream().buffered(65536)
            val stream = adbManager.openStream("shell:cat > '$escapedPath'")
            val outputStream = stream.openOutputStream().buffered(65536)
            
            var bytesWritten: Long = 0
            val buffer = ByteArray(65536) // 64KB buffer
            var len: Int
            var lastProgressUpdate = 0L
            
            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
                bytesWritten += len
                // Only emit progress every 256KB to reduce overhead
                if (bytesWritten - lastProgressUpdate >= 262144 || bytesWritten == totalSize) {
                    emit(FileOperationResult.Progress(bytesWritten, totalSize))
                    lastProgressUpdate = bytesWritten
                }
            }
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            stream.close()
            
            emit(FileOperationResult.Success("File uploaded successfully"))
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing file to $remotePath", e)
            emit(FileOperationResult.Error(e.message ?: "Failed to upload file"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val escapedPath = path.replace("'", "'\\''")
            val result = executeCommand("rm -rf '$escapedPath'")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file $path", e)
            Result.failure(e)
        }
    }

    override suspend fun createDirectory(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val escapedPath = path.replace("'", "'\\''")
            executeCommand("mkdir -p '$escapedPath'")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating directory $path", e)
            Result.failure(e)
        }
    }

    override suspend fun rename(oldPath: String, newPath: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val escapedOld = oldPath.replace("'", "'\\''")
                val escapedNew = newPath.replace("'", "'\\''")
                executeCommand("mv '$escapedOld' '$escapedNew'")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error renaming $oldPath to $newPath", e)
                Result.failure(e)
            }
        }

    override suspend fun getFileInfo(path: String): Result<RemoteFile> =
        withContext(Dispatchers.IO) {
            try {
                val escapedPath = path.replace("'", "'\\''")
                val result = executeCommand("ls -la '$escapedPath'")
                val parentPath = File(path).parent ?: "/"
                result?.let { line ->
                    parseListLine(line.trim(), parentPath)?.let {
                        return@withContext Result.success(it)
                    }
                }
                Result.failure(Exception("File not found"))
            } catch (e: Exception) {
                Log.e(TAG, "Error getting file info for $path", e)
                Result.failure(e)
            }
        }

    private suspend fun executeCommand(command: String): String? = withContext(Dispatchers.IO) {
        try {
            val adbManager = getAdbManager()
            val stream = adbManager.openStream("shell:$command")
            val reader = BufferedReader(InputStreamReader(stream.openInputStream()))
            val result = reader.readText()
            reader.close()
            stream.close()
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error executing command: $command", e)
            null
        }
    }
    
    override fun isAdbConnected(): Boolean {
        return try {
            getAdbManager().isConnected
        } catch (e: Exception) {
            Log.e(TAG, "Error checking ADB connection", e)
            false
        }
    }
}
