package `in`.hridayan.ashell.shell.file_browser.domain.repository

import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperationResult
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
     * Pull (download) a file from remote device to local storage.
     * @param remotePath Path on remote device
     * @param localPath Path on local device to save to
     */
    fun pullFile(remotePath: String, localPath: String): Flow<FileOperationResult>
    
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
     */
    suspend fun copy(sourcePath: String, destPath: String): Result<Unit>
    
    /**
     * Move a file or directory on remote device.
     */
    suspend fun move(sourcePath: String, destPath: String): Result<Unit>
    
    /**
     * Check if a file or directory exists at the given path.
     */
    suspend fun exists(path: String): Result<Boolean>
    
    /**
     * Check if the path is a directory.
     */
    suspend fun isDirectory(path: String): Result<Boolean>
    
    /**
     * Delete a file or directory recursively.
     */
    suspend fun delete(path: String): Result<Unit>
}
