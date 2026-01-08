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
            try {
                val adbManager = getAdbManager()
                val escapedPath = path.replace("'", "'\\''")
                val command = "shell:ls -la '$escapedPath'"

                val stream = adbManager.openStream(command)
                val reader = BufferedReader(InputStreamReader(stream.openInputStream()))
                val files = mutableListOf<RemoteFile>()

                // Add parent directory navigation if not at root
                if (path != "/" && path.isNotEmpty()) {
                    val parentPath = File(path).parent ?: "/"
                    files.add(
                        RemoteFile(
                            name = "..",
                            path = parentPath,
                            isDirectory = true
                        )
                    )
                }

                reader.useLines { lines ->
                    lines.forEach { line ->
                        parseListLine(line, path)?.let { file ->
                            if (file.name != "." && file.name != "..") {
                                files.add(file)
                            }
                        }
                    }
                }
                stream.close()

                // Sort: directories first, then alphabetically
                val sorted = files.sortedWith(
                    compareByDescending<RemoteFile> { it.isParentDirectory }
                        .thenByDescending { it.isDirectory }
                        .thenBy { it.name.lowercase() }
                )

                Result.success(sorted)
            } catch (e: Exception) {
                Log.e(TAG, "Error listing files at $path", e)
                Result.failure(e)
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

            // Use sync protocol for pull
            val syncStream = adbManager.openStream("sync:")
            val localFile = File(localPath)
            localFile.parentFile?.mkdirs()
            
            // For simplicity, use a shell cat command to read file content
            val stream = adbManager.openStream("shell:cat '$escapedPath'")
            val inputStream = stream.openInputStream()
            val outputStream = localFile.outputStream()
            
            var bytesRead: Long = 0
            val buffer = ByteArray(8192)
            var len: Int
            
            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
                bytesRead += len
                emit(FileOperationResult.Progress(bytesRead, totalSize))
            }
            
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
            
            // Use shell and base64 encoding for reliable transfer
            val inputStream = localFile.inputStream()
            val stream = adbManager.openStream("shell:cat > '$escapedPath'")
            val outputStream = stream.openOutputStream()
            
            var bytesWritten: Long = 0
            val buffer = ByteArray(8192)
            var len: Int
            
            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
                bytesWritten += len
                emit(FileOperationResult.Progress(bytesWritten, totalSize))
            }
            
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
}
