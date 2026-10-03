package `in`.hridayan.ashell.logcat.data.emitter

private const val LOGCAT = "logcat"
private const val FORMAT_FLAG = "-v"
private const val THREADTIME = "threadtime"
private const val SINCE_FLAG = "-T"
private val THREADTIME_TIMESTAMP = Regex("""\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d+""")

/**
 * The logcat invocation every emitter runs: threadtime output, optionally starting from a
 * timestamp instead of reading the device's whole buffer.
 *
 * The start time is spliced into a shell command line, so anything that is not exactly a
 * threadtime timestamp is ignored rather than escaped.
 */
internal object LogcatCommand {

    fun arguments(since: String?): List<String> {
        val start = validTimestamp(since) ?: return baseArguments()
        return baseArguments() + listOf(SINCE_FLAG, start)
    }

    fun shellLine(since: String?): String {
        val base = baseArguments().joinToString(" ")
        val start = validTimestamp(since) ?: return base
        return "$base $SINCE_FLAG '$start'"
    }

    private fun baseArguments(): List<String> = listOf(LOGCAT, FORMAT_FLAG, THREADTIME)

    private fun validTimestamp(since: String?): String? =
        since?.takeIf { THREADTIME_TIMESTAMP.matches(it) }
}
