package `in`.hridayan.ashell.logcat.data.emitter

private const val LOGCAT = "logcat"
private const val FORMAT_FLAG = "-v"
private const val THREADTIME = "threadtime"
private const val UID_MODIFIER = "uid"
private const val SINCE_FLAG = "-T"
private val THREADTIME_TIMESTAMP = Regex("""\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d+""")

/**
 * The logcat invocation every emitter runs: threadtime output, optionally with the UID of each
 * line and optionally starting from a timestamp instead of reading the device's whole buffer.
 *
 * The UID is what package filters match on, since logcat lines do not name the app. The start
 * time is spliced into a shell command line, so anything that is not exactly a threadtime
 * timestamp is ignored rather than escaped.
 */
internal object LogcatCommand {

    fun arguments(since: String?, withUid: Boolean): List<String> {
        val base = baseArguments(withUid)
        val start = validTimestamp(since) ?: return base
        return base + listOf(SINCE_FLAG, start)
    }

    fun shellLine(since: String?, withUid: Boolean): String {
        val base = baseArguments(withUid).joinToString(" ")
        val start = validTimestamp(since) ?: return base
        return "$base $SINCE_FLAG '$start'"
    }

    private fun baseArguments(withUid: Boolean): List<String> {
        val format = listOf(LOGCAT, FORMAT_FLAG, THREADTIME)
        return if (withUid) format + listOf(FORMAT_FLAG, UID_MODIFIER) else format
    }

    private fun validTimestamp(since: String?): String? =
        since?.takeIf { THREADTIME_TIMESTAMP.matches(it) }
}
