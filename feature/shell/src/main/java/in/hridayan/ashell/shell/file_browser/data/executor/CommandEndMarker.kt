package `in`.hridayan.ashell.shell.file_browser.data.executor

import java.util.UUID

/**
 * A token appended to a shell command so a reader can stop when the device echoes it, instead of
 * waiting for the stream to close.
 *
 * The token is random per command because a fixed one can occur in a file name and truncate the
 * output it was meant to terminate.
 */
internal object CommandEndMarker {

    fun next(): String = "__ASHELL_${UUID.randomUUID().toString().replace("-", "")}__"

    fun appendTo(command: String, marker: String): String = "$command; echo '$marker'"
}
