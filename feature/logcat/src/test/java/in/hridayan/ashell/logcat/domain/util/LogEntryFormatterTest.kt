package `in`.hridayan.ashell.logcat.domain.util

import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import `in`.hridayan.ashell.logcat.domain.parser.LogcatParser
import org.junit.Assert.assertEquals
import org.junit.Test

private fun entry(
    id: Long = 1,
    uid: String = "",
    pid: String = "4211",
    tid: String = "4230",
    level: LogLevel = LogLevel.ERROR,
    tag: String = "AndroidRuntime",
    message: String = "FATAL EXCEPTION: main",
) = LogEntry(
    id = id,
    timestamp = "10-17 14:02:11.532",
    pid = pid,
    tid = tid,
    uid = uid,
    packageName = "com.example",
    level = level,
    tag = tag,
    message = message,
)

class LogEntryFormatterTest {

    @Test
    fun `an entry without a uid is written in the threadtime layout`() {
        assertEquals(
            "10-17 14:02:11.532  4211  4230 E AndroidRuntime: FATAL EXCEPTION: main",
            entry().toLogcatLine(),
        )
    }

    @Test
    fun `an entry with a uid gets the uid column before the pid`() {
        assertEquals(
            "10-17 14:02:11.532 10123  4211  4230 E AndroidRuntime: FATAL EXCEPTION: main",
            entry(uid = "10123").toLogcatLine(),
        )
    }

    @Test
    fun `short ids are padded to five characters`() {
        assertEquals(
            "10-17 14:02:11.532     0     1     2 I init: started",
            entry(uid = "0", pid = "1", tid = "2", level = LogLevel.INFO, tag = "init", message = "started")
                .toLogcatLine(),
        )
    }

    @Test
    fun `a written line parses back to the same entry`() {
        val original = entry(uid = "10123", level = LogLevel.WARNING, message = "a: b : c")
        val parsed = LogcatParser.parse(original.toLogcatLine(), original.id)
        assertEquals(original.copy(packageName = ""), parsed)
    }

    @Test
    fun `several entries are joined one per line`() {
        val text = listOf(entry(id = 1, message = "first"), entry(id = 2, message = "second")).toLogcatText()
        assertEquals(
            "10-17 14:02:11.532  4211  4230 E AndroidRuntime: first\n" +
                "10-17 14:02:11.532  4211  4230 E AndroidRuntime: second",
            text,
        )
    }

    @Test
    fun `no entries give empty text`() {
        assertEquals("", emptyList<LogEntry>().toLogcatText())
    }
}
