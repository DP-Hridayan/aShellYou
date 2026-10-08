package `in`.hridayan.ashell.shell.file_browser.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.file_browser.data.executor.AdbCommandExecutor
import `in`.hridayan.ashell.shell.file_browser.data.executor.WifiAdbCommandExecutor
import `in`.hridayan.ashell.shell.file_browser.data.shell.CommandStatus
import `in`.hridayan.ashell.shell.file_browser.data.shell.RemoteShellCommands
import `in`.hridayan.ashell.shell.file_browser.data.shell.shellQuoted
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperationResult
import `in`.hridayan.ashell.shell.file_browser.domain.model.PathInfo
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile
import `in`.hridayan.ashell.shell.file_browser.domain.repository.FileBrowserRepository
import kotlinx.coroutines.CancellationException
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
import java.io.IOException
import java.io.OutputStream
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
            val command = "ls -la ${normalizedPath.shellQuoted()} 2>&1 || true"

            val fullOutput = executor.executeCommand(command)
                ?: return Result.failure(Exception("Command execution failed"))

            val rawLines = fullOutput
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

    override fun pullFile(remotePath: String, fileName: String): Flow<FileOperationResult> = flow {
        var unfinished: DownloadTarget? = null

        try {
            val totalSize = executor.stat(remotePath)?.size ?: 0L
            emit(FileOperationResult.Progress(0, totalSize))

            val target = openDownload(fileName)
            unfinished = target
            val written = writeDownload(remotePath, target, totalSize) { emit(it) }
            unfinished = null

            emit(FileOperationResult.Progress(written, totalSize))
            emit(FileOperationResult.Success(context.getString(R.string.file_downloaded)))
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling file $remotePath", e)
            runCatching { unfinished?.discard() }
            emit(transferError(e, R.string.failed_to_download_file))
        }
    }.buffer(PROGRESS_BUFFER, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        .flowOn(Dispatchers.IO)

    /** @return the number of bytes written. */
    private suspend fun writeDownload(
        remotePath: String,
        target: DownloadTarget,
        totalSize: Long,
        emit: suspend (FileOperationResult) -> Unit
    ): Long {
        var written = 0L
        target.sink.buffered(TRANSFER_BUFFER_SIZE).use { sink ->
            var lastEmitMs = 0L
            executor.pull(remotePath, sink) { transferred ->
                written = transferred
                val now = System.currentTimeMillis()
                if (now - lastEmitMs >= PROGRESS_INTERVAL_MS) {
                    lastEmitMs = now
                    emit(FileOperationResult.Progress(transferred, totalSize))
                }
            }
        }
        target.finish()
        return written
    }

    /**
     * An open download, and the two ways it can end.
     *
     * [finish] publishes it; until then the entry stays pending and is hidden from other apps.
     * [discard] removes it, so an interrupted download leaves nothing behind, not even an empty
     * media store row that would block the next attempt at the same name.
     */
    private class DownloadTarget(
        val sink: OutputStream,
        val finish: () -> Unit,
        val discard: () -> Unit
    )

    /** Removes the entry when the stream cannot be opened, so a failure leaves no empty row behind. */
    private fun openPendingSink(resolver: ContentResolver, uri: Uri): OutputStream = try {
        resolver.openOutputStream(uri)
            ?: throw IOException(context.getString(R.string.failed_to_download_file))
    } catch (e: Exception) {
        runCatching { resolver.delete(uri, null, null) }
        throw e
    }

    private fun openLegacyDownload(fileName: String): DownloadTarget {
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, fileName)
        file.parentFile?.mkdirs()
        return DownloadTarget(
            sink = file.outputStream(),
            finish = {},
            discard = { file.delete() }
        )
    }

    /**
     * Creates the destination in Downloads through the media store, which grants the app ownership
     * of what it inserts. Writing the same location as a plain path is refused for any name the app
     * does not already own.
     */
    private fun openDownload(fileName: String): DownloadTarget {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return openLegacyDownload(fileName)

        val resolver = context.contentResolver
        val pending = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, pending)
            ?: throw IOException(context.getString(R.string.failed_to_download_file))
        val sink = openPendingSink(resolver, uri)

        return DownloadTarget(
            sink = sink,
            finish = {
                val published = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                resolver.update(uri, published, null, null)
            },
            discard = { resolver.delete(uri, null, null) }
        )
    }

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

    override suspend fun deleteFile(path: String): Result<Unit> =
        runChecked(RemoteShellCommands.delete(path), longRunning = true).map { }

    override suspend fun createDirectory(path: String): Result<Unit> =
        runChecked(RemoteShellCommands.createDirectory(path)).map { }

    override suspend fun rename(oldPath: String, newPath: String): Result<Unit> =
        runChecked(RemoteShellCommands.rename(oldPath, newPath), longRunning = true).map { }

    override suspend fun getFileInfo(path: String): Result<RemoteFile> =
        withContext(Dispatchers.IO) {
            try {
                val result = executeCommand("ls -la ${path.shellQuoted()}")
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
        runChecked(RemoteShellCommands.copy(sourcePath, destPath), longRunning = true).map { }

    override suspend fun move(sourcePath: String, destPath: String): Result<Unit> =
        runChecked(RemoteShellCommands.move(sourcePath, destPath), longRunning = true).map { }

    override suspend fun inspect(paths: List<String>): Result<List<PathInfo>> {
        val infos = mutableListOf<PathInfo>()

        RemoteShellCommands.inspect(paths).forEach { (batch, command) ->
            val output = runChecked(command).getOrElse { return Result.failure(it) }
            val batchInfos = RemoteShellCommands.parseInspection(output, batch)
                ?: return Result.failure(IOException(context.getString(R.string.fb_unexpected_response)))
            infos += batchInfos
        }

        return Result.success(infos)
    }

    override suspend fun resolveDirectory(path: String): Result<String> =
        runChecked(RemoteShellCommands.resolveDirectory(path)).mapCatching { output ->
            output.lineSequence().map { it.trimEnd('\r') }.lastOrNull { it.startsWith("/") }
                ?: throw IOException(context.getString(R.string.fb_unexpected_response))
        }

    override suspend fun removeEmptyDirectory(path: String): Result<Unit> =
        runChecked(RemoteShellCommands.removeEmptyDirectory(path)).map { }

    /**
     * Runs a command built by [RemoteShellCommands] and turns its exit status into a result. No
     * answer at all counts as a failure, since a dropped connection says nothing about whether the
     * command ran.
     */
    private suspend fun runChecked(command: String, longRunning: Boolean = false): Result<String> {
        val rawOutput = try {
            withContext(Dispatchers.IO) {
                adbMutex.withLock {
                    if (longRunning) {
                        executor.executeLongRunningCommand(command)
                    } else {
                        executor.executeCommand(command)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return statusToResult(RemoteShellCommands.parseStatus(rawOutput))
    }

    private fun statusToResult(status: CommandStatus?): Result<String> = when {
        status == null -> Result.failure(IOException(context.getString(R.string.fb_no_response)))
        status.exitCode == 0 -> Result.success(status.output)
        status.exitCode == RemoteShellCommands.DESTINATION_EXISTS_STATUS ->
            Result.failure(IOException(context.getString(R.string.fb_destination_exists)))

        else -> Result.failure(
            IOException(
                status.output.ifBlank {
                    context.getString(R.string.fb_command_failed_status, status.exitCode)
                }
            )
        )
    }
}
