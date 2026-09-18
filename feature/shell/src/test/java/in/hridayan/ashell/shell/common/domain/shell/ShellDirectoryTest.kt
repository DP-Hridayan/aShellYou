package `in`.hridayan.ashell.shell.common.domain.shell

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellDirectoryTest {

    @Test
    fun `an absolute target is used as it stands`() {
        assertEquals("/data/local/tmp", ShellDirectory.candidate("/sdcard", "/data/local/tmp"))
    }

    @Test
    fun `a relative target hangs off the current directory`() {
        assertEquals("/sdcard/Download", ShellDirectory.candidate("/sdcard", "Download"))
    }

    @Test
    fun `a trailing slash on the current directory is not doubled`() {
        assertEquals("/sdcard/Download", ShellDirectory.candidate("/sdcard/", "Download"))
    }

    @Test
    fun `dot dot is left for the device to resolve`() {
        assertEquals("/sdcard/a/..", ShellDirectory.candidate("/sdcard/a", ".."))
    }

    @Test
    fun `a multi segment relative target is left for the device`() {
        assertEquals("/sdcard/../tmp/x", ShellDirectory.candidate("/sdcard", "../tmp/x"))
    }

    @Test
    fun `a bare cd goes to the root`() {
        assertEquals("/", ShellDirectory.candidate("/sdcard", ""))
    }

    @Test
    fun `the home shorthand goes to the root`() {
        assertEquals("/", ShellDirectory.candidate("/sdcard", "~"))
    }

    @Test
    fun `the probe does not start from the current directory`() {
        val probe = ShellDirectory.probeCommand("/sdcard/x")
        assertTrue(probe.startsWith("cd '/sdcard/x'"))
    }

    @Test
    fun `a quote in the path is escaped`() {
        val probe = ShellDirectory.probeCommand("/sdcard/it's")
        assertTrue(probe.contains("""/sdcard/it'\''s"""))
    }

    @Test
    fun `a successful probe yields the path the device reported`() {
        val result = ShellDirectory.interpret("__ASHELL_CD__/storage/emulated/0/Download\n")
        assertEquals(DirectoryResult.Moved("/storage/emulated/0/Download"), result)
    }

    @Test
    fun `the device answer wins over the candidate, so symlinks resolve`() {
        val result = ShellDirectory.interpret("__ASHELL_CD__/storage/emulated/0\n")
        assertEquals(DirectoryResult.Moved("/storage/emulated/0"), result)
    }

    @Test
    fun `a failing probe carries the device message`() {
        val message = "sh: cd: /nope: No such file or directory"
        assertEquals(DirectoryResult.Rejected(message), ShellDirectory.interpret("$message\n"))
    }

    @Test
    fun `no output at all is a rejection`() {
        assertEquals(DirectoryResult.Rejected(null), ShellDirectory.interpret(null))
        assertEquals(DirectoryResult.Rejected(null), ShellDirectory.interpret("   "))
    }

    @Test
    fun `a probe that reports the marker with no path falls back to the root`() {
        assertEquals(DirectoryResult.Moved("/"), ShellDirectory.interpret("__ASHELL_CD__"))
    }

    @Test
    fun `a cd command yields its target`() {
        assertEquals("/data", ShellDirectory.targetOf("cd /data"))
        assertEquals("", ShellDirectory.targetOf("cd"))
        assertEquals("..", ShellDirectory.targetOf("  cd ..  "))
    }

    @Test
    fun `a command that is not a cd yields nothing`() {
        assertNull(ShellDirectory.targetOf("ls"))
        assertNull(ShellDirectory.targetOf("cdrom"))
        assertNull(ShellDirectory.targetOf("echo cd /data"))
    }

    @Test
    fun `a compound cd separates target from remainder`() {
        assertEquals("/data", ShellDirectory.targetOf("cd /data && ls -la"))
        assertEquals("ls -la", ShellDirectory.remainderOf("cd /data && ls -la"))
    }

    @Test
    fun `a semicolon compound separates too`() {
        assertEquals("/data", ShellDirectory.targetOf("cd /data; ls"))
        assertEquals("ls", ShellDirectory.remainderOf("cd /data; ls"))
    }

    @Test
    fun `a plain cd has no remainder`() {
        assertNull(ShellDirectory.remainderOf("cd /data"))
    }
}
