package `in`.hridayan.ashell.shell.file_browser.domain.repository

import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperationResult
import `in`.hridayan.ashell.shell.file_browser.domain.model.PathInfo
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for file browser operations on remote ADB device.
 */
interface FileBrowserRepository {

    /**
     * List files and directories at the given path.
     */
    suspend fun listFiles(path: String): Result<List<RemoteFile>>

    /**
     * Pull (download) a file from the remote device into the local Downloads folder.
     *
     * @param remotePath Path on remote device
     * @param fileName Name to save under. The repository owns the destination, because shared
     * storage only accepts a write through the media store.
     */
    fun pullFile(remotePath: String, fileName: String): Flow<FileOperationResult>

    /**
     * Push (upload) a file from local storage to remote device.
     * @param localPath Path on local device
     * @param remotePath Path on remote device to save to
     */
    fun pushFile(localPath: String, remotePath: String): Flow<FileOperationResult>

    /**
     * Delete a file or directory on remote device.
     */
    suspend fun deleteFile(path: String): Result<Unit>

    /**
     * Create a directory on remote device.
     */
    suspend fun createDirectory(path: String): Result<Unit>

    /**
     * Rename a file or directory on remote device.
     */
    suspend fun rename(oldPath: String, newPath: String): Result<Unit>

    /**
     * Get file information.
     */
    suspend fun getFileInfo(path: String): Result<RemoteFile>

    /**
     * Check if ADB is currently connected.
     * Used to differentiate between empty folder and connection error.
     */
    fun isAdbConnected(): Boolean

    /**
     * Copy a file or directory on remote device.
     *
     * Fails without touching anything when [destPath] already exists, so a copy never overwrites
     * an item or lands inside an existing folder by accident.
     */
    suspend fun copy(sourcePath: String, destPath: String): Result<Unit>

    /**
     * Move a file or directory on remote device, refusing an existing [destPath] like [copy].
     */
    suspend fun move(sourcePath: String, destPath: String): Result<Unit>

    /**
     * Reports what exists at each path, in the same order, using as few device round trips as
     * the command length limit allows.
     */
    suspend fun inspect(paths: List<String>): Result<List<PathInfo>>

    /** The physical location of the folder at [path], with every symlink along it resolved. */
    suspend fun resolveDirectory(path: String): Result<String>

    /** Removes the folder only if it is empty, failing otherwise. */
    suspend fun removeEmptyDirectory(path: String): Result<Unit>
}
