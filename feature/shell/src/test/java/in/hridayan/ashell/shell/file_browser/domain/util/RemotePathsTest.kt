package `in`.hridayan.ashell.shell.file_browser.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemotePathsTest {

    @Test
    fun `a child of the root folder has a single slash`() {
        assertEquals("/a.txt", RemotePaths.childPath("/", "a.txt"))
    }

    @Test
    fun `a trailing slash on the folder is ignored`() {
        assertEquals("/sdcard/a.txt", RemotePaths.childPath("/sdcard/", "a.txt"))
    }

    @Test
    fun `the file name is the last component`() {
        assertEquals("a.txt", RemotePaths.fileName("/sdcard/Docs/a.txt"))
        assertEquals("Docs", RemotePaths.fileName("/sdcard/Docs/"))
    }

    @Test
    fun `a path is inside itself and its descendants but not a sibling with the same prefix`() {
        assertTrue(RemotePaths.isSameOrInside("/sdcard/A", "/sdcard/A"))
        assertTrue(RemotePaths.isSameOrInside("/sdcard/A/b/c", "/sdcard/A"))
        assertFalse(RemotePaths.isSameOrInside("/sdcard/AB", "/sdcard/A"))
        assertFalse(RemotePaths.isSameOrInside("/sdcard", "/sdcard/A"))
    }
}
