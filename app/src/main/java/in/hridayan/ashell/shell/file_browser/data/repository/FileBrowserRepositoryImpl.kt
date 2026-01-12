package `in`.hridayan.ashell.shell.file_browser.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import `in`.hridayan.ashell.shell.file_browser.data.executor.AdbCommandExecutor
import `in`.hridayan.ashell.shell.file_browser.data.executor.WifiAdbCommandExecutor
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperationResult
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile
import `in`.hridayan.ashell.shell.file_browser.domain.repository.FileBrowserRepository
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileBrowserRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val wifiAdbExecutor: WifiAdbCommandExecutor
) : FileBrowserRepository {
    private val TAG = "FileBrowser"
    private val adbMutex = Mutex()

    // Current executor - defaults to WiFi, can be switched for OTG
    private var executor: AdbCommandExecutor = wifiAdbExecutor

    /**
     * Switch the command executor (for OTG support).
     * EXISTING LOGIC PRESERVED: All parsing, retry, mutex logic remains unchanged.
     */
    fun setExecutor(newExecutor: AdbCommandExecutor) {
        executor = newExecutor
    }

    /**
     * Reset to default WiFi executor.
     */
    fun resetToWifiExecutor() {
        executor = wifiAdbExecutor
    }

    private fun getAdbManager(): AbsAdbConnectionManager {
        return AdbConnectionManager.getInstance(context)
    }

    override suspend fun listFiles(path: String): Result<List<RemoteFile>> =
        withContext(Dispatchers.IO) {
            adbMutex.withLock {
                var lastException: Exception? = null

                repeat(3) { attempt ->
                    try {
                        val result = listFilesInternal(path)
                        if (result.isSuccess) {
                            return@withContext result
                        }
                        lastException = result.exceptionOrNull() as? Exception
                        Log.w(TAG, "Attempt ${attempt + 1} failed for path: $path", lastException)

                        if (attempt < 2) {
                            delay(100)
                        }
                    } catch (e: Exception) {
                        lastException = e
                        Log.w(TAG, "Attempt ${attempt + 1} exception for path: $path", e)
                        if (attempt < 2) {
                            delay(100)
                        }
                    }
                }

                Result.failure(lastException ?: Exception("Failed to list files after 3 attempts"))
            }
        }

    /**
     * Internal implementation of listFiles.
     * MODIFIED: Now uses executor.executeCommand() instead of direct stream access.
     * PRESERVED: All parsing logic (parseListLine, sorting, parent directory) is unchanged.
     */
    private suspend fun listFilesInternal(path: String): Result<List<RemoteFile>> {
        try {
            if (!executor.isConnected()) {
                return Result.failure(Exception("Not connected to device"))
            }

            val normalizedPath = if (path.endsWith("/")) path else "$path/"
            val escapedPath = normalizedPath.replace("'", "'\\''")

            // Use executor.executeCommand instead of direct stream
            val command = "(ls -la '$escapedPath' 2>&1 || true); echo '__END__'"

            Log.d(TAG, "Listing files with command: $command")

            val fullOutput = executor.executeCommand(command)
                ?: return Result.failure(Exception("Command execution failed"))

            Log.d(TAG, "Got output length: ${fullOutput.length}")

            // Debug: log first few lines of raw output
            val debugLines = fullOutput.take(500)
            Log.d(TAG, "Raw output (first 500 chars): $debugLines")

            // UNCHANGED: All parsing logic below is preserved exactly as before
            val rawLines = fullOutput
                .replace("__END__", "")
                .trim()
                .split("\n")
                .filter { it.isNotBlank() }

            Log.d(TAG, "Got ${rawLines.size} lines from ls output")
            rawLines.firstOrNull { it.contains(" ") }?.let {
                Log.d(TAG, "Sample line with spaces: $it")
            }

            val files = mutableListOf<RemoteFile>()

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

            rawLines.forEach { l ->
                parseListLine(l, cleanPath.ifEmpty { "/" })?.let { file ->
                    if (file.name != "." && file.name != "..") {
                        files.add(file)
                    }
                }
            }

            val sorted = files.sortedWith(
                compareByDescending<RemoteFile> { it.isParentDirectory }
                    .thenByDescending { it.isDirectory }
                    .thenBy { it.name.lowercase() }
            )

            Log.d(TAG, "Returning ${sorted.size} files for path: $path")

            return Result.success(sorted)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing files at $path", e)
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
        // Note: 'ls:' error messages start with 'l' which would match the regex, skip them explicitly
        if (!line.matches(Regex("^[dlcbsp-].*")) || line.startsWith("ls:")) return null

        try {
            val permissions = line.take(minOf(10, line.length)).trim()
            if (permissions.length < 10) return null

            val isDirectory = permissions.startsWith("d")
            val isLink = permissions.startsWith("l")

            // Use regex to find the filename at the end, after the date/time pattern
            // Look for date pattern like "2024-01-08 10:30" or "Jan  8 10:30" or "Jan  8  2024"
            val dateTimeRegex =
                Regex("""(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}|\w{3}\s+\d{1,2}\s+(\d{2}:\d{2}|\d{4}))\s+(.+)$""")
            val match = dateTimeRegex.find(line)

            if (match != null) {
                val dateTime = match.groupValues[1]
                // Unescape backslash-escaped characters (ls sometimes displays "My\ File" for "My File")
                var name = match.groupValues[3].trim().replace("\\ ", " ")

                // Skip . and ..
                if (name == "." || name == "..") return null

                // Handle symlinks - extract actual name from "linkname -> target"
                if (isLink && name.contains(" -> ")) {
                    name = name.substringBefore(" -> ")
                }

                // Parse size - find the number before the date
                val beforeDate = line.take(match.range.first).trim()
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
            val escapedPath = remotePath.replace("'", "'\\''")

            // First get file size for progress
            val sizeResult = executeCommand("stat -c%s '$escapedPath'")
            val totalSize = sizeResult?.trim()?.toLongOrNull() ?: 0L

            emit(FileOperationResult.Progress(0, totalSize))

            val localFile = File(localPath)
            localFile.parentFile?.mkdirs()

            // Use executor's openReadStream for both WiFi and OTG
            val transferStream = executor.openReadStream("shell:cat '$escapedPath'")
            if (transferStream?.inputStream == null) {
                emit(FileOperationResult.Error("Failed to open stream for download"))
                return@flow
            }
            
            val inputStream = transferStream.inputStream
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
            transferStream.close()

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

            val escapedPath = remotePath.replace("'", "'\\''")

            // Use executor's openWriteStream for both WiFi and OTG
            val transferStream = executor.openWriteStream("shell:cat > '$escapedPath'")
            if (transferStream?.outputStream == null) {
                emit(FileOperationResult.Error("Failed to open stream for upload"))
                return@flow
            }

            val inputStream = localFile.inputStream().buffered(65536)
            val outputStream = transferStream.outputStream

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

            inputStream.close()
            transferStream.close()

            emit(FileOperationResult.Success("File uploaded successfully"))
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing file to $remotePath", e)
            emit(FileOperationResult.Error(e.message ?: "Failed to upload file"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val escapedPath = path.replace("'", "'\\''")
            executeCommand("rm -rf '$escapedPath'")
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

    /**
     * Execute command using the current executor.
     * EXISTING LOGIC PRESERVED: Mutex serialization is kept here, retry/timeout moved to executor.
     */
    private suspend fun executeCommand(command: String): String? = withContext(Dispatchers.IO) {
        // Serialize all ADB commands to prevent concurrent stream issues
        adbMutex.withLock {
            // Use the executor (supports both WiFi ADB and OTG)
            executor.executeCommand(command)
        }
    }

    override fun isAdbConnected(): Boolean {
        return executor.isConnected()
    }

    override suspend fun copy(sourcePath: String, destPath: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val escapedSource = sourcePath.replace("'", "'\\''")
                val escapedDest = destPath.replace("'", "'\\''")
                val command = "cp -r '$escapedSource' '$escapedDest'"
                val result = executeCommand(command)

                // Check if copy was successful
                if (result?.contains("error", ignoreCase = true) == true ||
                    result?.contains("cannot", ignoreCase = true) == true
                ) {
                    Result.failure(Exception(result))
                } else {
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error copying: $sourcePath -> $destPath", e)
                Result.failure(e)
            }
        }

    override suspend fun move(sourcePath: String, destPath: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val escapedSource = sourcePath.replace("'", "'\\''")
                val escapedDest = destPath.replace("'", "'\\''")
                val command = "mv '$escapedSource' '$escapedDest'"
                val result = executeCommand(command)

                // Check if move was successful
                if (result?.contains("error", ignoreCase = true) == true ||
                    result?.contains("cannot", ignoreCase = true) == true
                ) {
                    Result.failure(Exception(result))
                } else {
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error moving: $sourcePath -> $destPath", e)
                Result.failure(e)
            }
        }

    override suspend fun exists(path: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val escapedPath = path.replace("'", "'\\'")
            val command = "[ -e '$escapedPath' ] && echo 'EXISTS' || echo 'NOT_EXISTS'"

            Log.d(TAG, "exists: checking path=$path")

            val result = executeCommand(command)

            if (result == null) {
                Log.e(TAG, "exists: executeCommand returned null for $path")
                return@withContext Result.failure(Exception("ADB command failed for exists check"))
            }

            val exists = result.contains("EXISTS") && !result.contains("NOT_EXISTS")
            Log.d(TAG, "exists: path=$path, result=$exists, raw='${result.trim()}'")

            Result.success(exists)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking exists: $path", e)
            Result.failure(e)
        }
    }

    override suspend fun isDirectory(path: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val escapedPath = path.replace("'", "'\\'")
            val command = "[ -d '$escapedPath' ] && echo 'IS_DIR' || echo 'NOT_DIR'"

            Log.d(TAG, "isDirectory: checking path=$path")

            val result = executeCommand(command)

            if (result == null) {
                Log.e(TAG, "isDirectory: executeCommand returned null for $path")
                return@withContext Result.failure(Exception("ADB command failed for isDirectory check"))
            }

            val isDir = result.contains("IS_DIR") && !result.contains("NOT_DIR")
            Log.d(TAG, "isDirectory: path=$path, result=$isDir")

            Result.success(isDir)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking isDirectory: $path", e)
            Result.failure(e)
        }
    }

    override suspend fun delete(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val escapedPath = path.replace("'", "'\\'")
            executeCommand("rm -rf '$escapedPath'")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting $path", e)
            Result.failure(e)
        }
    }
}
