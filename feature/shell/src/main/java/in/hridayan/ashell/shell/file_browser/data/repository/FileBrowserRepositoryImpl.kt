package `in`.hridayan.ashell.shell.file_browser.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.shell.file_browser.data.executor.AdbCommandExecutor
import `in`.hridayan.ashell.shell.file_browser.data.executor.WifiAdbCommandExecutor
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperationResult
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile
import `in`.hridayan.ashell.shell.file_browser.domain.repository.FileBrowserRepository
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
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

    companion object {
        const val TAG = "FileBrowser"
        private const val TRANSFER_BUFFER_SIZE = 64 * 1024
        private const val PROGRESS_INTERVAL_MS = 100L
        private const val PROGRESS_BUFFER = 4
    }

    /**
     * Turns a transfer failure into a user facing line, preferring the message the device sent over
     * a generic one. Piping through the shell could not report a reason at all.
     */
    private fun transferError(error: Throwable, fallback: Int): FileOperationResult.Error {
        val detail = error.message?.takeIf { it.isNotBlank() }
        return FileOperationResult.Error(detail ?: context.getString(fallback))
    }

    private val adbMutex = Mutex()
    private var executor: AdbCommandExecutor = wifiAdbExecutor

    fun setExecutor(newExecutor: AdbCommandExecutor) {
        executor = newExecutor
    }

    fun resetToWifiExecutor() {
        executor = wifiAdbExecutor
    }

    override suspend fun listFiles(path: String): Result<List<RemoteFile>> =
        withContext(Dispatchers.IO) {
            adbMutex.withLock {
                var lastException: Exception? = null
                repeat(3) { attempt ->
                    try {
                        val result = fetchRemoteFiles(path)
                        if (result.isSuccess) return@withContext result
                        lastException = result.exceptionOrNull() as? Exception
                        if (attempt < 2) delay(100)
                    } catch (e: Exception) {
                        lastException = e
                        if (attempt < 2) delay(100)
                    }
                }
                Result.failure(lastException ?: Exception("Failed to list files after 3 attempts"))
            }
        }

    private suspend fun fetchRemoteFiles(path: String): Result<List<RemoteFile>> {
        try {
            if (!executor.isConnected()) {
                return Result.failure(Exception("Not connected to device"))
            }

            val normalizedPath = if (path.endsWith("/")) path else "$path/"
            val escapedPath = normalizedPath.replace("'", "'\\''")
            val command = "(ls -la '$escapedPath' 2>&1 || true); echo '__END__'"

            val fullOutput = executor.executeCommand(command)
                ?: return Result.failure(Exception("Command execution failed"))

            val rawLines = fullOutput
                .replace("__END__", "")
                .trim()
                .split("\n")
                .filter { it.isNotBlank() }

            val files = mutableListOf<RemoteFile>()
            val cleanPath = path.trimEnd('/')

            if (cleanPath.isNotEmpty() && cleanPath != "/") {
                val parentPath = File(cleanPath).parent ?: "/"
                files.add(RemoteFile(name = "..", path = parentPath, isDirectory = true))
            }

            rawLines.forEach { line ->
                parseFileEntry(line, cleanPath.ifEmpty { "/" })?.let { file ->
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

            return Result.success(sorted)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing files at $path", e)
            return Result.failure(e)
        }
    }

    private fun parseFileEntry(line: String, basePath: String): RemoteFile? {
        if (line.isBlank() || line.startsWith("total ")) return null
        if (!line.matches(Regex("^[dlcbsp-].*")) || line.startsWith("ls:")) return null

        try {
            val permissions = line.take(minOf(10, line.length)).trim()
            if (permissions.length < 10) return null

            val isDirectory = permissions.startsWith("d")
            val isLink = permissions.startsWith("l")

            val dateTimeRegex =
                Regex("""(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}|\w{3}\s+\d{1,2}\s+(\d{2}:\d{2}|\d{4}))\s+(.+)$""")
            val match = dateTimeRegex.find(line)

            if (match != null) {
                val dateTime = match.groupValues[1]
                var name = match.groupValues[3].trim().replace("\\ ", " ")

                if (name == "." || name == "..") return null
                if (isLink && name.contains(" -> ")) {
                    name = name.substringBefore(" -> ")
                }

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

            val parts = line.split(Regex("\\s+"))
            if (parts.size >= 7) {
                val name = parts.drop(6).joinToString(" ").let { rawName ->
                    if (isLink && rawName.contains(" -> ")) rawName.substringBefore(" -> ") else rawName
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
            return null
        }
    }

    override fun pullFile(remotePath: String, localPath: String): Flow<FileOperationResult> = flow {
        val localFile = File(localPath)

        try {
            val totalSize = executor.stat(remotePath)?.size ?: 0L
            emit(FileOperationResult.Progress(0, totalSize))

            localFile.parentFile?.mkdirs()

            localFile.outputStream().buffered(TRANSFER_BUFFER_SIZE).use { sink ->
                var lastEmitMs = 0L
                executor.pull(remotePath, sink) { transferred ->
                    val now = System.currentTimeMillis()
                    if (now - lastEmitMs >= PROGRESS_INTERVAL_MS) {
                        lastEmitMs = now
                        emit(FileOperationResult.Progress(transferred, totalSize))
                    }
                }
            }

            emit(FileOperationResult.Progress(localFile.length(), totalSize))
            emit(FileOperationResult.Success(context.getString(R.string.file_downloaded)))
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling file $remotePath", e)
            runCatching { if (localFile.exists()) localFile.delete() }
            emit(transferError(e, R.string.failed_to_download_file))
        }
    }.buffer(PROGRESS_BUFFER, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        .flowOn(Dispatchers.IO)

    override fun pushFile(localPath: String, remotePath: String): Flow<FileOperationResult> = flow {
        val localFile = File(localPath)
        if (!localFile.exists()) {
            emit(FileOperationResult.Error(context.getString(R.string.local_file_does_not_exist)))
            return@flow
        }

        try {
            val totalSize = localFile.length()
            emit(FileOperationResult.Progress(0, totalSize))

            localFile.inputStream().buffered(TRANSFER_BUFFER_SIZE).use { source ->
                var lastEmitMs = 0L
                executor.push(source, remotePath) { transferred ->
                    val now = System.currentTimeMillis()
                    if (now - lastEmitMs >= PROGRESS_INTERVAL_MS) {
                        lastEmitMs = now
                        emit(FileOperationResult.Progress(transferred, totalSize))
                    }
                }
            }

            emit(FileOperationResult.Progress(totalSize, totalSize))
            emit(FileOperationResult.Success(context.getString(R.string.file_uploaded)))
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing file to $remotePath", e)
            emit(transferError(e, R.string.failed_to_upload_file))
        }
    }.buffer(PROGRESS_BUFFER, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        .flowOn(Dispatchers.IO)

    override suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val escapedPath = path.replace("'", "'\\''")
            executeCommand("rm -rf '$escapedPath'")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDirectory(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val escapedPath = path.replace("'", "'\\''")
            executeCommand("mkdir -p '$escapedPath'")
            Result.success(Unit)
        } catch (e: Exception) {
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
                    parseFileEntry(line.trim(), parentPath)?.let {
                        return@withContext Result.success(it)
                    }
                }
                Result.failure(Exception("File not found"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun executeCommand(command: String): String? = withContext(Dispatchers.IO) {
        adbMutex.withLock {
            executor.executeCommand(command)
        }
    }

    override fun isAdbConnected(): Boolean = executor.isConnected()

    override suspend fun copy(sourcePath: String, destPath: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val escapedSource = sourcePath.replace("'", "'\\''")
                val escapedDest = destPath.replace("'", "'\\''")
                val result = executeCommand("cp -r '$escapedSource' '$escapedDest'")

                if (result?.contains("error", ignoreCase = true) == true ||
                    result?.contains("cannot", ignoreCase = true) == true
                ) {
                    Result.failure(Exception(result))
                } else {
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun move(sourcePath: String, destPath: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val escapedSource = sourcePath.replace("'", "'\\''")
                val escapedDest = destPath.replace("'", "'\\''")
                val result = executeCommand("mv '$escapedSource' '$escapedDest'")

                if (result?.contains("error", ignoreCase = true) == true ||
                    result?.contains("cannot", ignoreCase = true) == true
                ) {
                    Result.failure(Exception(result))
                } else {
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun exists(path: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val escapedPath = path.replace("'", "'\\'")
            val result =
                executeCommand("[ -e '$escapedPath' ] && echo 'EXISTS' || echo 'NOT_EXISTS'")
                    ?: return@withContext Result.failure(Exception("ADB command failed"))
            val exists = result.contains("EXISTS") && !result.contains("NOT_EXISTS")
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isDirectory(path: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val escapedPath = path.replace("'", "'\\'")
            val result = executeCommand("[ -d '$escapedPath' ] && echo 'IS_DIR' || echo 'NOT_DIR'")
                ?: return@withContext Result.failure(Exception("ADB command failed"))
            val isDir = result.contains("IS_DIR") && !result.contains("NOT_DIR")
            Result.success(isDir)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val escapedPath = path.replace("'", "'\\'")
            executeCommand("rm -rf '$escapedPath'")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
