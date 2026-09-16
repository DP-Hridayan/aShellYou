package `in`.hridayan.ashell.shell.common.domain.usecase

import `in`.hridayan.ashell.shell.common.domain.model.InputContext
import `in`.hridayan.ashell.shell.common.domain.model.SuggestionType
import javax.inject.Inject

private const val ADB_TOKEN = "adb"
private const val ANDROID_NAMESPACE = "android"
private const val TOKEN_SEPARATOR = " "
private const val PACKAGE_SEPARATOR = "."

private val SHELL_SUBCOMMANDS = setOf("shell", "exec-out")

/**
 * Works out which kind of suggestion the current input is asking for.
 *
 * A line beginning with the `adb` token is answered with [SuggestionType.ADB], except after
 * `adb shell` or `adb exec-out`, where the remainder is an ordinary shell command and detection
 * delegates to itself so package and permission suggestions keep working. The adb check runs before
 * the dotted-token check, because an address such as `192.168.1.5` would otherwise look like a
 * package name.
 *
 * Delegation returns a context whose `fullText` is the remainder rather than the original line. Only
 * `suggestionType` and `filterPrefix` are read by callers, so this stays safe as long as that holds.
 */
class DetectSuggestionTypeUseCase @Inject constructor() {

    operator fun invoke(text: String): InputContext {
        if (text.isBlank()) return commandContext(text, currentToken = "", filterPrefix = "")
        return adbContextOrNull(text) ?: shellContext(text)
    }

    private fun shellContext(text: String): InputContext {
        val currentToken = text.split(TOKEN_SEPARATOR).lastOrNull().orEmpty()

        return when {
            currentToken.isBlank() || text.endsWith(TOKEN_SEPARATOR) ->
                commandContext(text, currentToken = "", filterPrefix = text.trim())

            !currentToken.contains(PACKAGE_SEPARATOR) ->
                commandContext(text, currentToken = currentToken, filterPrefix = text.trim())

            else -> InputContext(
                fullText = text,
                currentToken = currentToken,
                suggestionType = namespaceTypeOf(currentToken),
                filterPrefix = currentToken
            )
        }
    }

    private fun adbContextOrNull(text: String): InputContext? {
        val trimmed = text.trimStart()
        val tokens = trimmed.split(TOKEN_SEPARATOR)
        if (tokens.firstOrNull() != ADB_TOKEN) return null

        val subcommand = tokens.getOrNull(1)
        if (subcommand in SHELL_SUBCOMMANDS) {
            val remainder = trimmed.substringAfter(subcommand + TOKEN_SEPARATOR, "")
            if (remainder.isNotEmpty()) return invoke(remainder)
        }

        return InputContext(
            fullText = text,
            currentToken = tokens.last(),
            suggestionType = SuggestionType.ADB,
            filterPrefix = trimmed.trimEnd()
        )
    }

    private fun namespaceTypeOf(token: String): SuggestionType {
        val firstSegment = token.substringBefore(PACKAGE_SEPARATOR)
        return if (firstSegment.equals(ANDROID_NAMESPACE, ignoreCase = true)) {
            SuggestionType.PERMISSION
        } else {
            SuggestionType.PACKAGE
        }
    }

    private fun commandContext(
        fullText: String,
        currentToken: String,
        filterPrefix: String
    ) = InputContext(
        fullText = fullText,
        currentToken = currentToken,
        suggestionType = SuggestionType.COMMAND,
        filterPrefix = filterPrefix
    )
}
