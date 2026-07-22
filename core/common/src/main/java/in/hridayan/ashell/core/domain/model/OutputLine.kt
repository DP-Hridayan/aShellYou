package `in`.hridayan.ashell.core.domain.model

import androidx.compose.runtime.Immutable

/**
 * Represents a single line of output from a shell command.
 *
 * @property text The content of the output line.
 * @property isError Indicates whether this line originated from the error stream.
 */
@Immutable
data class OutputLine(
    val text: String,
    val isError: Boolean = false
)

