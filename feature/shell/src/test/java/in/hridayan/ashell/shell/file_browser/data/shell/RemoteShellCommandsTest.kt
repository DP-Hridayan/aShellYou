package `in`.hridayan.ashell.shell.file_browser.data.shell

import `in`.hridayan.ashell.shell.file_browser.domain.model.PathInfo
import `in`.hridayan.ashell.shell.file_browser.domain.model.PathKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteShellCommandsTest {

    @Test
    fun `quoting escapes an apostrophe by closing and reopening the quote`() {
        assertEquals("'/sdcard/Mom'\\''s photos'", "/sdcard/Mom's photos".shellQuoted())
    }

    @Test
    fun `quoting leaves every other character literal`() {
        assertEquals("'\$(reboot) `id` ; * #'", "\$(reboot) `id` ; * #".shellQuoted())
    }

    @Test
    fun `a status line is split from the output`() {
        val status = RemoteShellCommands.parseStatus("cp: bad '/x': No such file\n__ASHELL_STATUS=1\n")

        assertEquals(CommandStatus(1, "cp: bad '/x': No such file"), status)
    }

    @Test
    fun `a status line with carriage returns from a terminal still parses`() {
        val status = RemoteShellCommands.parseStatus("done\r\n__ASHELL_STATUS=0\r\n")

        assertEquals(CommandStatus(0, "done"), status)
    }

    @Test
    fun `the last status line wins over one printed by a file name`() {
        val status = RemoteShellCommands.parseStatus("__ASHELL_STATUS=0 in a name\n__ASHELL_STATUS=2\n")

        assertEquals(2, status?.exitCode)
    }

    @Test
    fun `missing output or a missing status means the command did not finish`() {
        assertNull(RemoteShellCommands.parseStatus(null))
        assertNull(RemoteShellCommands.parseStatus("partial output"))
        assertNull(RemoteShellCommands.parseStatus("__ASHELL_STATUS="))
    }

    @Test
    fun `inspection lines map to kinds and real paths in order`() {
        val infos = RemoteShellCommands.parseInspection(
            "D/storage/emulated/0\nF/storage/emulated/0/Docs\nN\n",
            listOf("/sdcard/Docs", "/sdcard/Docs/a.txt", "/missing/b.txt")
        )

        assertEquals(
            listOf(
                PathInfo(PathKind.DIRECTORY, "/storage/emulated/0/Docs"),
                PathInfo(PathKind.FILE, "/storage/emulated/0/Docs/a.txt"),
                PathInfo(PathKind.MISSING, null)
            ),
            infos
        )
    }

    @Test
    fun `an item in the root folder gets a single slash`() {
        val infos = RemoteShellCommands.parseInspection("D/\n", listOf("/data"))

        assertEquals(PathInfo(PathKind.DIRECTORY, "/data"), infos?.single())
    }

    @Test
    fun `inspection output that does not match the request is rejected`() {
        assertNull(RemoteShellCommands.parseInspection("D/\n", listOf("/a", "/b")))
        assertNull(RemoteShellCommands.parseInspection("X/\n", listOf("/a")))
    }

    @Test
    fun `long inspections are split into commands of bounded length`() {
        val paths = (1..400).map { "/storage/emulated/0/some/fairly/long/folder/name/file_$it.txt" }

        val batches = RemoteShellCommands.inspect(paths)

        assertTrue(batches.size > 1)
        assertEquals(paths, batches.flatMap { it.first })
        batches.forEach { (_, command) -> assertTrue(command.length < 3200) }
    }

    @Test
    fun `an empty inspection sends nothing`() {
        assertTrue(RemoteShellCommands.inspect(emptyList()).isEmpty())
    }
}
