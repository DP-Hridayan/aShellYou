package `in`.hridayan.ashell.logcat.domain.parser

import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LogcatParserTest {

    @Test
    fun `plain threadtime lines still parse, with no uid`() {
        val entry = LogcatParser.parse("07-19 20:12:17.345  1234  5678 D MyTag  : Hello world", 1)
        assertNotNull(entry)
        assertEquals("1234", entry!!.pid)
        assertEquals("5678", entry.tid)
        assertEquals("", entry.uid)
        assertEquals(LogLevel.DEBUG, entry.level)
        assertEquals("MyTag", entry.tag)
        assertEquals("Hello world", entry.message)
    }

    @Test
    fun `the current uid column, without a colon, is read`() {
        val entry = LogcatParser.parse(
            "10-04 14:05:10.604 10282  7341  7476 W fb4a.MultiSignalANRDetectorLacrima: Pausing error state checks",
            1,
        )
        assertEquals("10282", entry!!.uid)
        assertEquals("7341", entry.pid)
        assertEquals("7476", entry.tid)
        assertEquals(LogLevel.WARNING, entry.level)
        assertEquals("fb4a.MultiSignalANRDetectorLacrima", entry.tag)
        assertEquals("Pausing error state checks", entry.message)
    }

    @Test
    fun `a named uid without a colon is read`() {
        val root = LogcatParser.parse("10-04 14:05:04.756  root     0     0 I servicemanager: Caller(pid=7341)", 1)
        assertEquals("0", root!!.uid)
        assertEquals("0", root.pid)
        assertEquals("servicemanager", root.tag)
        assertEquals("1069", LogcatParser.parse("10-04 14:05:04.756  lmkd   812   812 I lowmemorykiller: x", 1)!!.uid)
        assertEquals("u0_i1", LogcatParser.parse("10-04 14:05:04.756 u0_i1  9001  9001 I Isolated: x", 1)!!.uid)
    }

    @Test
    fun `a five digit pid after the older colon form is read`() {
        val entry = LogcatParser.parse("10-03 10:00:00.123 10077:12345  5678 I Tag: m", 1)
        assertEquals("10077", entry!!.uid)
        assertEquals("12345", entry.pid)
    }

    @Test
    fun `a plain line is never mistaken for one with a uid`() {
        val entry = LogcatParser.parse("10-04 14:05:38.801  1950  3059 D InputManagerService: transferTouchGesture", 1)
        assertEquals("", entry!!.uid)
        assertEquals("1950", entry.pid)
        assertEquals("3059", entry.tid)
        assertEquals(LogLevel.DEBUG, entry.level)
    }

    @Test
    fun `a numeric uid column is read`() {
        val entry = LogcatParser.parse("10-03 10:00:00.123 10077:  1234  5678 I ActivityManager: Start", 1)
        assertEquals("10077", entry!!.uid)
        assertEquals("1234", entry.pid)
        assertEquals("ActivityManager", entry.tag)
        assertEquals("Start", entry.message)
    }

    @Test
    fun `a short system name is converted to its uid`() {
        assertEquals("2000", LogcatParser.parse("10-03 10:00:00.123 shell:  1234  5678 I Tag: m", 1)!!.uid)
        assertEquals("0", LogcatParser.parse("10-03 10:00:00.123  root:     1     1 I init: m", 1)!!.uid)
        assertEquals("1001", LogcatParser.parse("10-03 10:00:00.123 radio:  1234  5678 I Tag: m", 1)!!.uid)
    }

    @Test
    fun `a short app user name is converted to its uid`() {
        assertEquals("10003", LogcatParser.parse("10-03 10:00:00.123 u0_a3:  1234  5678 I Tag: m", 1)!!.uid)
        assertEquals("1010005", LogcatParser.parse("10-03 10:00:00.123 u10_a5:  1234  5678 I Tag: m", 1)!!.uid)
    }

    @Test
    fun `an unknown name is kept as printed`() {
        assertEquals("oem", LogcatParser.parse("10-03 10:00:00.123   oem:  1234  5678 I Tag: m", 1)!!.uid)
    }

    @Test
    fun `a colon inside the message is kept`() {
        val entry = LogcatParser.parse("10-03 10:00:00.123 10077:  1234  5678 W Net: host: example.com", 1)
        assertEquals("Net", entry!!.tag)
        assertEquals("host: example.com", entry.message)
    }

    @Test
    fun `lines that are not log entries are rejected`() {
        assertNull(LogcatParser.parse("--------- beginning of main", 1))
        assertNull(LogcatParser.parse("No OTG ADB connection", 1))
    }
}
