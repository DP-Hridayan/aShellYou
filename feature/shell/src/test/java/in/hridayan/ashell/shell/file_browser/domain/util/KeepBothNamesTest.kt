package `in`.hridayan.ashell.shell.file_browser.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class KeepBothNamesTest {

    private fun firstTwo(name: String, isDirectory: Boolean = false): List<String> =
        KeepBothNames.candidates(name, isDirectory).take(2).toList()

    @Test
    fun `a file gets the number before its extension`() {
        assertEquals(listOf("photo (1).jpg", "photo (2).jpg"), firstTwo("photo.jpg"))
    }

    @Test
    fun `a file without an extension gets the number at the end`() {
        assertEquals(listOf("Makefile (1)", "Makefile (2)"), firstTwo("Makefile"))
    }

    @Test
    fun `a dot file keeps its whole name`() {
        assertEquals(listOf(".nomedia (1)", ".nomedia (2)"), firstTwo(".nomedia"))
    }

    @Test
    fun `a dot file with an extension numbers the part before it`() {
        assertEquals(listOf(".config (1).json", ".config (2).json"), firstTwo(".config.json"))
    }

    @Test
    fun `a folder with a dot in its name is numbered as a whole`() {
        assertEquals(listOf("my.folder (1)", "my.folder (2)"), firstTwo("my.folder", isDirectory = true))
    }

    @Test
    fun `a tar archive keeps its double extension`() {
        assertEquals(listOf("backup (1).tar.gz", "backup (2).tar.gz"), firstTwo("backup.tar.gz"))
    }

    @Test
    fun `a name that already has a number continues from it`() {
        assertEquals(listOf("photo (2).jpg", "photo (3).jpg"), firstTwo("photo (1).jpg"))
    }

    @Test
    fun `candidates never repeat`() {
        val candidates = KeepBothNames.candidates("a.txt", isDirectory = false).take(500).toList()
        assertEquals(candidates.size, candidates.toSet().size)
    }
}
