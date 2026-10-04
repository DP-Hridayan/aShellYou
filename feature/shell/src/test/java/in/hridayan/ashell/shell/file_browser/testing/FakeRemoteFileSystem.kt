package `in`.hridayan.ashell.shell.file_browser.testing

import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperationResult
import `in`.hridayan.ashell.shell.file_browser.domain.model.PathInfo
import `in`.hridayan.ashell.shell.file_browser.domain.model.PathKind
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile
import `in`.hridayan.ashell.shell.file_browser.domain.repository.FileBrowserRepository
import `in`.hridayan.ashell.shell.file_browser.domain.util.RemotePaths
import kotlinx.coroutines.flow.Flow
import java.io.IOException

private const val ROOT = "/"

/**
 * An in-memory device file system that behaves like the shell commands the real repository sends.
 *
 * Copy and move refuse an existing destination, as the guarded commands do. [aliases] stand in for
 * symlinked folders such as `/sdcard`, and [caseInsensitive] for shared storage, where `Photo.jpg`
 * and `photo.jpg` are the same file. [failWhen] injects a device failure for one operation.
 */
class FakeRemoteFileSystem(
    private val aliases: Map<String, String> = emptyMap(),
    private val caseInsensitive: Boolean = false
) : FileBrowserRepository {

    /** Receives the operation name, its path and, for copy and move, the destination. */
    var failWhen: (operation: String, path: String, destination: String?) -> Boolean =
        { _, _, _ -> false }

    /**
     * Entries keyed by [key]. Each keeps its path in canonical form with the original letter case,
     * and its content, which is null for a folder.
     */
    private val entries = linkedMapOf<String, Entry>()

    private data class Entry(val path: String, val content: String?)

    fun file(path: String, content: String = "") {
        createParents(path)
        entries[key(path)] = Entry(canonical(path), content)
    }

    fun dir(path: String) {
        createParents(path)
        entries[key(path)] = Entry(canonical(path), null)
    }

    fun contentOf(path: String): String? = entries[key(path)]?.content

    fun exists(path: String): Boolean = isRoot(path) || key(path) in entries

    fun isDirectoryAt(path: String): Boolean =
        isRoot(path) || entries[key(path)]?.let { it.content == null } == true

    fun childNames(path: String): List<String> = children(path).map { RemotePaths.fileName(it.path) }

    fun allPaths(): List<String> = entries.values.map { it.path }

    /** True when a replace left a staging or backup item behind. */
    fun hasTemporaryLeftovers(): Boolean =
        allPaths().any { RemotePaths.fileName(it).startsWith(".ashell-") }

    override suspend fun copy(sourcePath: String, destPath: String): Result<Unit> =
        transfer("copy", sourcePath, destPath, removeSource = false)

    override suspend fun move(sourcePath: String, destPath: String): Result<Unit> =
        transfer("move", sourcePath, destPath, removeSource = true)

    override suspend fun deleteFile(path: String): Result<Unit> {
        if (failWhen("delete", path, null)) return failure("injected delete failure")
        subtree(path).forEach { entries.remove(key(it.path)) }
        return Result.success(Unit)
    }

    override suspend fun removeEmptyDirectory(path: String): Result<Unit> {
        if (!isDirectoryAt(path)) return failure("Not a directory")
        if (children(path).isNotEmpty()) return failure("Directory not empty")
        entries.remove(key(path))
        return Result.success(Unit)
    }

    override suspend fun inspect(paths: List<String>): Result<List<PathInfo>> {
        if (paths.any { failWhen("inspect", it, null) }) return failure("injected inspect failure")
        return Result.success(paths.map(::infoOf))
    }

    override suspend fun resolveDirectory(path: String): Result<String> =
        if (isDirectoryAt(path)) Result.success(canonical(path)) else failure("No such directory")

    override suspend fun listFiles(path: String): Result<List<RemoteFile>> {
        if (failWhen("list", path, null)) return failure("injected list failure")
        if (!isDirectoryAt(path)) return failure("No such directory")

        val parent = RemoteFile(name = "..", path = parentOf(path), isDirectory = true)
        val listed = children(path).map { entry ->
            val name = RemotePaths.fileName(entry.path)
            RemoteFile(name, RemotePaths.childPath(path, name), isDirectory = entry.content == null)
        }
        return Result.success(listOf(parent) + listed)
    }

    override fun pullFile(remotePath: String, fileName: String): Flow<FileOperationResult> =
        error("not used by paste")

    override fun pushFile(localPath: String, remotePath: String): Flow<FileOperationResult> =
        error("not used by paste")

    override suspend fun createDirectory(path: String): Result<Unit> = error("not used by paste")

    override suspend fun rename(oldPath: String, newPath: String): Result<Unit> =
        error("not used by paste")

    override suspend fun getFileInfo(path: String): Result<RemoteFile> = error("not used by paste")

    override fun isAdbConnected(): Boolean = true

    private fun transfer(
        operation: String,
        source: String,
        destination: String,
        removeSource: Boolean
    ): Result<Unit> {
        val refusal = when {
            failWhen(operation, source, destination) -> "injected $operation failure"
            !exists(source) -> "No such file or directory"
            exists(destination) -> "File exists"
            !isDirectoryAt(parentOf(destination)) -> "No such directory"
            isDirectoryAt(source) && RemotePaths.isSameOrInside(key(destination), key(source)) ->
                "Invalid argument"

            else -> null
        }
        if (refusal != null) return failure(refusal)

        val sourceEntries = subtree(source)
        val sourceRoot = entries.getValue(key(source)).path
        val destinationRoot = canonical(destination)

        if (removeSource) sourceEntries.forEach { entries.remove(key(it.path)) }
        sourceEntries.forEach { entry ->
            val newPath = destinationRoot + entry.path.substring(sourceRoot.length)
            entries[key(newPath)] = Entry(newPath, entry.content)
        }
        return Result.success(Unit)
    }

    private fun infoOf(path: String): PathInfo {
        val kind = when {
            !exists(path) -> PathKind.MISSING
            isDirectoryAt(path) -> PathKind.DIRECTORY
            else -> PathKind.FILE
        }
        val parent = parentOf(path)
        val realPath = if (isDirectoryAt(parent)) {
            RemotePaths.childPath(canonical(parent), RemotePaths.fileName(path))
        } else {
            null
        }
        return PathInfo(kind, realPath)
    }

    private fun children(path: String): List<Entry> {
        val parentKey = key(path)
        return entries.values.filter { key(parentOf(it.path)) == parentKey }
    }

    private fun subtree(path: String): List<Entry> {
        val rootKey = key(path)
        return entries.values.filter { RemotePaths.isSameOrInside(key(it.path), rootKey) }
    }

    private fun createParents(path: String) {
        var parent = parentOf(path)
        val missing = mutableListOf<String>()
        while (!isRoot(parent) && key(parent) !in entries) {
            missing += parent
            parent = parentOf(parent)
        }
        missing.asReversed().forEach { entries[key(it)] = Entry(canonical(it), null) }
    }

    private fun canonical(path: String): String {
        val trimmed = path.trimEnd('/').ifEmpty { ROOT }
        val alias = aliases.keys.firstOrNull { RemotePaths.isSameOrInside(trimmed, it) }
            ?: return trimmed
        return aliases.getValue(alias) + trimmed.removePrefix(alias)
    }

    private fun key(path: String): String =
        canonical(path).let { if (caseInsensitive) it.lowercase() else it }

    private fun parentOf(path: String): String =
        path.trimEnd('/').substringBeforeLast('/').ifEmpty { ROOT }

    private fun isRoot(path: String): Boolean = canonical(path) == ROOT

    private fun <T> failure(message: String): Result<T> = Result.failure(IOException(message))
}
