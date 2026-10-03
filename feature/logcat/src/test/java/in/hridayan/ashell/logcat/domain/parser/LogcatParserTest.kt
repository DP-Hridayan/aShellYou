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
