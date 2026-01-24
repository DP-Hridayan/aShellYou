package `in`.hridayan.ashell.shell.common.domain.model

/**
 * Represents a single line of output from a shell command.
 *
 * @property text The content of the output line.
 * @property isError Indicates whether this line originated from the error stream.
 */
data class OutputLine(
    val text: String,
    val isError: Boolean = false
)
