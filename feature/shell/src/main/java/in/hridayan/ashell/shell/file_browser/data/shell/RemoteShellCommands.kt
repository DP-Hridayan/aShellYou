package `in`.hridayan.ashell.shell.file_browser.data.shell

import `in`.hridayan.ashell.shell.file_browser.domain.model.PathInfo
import `in`.hridayan.ashell.shell.file_browser.domain.model.PathKind
import `in`.hridayan.ashell.shell.file_browser.domain.util.RemotePaths

private const val STATUS_MARKER = "__ASHELL_STATUS="
private const val MAX_BATCH_COMMAND_LENGTH = 3000
private const val INSPECT_CALL_OVERHEAD = "_i ; ".length
private const val KIND_DIRECTORY = 'D'
private const val KIND_FILE = 'F'
private const val KIND_MISSING = 'N'

/**
 * Prints one line per path: a kind letter followed by the physical location of the path's parent
 * folder, which is empty when the parent cannot be entered.
 */
private const val INSPECT_FUNCTION = "_i(){ " +
        "if [ -d \"\$1\" ]; then k=$KIND_DIRECTORY; " +
        "elif [ -e \"\$1\" ] || [ -L \"\$1\" ]; then k=$KIND_FILE; " +
        "else k=$KIND_MISSING; fi; " +
        "d=\$(cd \"\${1%/*}/\" 2>/dev/null && pwd -P); " +
        "printf '%s%s\\n' \"\$k\" \"\$d\"; }"

/**
 * Single quotes a value for the device shell. A quote inside the value closes the quoting, adds an
 * escaped quote and reopens it, so no file name can end up running as shell syntax.
 */
internal fun String.shellQuoted(): String = "'" + replace("'", "'\\''") + "'"

/** A command's exit status and everything it printed, with stderr folded into stdout. */
internal data class CommandStatus(
    val exitCode: Int,
    val output: String
)

/**
 * Builds the shell commands the file browser sends to the device and reads their output.
 *
 * Commands that change files are wrapped by [withStatus], because the shell transport only returns
 * text, and a command that failed without a recognisable message would otherwise look successful.
 */
internal object RemoteShellCommands {

    /** Exit status of [copy] and [move] when the destination already exists, matching EEXIST. */
    const val DESTINATION_EXISTS_STATUS = 17

    fun withStatus(command: String): String = "{ $command; } 2>&1; echo \"$STATUS_MARKER\$?\""

    /** Refuses an existing destination, where `cp -r` would nest the source inside a folder. */
    fun copy(source: String, destination: String): String =
        guardedTransfer("cp -r", source, destination)

    /** Refuses an existing destination, where `mv` would nest the source or overwrite a file. */
    fun move(source: String, destination: String): String =
        guardedTransfer("mv", source, destination)

    fun delete(path: String): String = withStatus("rm -rf ${path.shellQuoted()}")

    fun removeEmptyDirectory(path: String): String = withStatus("rmdir ${path.shellQuoted()}")

    fun createDirectory(path: String): String = withStatus("mkdir -p ${path.shellQuoted()}")

    fun rename(oldPath: String, newPath: String): String =
        withStatus("mv ${oldPath.shellQuoted()} ${newPath.shellQuoted()}")

    fun resolveDirectory(path: String): String = withStatus("cd ${path.shellQuoted()} && pwd -P")

    /**
     * Splits [paths] into commands short enough for the oldest ADB versions, whose service name is
     * limited to a few kilobytes. Each command answers for its batch in order; see [parseInspection].
     */
    fun inspect(paths: List<String>): List<Pair<List<String>, String>> =
        batchByLength(paths).map { batch ->
            val calls = batch.joinToString("; ") { "_i ${it.shellQuoted()}" }
            batch to withStatus("$INSPECT_FUNCTION; $calls")
        }

    /** @return null when the output carries no status, which means the command did not finish. */
    fun parseStatus(rawOutput: String?): CommandStatus? {
        if (rawOutput == null) return null

        val markerIndex = rawOutput.lastIndexOf(STATUS_MARKER)
        if (markerIndex < 0) return null

        val exitCode = rawOutput.substring(markerIndex + STATUS_MARKER.length)
            .trim()
            .takeWhile { it.isDigit() }
            .toIntOrNull()
            ?: return null

        return CommandStatus(exitCode, rawOutput.substring(0, markerIndex).trimEnd('\n', '\r'))
    }

    /** @return null when the output does not hold exactly one valid line per path. */
    fun parseInspection(output: String, paths: List<String>): List<PathInfo>? {
        val lines = output.split('\n').map { it.trimEnd('\r') }.filter { it.isNotEmpty() }
        if (lines.size != paths.size) return null

        return lines.zip(paths).map { (line, path) -> parseInspectionLine(line, path) ?: return null }
    }

    private fun parseInspectionLine(line: String, path: String): PathInfo? {
        val kind = when (line.first()) {
            KIND_DIRECTORY -> PathKind.DIRECTORY
            KIND_FILE -> PathKind.FILE
            KIND_MISSING -> PathKind.MISSING
            else -> return null
        }
        val realParent = line.substring(1)
        val realPath = realParent.takeIf { it.isNotEmpty() }
            ?.let { RemotePaths.childPath(it, RemotePaths.fileName(path)) }

        return PathInfo(kind, realPath)
    }

    private fun guardedTransfer(tool: String, source: String, destination: String): String {
        val quotedDestination = destination.shellQuoted()
        return withStatus(
            "if [ -e $quotedDestination ] || [ -L $quotedDestination ]; " +
                    "then (exit $DESTINATION_EXISTS_STATUS); " +
                    "else $tool ${source.shellQuoted()} $quotedDestination; fi"
        )
    }

    private fun batchByLength(paths: List<String>): List<List<String>> {
        val batches = mutableListOf<MutableList<String>>()
        var batchLength = 0

        paths.forEach { path ->
            val callLength = path.shellQuoted().length + INSPECT_CALL_OVERHEAD
            if (batches.isEmpty() || batchLength + callLength > MAX_BATCH_COMMAND_LENGTH) {
                batches += mutableListOf<String>()
                batchLength = INSPECT_FUNCTION.length
            }
            batches.last() += path
            batchLength += callLength
        }

        return batches
    }
}
