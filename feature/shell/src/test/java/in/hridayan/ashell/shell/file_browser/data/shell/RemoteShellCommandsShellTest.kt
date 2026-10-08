package `in`.hridayan.ashell.shell.file_browser.data.shell

import `in`.hridayan.ashell.shell.file_browser.domain.model.PathInfo
import `in`.hridayan.ashell.shell.file_browser.domain.model.PathKind
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * Runs the generated commands through a real shell inside a temporary folder, so quoting, exit
 * statuses and the inspection script are checked by a shell rather than by string comparison.
 * Skipped where no POSIX shell is installed.
 */
class RemoteShellCommandsShellTest {

    private val shell = LocalShell.findOrNull()
    private lateinit var root: String

    private val hostileNames = listOf(
        "it's",
        "a'; touch pwned; '",
        "\$(touch pwned)",
        "`touch pwned`",
        "semi;colon & amp",
        "two  spaces"
    )

    @Before
    fun setUp() {
        assumeTrue("no POSIX shell available", shell != null)
        root = sh("mktemp -d").trim()
        assumeTrue("mktemp gave no usable folder: $root", root.startsWith("/"))
    }

    @After
    fun tearDown() {
        if (::root.isInitialized) sh("rm -rf ${root.shellQuoted()}")
    }

    @Test
    fun `a hostile name is deleted as one literal path`() {
        val work = "$root/work"
        sh("mkdir -p ${work.shellQuoted()} && touch ${"$work/canary".shellQuoted()}")

        (hostileNames + "x' * #").forEach { name ->
            val status = run("cd ${work.shellQuoted()} && " + RemoteShellCommands.delete("$work/$name"))
            assertEquals("delete of <$name>", 0, status.exitCode)
        }

        assertTrue(exists("$work/canary"))
        assertFalse(exists("$work/pwned"))
    }

    @Test
    fun `the sandbox catches the quoting the file browser used before`() {
        val work = "$root/work"
        sh("mkdir -p ${work.shellQuoted()} && touch ${"$work/canary".shellQuoted()}")
        val brokenQuoting = "$work/x' * #".replace("'", "'\\'")

        sh("cd ${work.shellQuoted()} && rm -rf '$brokenQuoting'")

        assertFalse(exists("$work/canary"))
    }

    @Test
    fun `names with quotes and shell syntax round trip through create and inspect`() {
        val realRoot = realPathOf(root)

        hostileNames.forEach { name ->
            assertEquals(0, run(RemoteShellCommands.createDirectory("$root/$name")).exitCode)
        }
        val infos = inspect(hostileNames.map { "$root/$it" })

        assertEquals(hostileNames.map { PathInfo(PathKind.DIRECTORY, "$realRoot/$it") }, infos)
        assertFalse(exists("$root/pwned"))
    }

    @Test
    fun `a failing copy reports a non zero status and the reason`() {
        val status = run(RemoteShellCommands.copy("$root/missing", "$root/target"))

        assertNotEquals(0, status.exitCode)
        assertTrue(status.output.isNotBlank())
    }

    @Test
    fun `copy refuses an existing file and leaves it untouched`() {
        write("$root/a.txt", "new")
        write("$root/b.txt", "old")

        val status = run(RemoteShellCommands.copy("$root/a.txt", "$root/b.txt"))

        assertEquals(RemoteShellCommands.DESTINATION_EXISTS_STATUS, status.exitCode)
        assertEquals("old", read("$root/b.txt"))
    }

    @Test
    fun `move refuses an existing folder instead of moving into it`() {
        write("$root/A/x.txt", "x")
        sh("mkdir -p ${"$root/B".shellQuoted()}")

        val status = run(RemoteShellCommands.move("$root/A", "$root/B"))

        assertEquals(RemoteShellCommands.DESTINATION_EXISTS_STATUS, status.exitCode)
        assertTrue(exists("$root/A/x.txt"))
        assertFalse(exists("$root/B/A"))
    }

    @Test
    fun `copying a folder to a free name creates it under that name`() {
        write("$root/A/x.txt", "x")

        val status = run(RemoteShellCommands.copy("$root/A", "$root/A (1)"))

        assertEquals(0, status.exitCode)
        assertEquals("x", read("$root/A (1)/x.txt"))
        assertEquals("x", read("$root/A/x.txt"))
    }

    @Test
    fun `inspect reports kinds and resolved parents in order`() {
        val realRoot = realPathOf(root)
        write("$root/dir/file.txt", "f")

        val infos = inspect(
            listOf("$root/dir", "$root/dir/file.txt", "$root/dir/missing", "$root/nope/missing")
        )

        assertEquals(
            listOf(
                PathInfo(PathKind.DIRECTORY, "$realRoot/dir"),
                PathInfo(PathKind.FILE, "$realRoot/dir/file.txt"),
                PathInfo(PathKind.MISSING, "$realRoot/dir/missing"),
                PathInfo(PathKind.MISSING, null)
            ),
            infos
        )
    }

    @Test
    fun `inspect and resolve see through a symlinked folder`() {
        write("$root/target/file.txt", "f")
        sh("ln -s ${"$root/target".shellQuoted()} ${"$root/link".shellQuoted()}")
        assumeTrue("symlinks unsupported here", isSymlink("$root/link"))
        val realTarget = realPathOf("$root/target")

        val info = inspect(listOf("$root/link/file.txt")).single()
        val resolved = run(RemoteShellCommands.resolveDirectory("$root/link"))

        assertEquals("$realTarget/file.txt", info.realPath)
        assertEquals(realTarget, resolved.output.trim())
    }

    @Test
    fun `a long inspection split into batches parses completely`() {
        write("$root/present.txt", "p")
        val paths = (1..150).map { "$root/a_rather_long_file_name_to_force_batching_number_$it.txt" } +
                "$root/present.txt"

        val infos = inspect(paths)

        assertTrue(RemoteShellCommands.inspect(paths).size > 1)
        assertEquals(paths.size, infos.size)
        assertEquals(PathKind.FILE, infos.last().kind)
        assertTrue(infos.dropLast(1).all { it.kind == PathKind.MISSING })
    }

    @Test
    fun `removing a folder only succeeds once it is empty`() {
        write("$root/D/x.txt", "x")

        val refused = run(RemoteShellCommands.removeEmptyDirectory("$root/D"))
        sh("rm ${"$root/D/x.txt".shellQuoted()}")
        val removed = run(RemoteShellCommands.removeEmptyDirectory("$root/D"))

        assertNotEquals(0, refused.exitCode)
        assertEquals(0, removed.exitCode)
        assertFalse(exists("$root/D"))
    }

    private fun sh(script: String): String = requireNotNull(shell).run(script)

    private fun run(command: String): CommandStatus {
        val output = sh(command)
        return requireNotNull(RemoteShellCommands.parseStatus(output)) { "no status in: $output" }
    }

    private fun inspect(paths: List<String>): List<PathInfo> =
        RemoteShellCommands.inspect(paths).flatMap { (batch, command) ->
            val status = run(command)
            assertEquals(status.output, 0, status.exitCode)
            requireNotNull(RemoteShellCommands.parseInspection(status.output, batch)) {
                "unparseable: ${status.output}"
            }
        }

    private fun exists(path: String): Boolean =
        sh("[ -e ${path.shellQuoted()} ] && echo yes").trim() == "yes"

    private fun isSymlink(path: String): Boolean =
        sh("[ -L ${path.shellQuoted()} ] && echo yes").trim() == "yes"

    private fun realPathOf(path: String): String = sh("cd ${path.shellQuoted()} && pwd -P").trim()

    private fun read(path: String): String = sh("cat ${path.shellQuoted()}")

    private fun write(path: String, content: String) {
        val parent = path.substringBeforeLast('/')
        sh("mkdir -p ${parent.shellQuoted()} && printf '%s' ${content.shellQuoted()} > ${path.shellQuoted()}")
    }
}
