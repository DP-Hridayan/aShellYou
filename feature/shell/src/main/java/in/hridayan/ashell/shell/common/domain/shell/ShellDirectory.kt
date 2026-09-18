package `in`.hridayan.ashell.shell.common.domain.shell

private const val ROOT = "/"
private const val HOME_SHORTHAND = "~"
private const val SUCCESS_MARKER = "__ASHELL_CD__"
private const val QUOTE = "'"
private const val ESCAPED_QUOTE = "'\\''"

/**
 * The outcome of asking the device to change directory.
 */
sealed interface DirectoryResult {

    /** @property path the device's own answer, so symlinks and `..` are already resolved. */
    data class Moved(val path: String) : DirectoryResult

    /** @property reason the device's message, or null when it said nothing useful. */
    data class Rejected(val reason: String?) : DirectoryResult
}

/**
 * Builds and reads back the probe that decides whether a `cd` is allowed.
 *
 * The app used to rewrite its idea of the current directory from the command text alone and report
 * success, so `cd` into a directory that does not exist looked fine and every command afterwards
 * failed for an invisible reason. Asking the device removes the guesswork, and taking [ROOT] relative
 * work with it removes the hand-rolled path arithmetic: `..`, `.`, multiple segments and symlinks are
 * all resolved by the device, which already knows how.
 *
 * The probe deliberately does **not** start from the current directory. If that directory has been
 * deleted underneath the user, a probe that began by entering it would fail for every target, leaving
 * no way out.
 */
object ShellDirectory {

    /**
     * The absolute path to try, built without touching the device so a deleted current directory
     * cannot block it. Anything not absolute is simply hung off the current directory; the device
     * resolves the rest.
     */
    fun candidate(currentDir: String, target: String): String = when {
        target.isEmpty() || target == HOME_SHORTHAND -> ROOT
        target.startsWith(ROOT) -> target
        else -> currentDir.trimEnd('/').ifEmpty { "" } + ROOT + target
    }

    fun probeCommand(candidate: String): String =
        "cd '${candidate.escapeForSingleQuotes()}' 2>&1 && printf '%s' '$SUCCESS_MARKER' && pwd"

    fun interpret(output: String?): DirectoryResult {
        if (output == null) return DirectoryResult.Rejected(null)
        if (!output.contains(SUCCESS_MARKER)) {
            return DirectoryResult.Rejected(output.trim().ifEmpty { null })
        }
        val path = output.substringAfter(SUCCESS_MARKER).trim()
        return DirectoryResult.Moved(path.ifEmpty { ROOT })
    }

    /**
     * The directory argument of a `cd`, or null when the command is not one.
     *
     * A compound such as `cd /data && ls` yields `/data` here and its remainder from [remainderOf],
     * so the rest only runs once the directory is known to exist.
     */
    fun targetOf(command: String): String? {
        val trimmed = command.trim()
        if (trimmed != "cd" && !trimmed.startsWith("cd ")) return null
        return trimmed.cdPart().split(WHITESPACE, limit = 2).getOrElse(1) { "" }.trim()
    }

    fun remainderOf(command: String): String? {
        val trimmed = command.trim()
        val separator = trimmed.separatorIndex()
        if (separator < 0) return null
        return trimmed.substring(separator).trimStart(' ', '&', ';').trim().ifEmpty { null }
    }

    private fun String.cdPart(): String {
        val separator = separatorIndex()
        return if (separator < 0) this else take(separator).trim()
    }

    private fun String.separatorIndex(): Int {
        val and = indexOf(" && ")
        val semicolon = indexOf("; ")
        return when {
            and >= 0 && semicolon >= 0 -> minOf(and, semicolon)
            and >= 0 -> and
            else -> semicolon
        }
    }

    private fun String.escapeForSingleQuotes(): String = replace(QUOTE, ESCAPED_QUOTE)
}

private val WHITESPACE = "\\s+".toRegex()
