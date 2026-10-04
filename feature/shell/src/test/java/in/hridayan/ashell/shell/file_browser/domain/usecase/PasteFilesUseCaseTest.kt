package `in`.hridayan.ashell.shell.file_browser.domain.usecase

import `in`.hridayan.ashell.shell.file_browser.domain.model.ConflictDecision
import `in`.hridayan.ashell.shell.file_browser.domain.model.ConflictResolution
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileConflict
import `in`.hridayan.ashell.shell.file_browser.domain.model.OperationType
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteFailureReason
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteProgress
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteRequest
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteSummary
import `in`.hridayan.ashell.shell.file_browser.testing.FakeRemoteFileSystem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

private const val STORAGE = "/storage/emulated/0"
private const val SDCARD = "/sdcard"

class PasteFilesUseCaseTest {

    private val fs = FakeRemoteFileSystem(aliases = mapOf(SDCARD to STORAGE))
    private val pasteFiles = PasteFilesUseCase(fs)
    private val askedConflicts = mutableListOf<FileConflict>()

    private suspend fun paste(
        sources: List<String>,
        destination: String,
        type: OperationType = OperationType.COPY,
        isCancelled: () -> Boolean = { false },
        onProgress: (PasteProgress) -> Unit = {},
        answer: (FileConflict) -> ConflictDecision? = { fail("no conflict expected: $it"); null }
    ): PasteSummary = pasteFiles(
        request = PasteRequest(sources, destination, type),
        resolveConflict = { conflict ->
            askedConflicts += conflict
            answer(conflict)
        },
        isCancelled = isCancelled,
        onProgress = onProgress
    )

    private fun always(resolution: ConflictResolution, applyToAll: Boolean = false) =
        { _: FileConflict -> ConflictDecision(resolution, applyToAll) }

    @Test
    fun `copying into the source's own folder duplicates the item instead of deleting it`() =
        runTest {
            fs.file("$SDCARD/Docs/a.txt", "original")

            val summary = paste(listOf("$SDCARD/Docs/a.txt"), "$SDCARD/Docs")

            assertEquals("original", fs.contentOf("$SDCARD/Docs/a.txt"))
            assertEquals("original", fs.contentOf("$SDCARD/Docs/a (1).txt"))
            assertEquals(1, summary.completedCount)
            assertTrue(askedConflicts.isEmpty())
        }

    @Test
    fun `moving into the source's own folder leaves the item where it is`() = runTest {
        fs.file("$SDCARD/Docs/a.txt", "original")

        val summary = paste(listOf("$SDCARD/Docs/a.txt"), "$SDCARD/Docs", OperationType.MOVE)

        assertEquals("original", fs.contentOf("$SDCARD/Docs/a.txt"))
        assertEquals(listOf("a.txt"), fs.childNames("$SDCARD/Docs"))
        assertEquals(1, summary.skippedCount)
    }

    @Test
    fun `the source's own folder is recognised through a symlinked alias`() = runTest {
        fs.file("$SDCARD/Docs/a.txt", "original")

        paste(listOf("$SDCARD/Docs/a.txt"), "$STORAGE/Docs")

        assertEquals("original", fs.contentOf("$STORAGE/Docs/a.txt"))
        assertEquals("original", fs.contentOf("$STORAGE/Docs/a (1).txt"))
    }

    @Test
    fun `replace by copy swaps in the source and keeps the original`() = runTest {
        fs.file("$SDCARD/A/x.txt", "new")
        fs.file("$SDCARD/B/x.txt", "old")

        val summary =
            paste(listOf("$SDCARD/A/x.txt"), "$SDCARD/B", answer = always(ConflictResolution.REPLACE))

        assertEquals("new", fs.contentOf("$SDCARD/B/x.txt"))
        assertEquals("new", fs.contentOf("$SDCARD/A/x.txt"))
        assertEquals(1, summary.completedCount)
        assertFalse(fs.hasTemporaryLeftovers())
    }

    @Test
    fun `replace by move removes the source`() = runTest {
        fs.file("$SDCARD/A/x.txt", "new")
        fs.file("$SDCARD/B/x.txt", "old")

        paste(
            listOf("$SDCARD/A/x.txt"),
            "$SDCARD/B",
            OperationType.MOVE,
            answer = always(ConflictResolution.REPLACE)
        )

        assertEquals("new", fs.contentOf("$SDCARD/B/x.txt"))
        assertFalse(fs.exists("$SDCARD/A/x.txt"))
        assertFalse(fs.hasTemporaryLeftovers())
    }

    @Test
    fun `replacing a folder removes what only the old folder held`() = runTest {
        fs.file("$SDCARD/A/D/new.txt", "new")
        fs.file("$SDCARD/B/D/old.txt", "old")

        paste(listOf("$SDCARD/A/D"), "$SDCARD/B", answer = always(ConflictResolution.REPLACE))

        assertEquals(listOf("new.txt"), fs.childNames("$SDCARD/B/D"))
        assertFalse(fs.hasTemporaryLeftovers())
    }

    @Test
    fun `a failed swap by copy restores the existing item and keeps the source`() = runTest {
        fs.file("$SDCARD/A/x.txt", "new")
        fs.file("$SDCARD/B/x.txt", "old")
        fs.failWhen = { operation, path, destination ->
            operation == "move" && path.contains(".ashell-staging-") && destination == "$SDCARD/B/x.txt"
        }

        val summary =
            paste(listOf("$SDCARD/A/x.txt"), "$SDCARD/B", answer = always(ConflictResolution.REPLACE))

        assertEquals("old", fs.contentOf("$SDCARD/B/x.txt"))
        assertEquals("new", fs.contentOf("$SDCARD/A/x.txt"))
        assertEquals(PasteFailureReason.COMMAND_FAILED, summary.failures.single().reason)
        assertFalse(fs.hasTemporaryLeftovers())
    }

    @Test
    fun `a failed swap by move puts the source back`() = runTest {
        fs.file("$SDCARD/A/x.txt", "new")
        fs.file("$SDCARD/B/x.txt", "old")
        fs.failWhen = { operation, path, destination ->
            operation == "move" && path.contains(".ashell-staging-") && destination == "$SDCARD/B/x.txt"
        }

        paste(
            listOf("$SDCARD/A/x.txt"),
            "$SDCARD/B",
            OperationType.MOVE,
            answer = always(ConflictResolution.REPLACE)
        )

        assertEquals("old", fs.contentOf("$SDCARD/B/x.txt"))
        assertEquals("new", fs.contentOf("$SDCARD/A/x.txt"))
        assertFalse(fs.hasTemporaryLeftovers())
    }

    @Test
    fun `the existing item survives even when putting it back fails too`() = runTest {
        fs.file("$SDCARD/A/x.txt", "new")
        fs.file("$SDCARD/B/x.txt", "old")
        fs.failWhen = { operation, _, destination ->
            operation == "move" && destination == "$SDCARD/B/x.txt"
        }

        paste(listOf("$SDCARD/A/x.txt"), "$SDCARD/B", answer = always(ConflictResolution.REPLACE))

        assertTrue(fs.allPaths().any { fs.contentOf(it) == "old" })
        assertEquals("new", fs.contentOf("$SDCARD/A/x.txt"))
    }

    @Test
    fun `a failed staging copy never touches the existing item`() = runTest {
        fs.file("$SDCARD/A/x.txt", "new")
        fs.file("$SDCARD/B/x.txt", "old")
        fs.failWhen = { operation, _, _ -> operation == "copy" }

        val summary =
            paste(listOf("$SDCARD/A/x.txt"), "$SDCARD/B", answer = always(ConflictResolution.REPLACE))

        assertEquals("old", fs.contentOf("$SDCARD/B/x.txt"))
        assertEquals(1, summary.failedCount)
        assertFalse(fs.hasTemporaryLeftovers())
    }

    @Test
    fun `replace is not offered when the existing folder contains the source`() = runTest {
        fs.file("$SDCARD/A/A/inner.txt", "inner")
        fs.file("$SDCARD/A/sibling.txt", "sibling")

        val summary =
            paste(listOf("$SDCARD/A/A"), SDCARD, answer = always(ConflictResolution.REPLACE))

        assertFalse(askedConflicts.single().canReplace)
        assertEquals("inner", fs.contentOf("$SDCARD/A/A/inner.txt"))
        assertEquals("sibling", fs.contentOf("$SDCARD/A/sibling.txt"))
        assertEquals(1, summary.skippedCount)
    }

    @Test
    fun `merge checks conflicts inside the merged folder, not the folder on screen`() = runTest {
        fs.file("$SDCARD/A/D/x.txt", "source x")
        fs.file("$SDCARD/A/D/y.txt", "source y")
        fs.file("$SDCARD/B/x.txt", "top level x")
        fs.file("$SDCARD/B/D/y.txt", "existing y")

        paste(listOf("$SDCARD/A/D"), "$SDCARD/B") { conflict ->
            if (conflict.canMerge) {
                ConflictDecision(ConflictResolution.MERGE)
            } else {
                ConflictDecision(ConflictResolution.SKIP)
            }
        }

        assertEquals(
            listOf("$SDCARD/B/D", "$SDCARD/B/D/y.txt"),
            askedConflicts.map { it.destPath }
        )
        assertEquals("source x", fs.contentOf("$SDCARD/B/D/x.txt"))
        assertEquals("existing y", fs.contentOf("$SDCARD/B/D/y.txt"))
        assertEquals("top level x", fs.contentOf("$SDCARD/B/x.txt"))
    }

    @Test
    fun `merging a subfolder that also exists merges it instead of nesting it`() = runTest {
        fs.file("$SDCARD/A/D/sub/one.txt", "1")
        fs.file("$SDCARD/B/D/sub/two.txt", "2")

        paste(listOf("$SDCARD/A/D"), "$SDCARD/B", answer = always(ConflictResolution.MERGE, true))

        assertEquals(setOf("one.txt", "two.txt"), fs.childNames("$SDCARD/B/D/sub").toSet())
        assertEquals(1, askedConflicts.size)
    }

    @Test
    fun `merge by move removes the emptied source folder`() = runTest {
        fs.file("$SDCARD/A/D/x.txt", "x")
        fs.dir("$SDCARD/B/D")

        val summary = paste(
            listOf("$SDCARD/A/D"),
            "$SDCARD/B",
            OperationType.MOVE,
            answer = always(ConflictResolution.MERGE)
        )

        assertFalse(fs.exists("$SDCARD/A/D"))
        assertEquals("x", fs.contentOf("$SDCARD/B/D/x.txt"))
        assertEquals(1, summary.completedCount)
    }

    @Test
    fun `merge by move keeps the source folder when a child was skipped`() = runTest {
        fs.file("$SDCARD/A/D/x.txt", "x")
        fs.file("$SDCARD/A/D/y.txt", "source y")
        fs.file("$SDCARD/B/D/y.txt", "existing y")

        paste(listOf("$SDCARD/A/D"), "$SDCARD/B", OperationType.MOVE) { conflict ->
            if (conflict.canMerge) {
                ConflictDecision(ConflictResolution.MERGE)
            } else {
                ConflictDecision(ConflictResolution.SKIP)
            }
        }

        assertEquals(listOf("y.txt"), fs.childNames("$SDCARD/A/D"))
        assertEquals("x", fs.contentOf("$SDCARD/B/D/x.txt"))
    }

    @Test
    fun `merging into a folder that holds the source never offers to replace that folder`() =
        runTest {
            fs.file("$SDCARD/A/A/A/deep.txt", "deep")
            fs.file("$SDCARD/A/A/z.txt", "z")

            paste(listOf("$SDCARD/A/A"), SDCARD, OperationType.MOVE) { conflict ->
                if (conflict.destPath == "$SDCARD/A") {
                    ConflictDecision(ConflictResolution.MERGE)
                } else {
                    ConflictDecision(ConflictResolution.SKIP)
                }
            }

            val nested = askedConflicts.single { it.destPath == "$SDCARD/A/A" }
            assertFalse(nested.canReplace)
            assertEquals("deep", fs.contentOf("$SDCARD/A/A/A/deep.txt"))
            assertEquals("z", fs.contentOf("$SDCARD/A/z.txt"))
        }

    @Test
    fun `apply to all merge still asks about a file conflict`() = runTest {
        fs.file("$SDCARD/A/D/inner.txt", "inner")
        fs.file("$SDCARD/A/t.txt", "source t")
        fs.dir("$SDCARD/B/D")
        fs.file("$SDCARD/B/t.txt", "existing t")

        paste(listOf("$SDCARD/A/D", "$SDCARD/A/t.txt"), "$SDCARD/B") { conflict ->
            if (conflict.canMerge) {
                ConflictDecision(ConflictResolution.MERGE, applyToAll = true)
            } else {
                ConflictDecision(ConflictResolution.SKIP)
            }
        }

        assertEquals(listOf("$SDCARD/B/D", "$SDCARD/B/t.txt"), askedConflicts.map { it.destPath })
        assertEquals("existing t", fs.contentOf("$SDCARD/B/t.txt"))
        assertFalse(fs.exists("$SDCARD/B/t (1).txt"))
    }

    @Test
    fun `apply to all replace covers later conflicts without asking`() = runTest {
        listOf("a", "b", "c").forEach {
            fs.file("$SDCARD/A/$it.txt", "new $it")
            fs.file("$SDCARD/B/$it.txt", "old $it")
        }

        paste(
            listOf("a", "b", "c").map { "$SDCARD/A/$it.txt" },
            "$SDCARD/B",
            answer = always(ConflictResolution.REPLACE, applyToAll = true)
        )

        assertEquals(1, askedConflicts.size)
        listOf("a", "b", "c").forEach { assertEquals("new $it", fs.contentOf("$SDCARD/B/$it.txt")) }
    }

    @Test
    fun `the remaining count covers only items that clash`() = runTest {
        listOf("a", "b", "c").forEach { fs.file("$SDCARD/A/$it.txt", it) }
        fs.file("$SDCARD/B/a.txt")
        fs.file("$SDCARD/B/c.txt")

        paste(
            listOf("a", "b", "c").map { "$SDCARD/A/$it.txt" },
            "$SDCARD/B",
            answer = always(ConflictResolution.SKIP)
        )

        assertEquals(listOf(1, 0), askedConflicts.map { it.remainingConflicts })
    }

    @Test
    fun `a folder cannot be pasted into itself or below itself`() = runTest {
        fs.file("$SDCARD/A/sub/f.txt", "f")

        val intoItself = paste(listOf("$SDCARD/A"), "$SDCARD/A")
        val belowItself = paste(listOf("$SDCARD/A"), "$SDCARD/A/sub", OperationType.MOVE)

        assertEquals(PasteFailureReason.INTO_ITSELF, intoItself.failures.single().reason)
        assertEquals(PasteFailureReason.INTO_ITSELF, belowItself.failures.single().reason)
        assertEquals(listOf("sub"), fs.childNames("$SDCARD/A"))
        assertEquals(listOf("f.txt"), fs.childNames("$SDCARD/A/sub"))
    }

    @Test
    fun `a source deleted before pasting is reported as missing`() = runTest {
        fs.dir("$SDCARD/B")

        val summary = paste(listOf("$SDCARD/A/gone.txt"), "$SDCARD/B")

        assertEquals(PasteFailureReason.SOURCE_MISSING, summary.failures.single().reason)
        assertEquals(0, summary.completedCount)
    }

    @Test
    fun `names that differ only in case conflict on case insensitive storage`() = runTest {
        val storage = FakeRemoteFileSystem(caseInsensitive = true)
        storage.file("/data/A/photo.jpg", "new")
        storage.file("/data/B/Photo.jpg", "old")

        PasteFilesUseCase(storage)(
            PasteRequest(listOf("/data/A/photo.jpg"), "/data/B", OperationType.COPY),
            resolveConflict = { conflict ->
                askedConflicts += conflict
                ConflictDecision(ConflictResolution.SKIP)
            }
        )

        assertEquals(1, askedConflicts.size)
        assertEquals("old", storage.contentOf("/data/B/Photo.jpg"))
    }

    @Test
    fun `keep both takes the first unused number`() = runTest {
        fs.file("$SDCARD/A/a.txt", "new")
        fs.file("$SDCARD/B/a.txt", "0")
        fs.file("$SDCARD/B/a (1).txt", "1")
        fs.file("$SDCARD/B/a (2).txt", "2")

        paste(listOf("$SDCARD/A/a.txt"), "$SDCARD/B", answer = always(ConflictResolution.KEEP_BOTH))

        assertEquals("new", fs.contentOf("$SDCARD/B/a (3).txt"))
        assertEquals("1", fs.contentOf("$SDCARD/B/a (1).txt"))
    }

    @Test
    fun `keep both on a folder numbers the whole name`() = runTest {
        fs.file("$SDCARD/A/my.folder/f.txt", "f")
        fs.dir("$SDCARD/B/my.folder")

        paste(listOf("$SDCARD/A/my.folder"), "$SDCARD/B", answer = always(ConflictResolution.KEEP_BOTH))

        assertEquals("f", fs.contentOf("$SDCARD/B/my.folder (1)/f.txt"))
    }

    @Test
    fun `an item created earlier in the same paste is treated as a conflict`() = runTest {
        fs.file("$SDCARD/A/a.txt", "a")
        fs.file("$SDCARD/A/a (1).txt", "a one")
        fs.file("$SDCARD/B/a.txt", "existing")

        paste(
            listOf("$SDCARD/A/a.txt", "$SDCARD/A/a (1).txt"),
            "$SDCARD/B",
            answer = always(ConflictResolution.KEEP_BOTH)
        )

        assertEquals(listOf("$SDCARD/B/a.txt", "$SDCARD/B/a (1).txt"), askedConflicts.map { it.destPath })
        assertEquals("a", fs.contentOf("$SDCARD/B/a (1).txt"))
        assertEquals("a one", fs.contentOf("$SDCARD/B/a (2).txt"))
    }

    @Test
    fun `dismissing a conflict stops the remaining items`() = runTest {
        fs.file("$SDCARD/A/a.txt", "a")
        fs.file("$SDCARD/A/b.txt", "b")
        fs.file("$SDCARD/B/a.txt", "existing")

        val summary = paste(listOf("$SDCARD/A/a.txt", "$SDCARD/A/b.txt"), "$SDCARD/B") { null }

        assertTrue(summary.cancelled)
        assertFalse(fs.exists("$SDCARD/B/b.txt"))
        assertEquals("existing", fs.contentOf("$SDCARD/B/a.txt"))
    }

    @Test
    fun `cancelling lets the current item finish and stops before the next`() = runTest {
        fs.file("$SDCARD/A/a.txt", "a")
        fs.file("$SDCARD/A/b.txt", "b")
        fs.dir("$SDCARD/B")
        var cancelled = false

        val summary = paste(
            listOf("$SDCARD/A/a.txt", "$SDCARD/A/b.txt"),
            "$SDCARD/B",
            isCancelled = { cancelled },
            onProgress = { cancelled = true }
        )

        assertTrue(summary.cancelled)
        assertEquals("a", fs.contentOf("$SDCARD/B/a.txt"))
        assertFalse(fs.exists("$SDCARD/B/b.txt"))
    }

    @Test
    fun `a command the device rejects is counted as failed with its message`() = runTest {
        fs.file("$SDCARD/A/a.txt", "a")
        fs.dir("$SDCARD/B")
        fs.failWhen = { operation, _, _ -> operation == "copy" }

        val summary = paste(listOf("$SDCARD/A/a.txt"), "$SDCARD/B")

        val failure = summary.failures.single()
        assertEquals(PasteFailureReason.COMMAND_FAILED, failure.reason)
        assertEquals("injected copy failure", failure.message)
        assertEquals(0, summary.completedCount)
    }

    @Test
    fun `progress reports each pasted item in order`() = runTest {
        fs.file("$SDCARD/A/a.txt")
        fs.file("$SDCARD/A/b.txt")
        fs.dir("$SDCARD/B")
        val progress = mutableListOf<PasteProgress>()

        paste(listOf("$SDCARD/A/a.txt", "$SDCARD/A/b.txt"), "$SDCARD/B", onProgress = { progress += it })

        assertEquals(
            listOf(PasteProgress(1, 2, "a.txt"), PasteProgress(2, 2, "b.txt")),
            progress
        )
    }

    @Test
    fun `pasting into the root folder builds clean paths`() = runTest {
        val root = FakeRemoteFileSystem()
        root.file("/data/a.txt", "a")

        PasteFilesUseCase(root)(
            PasteRequest(listOf("/data/a.txt"), "/", OperationType.COPY),
            resolveConflict = { null }
        )

        assertEquals("a", root.contentOf("/a.txt"))
        assertNull(root.allPaths().firstOrNull { it.startsWith("//") })
    }
}
