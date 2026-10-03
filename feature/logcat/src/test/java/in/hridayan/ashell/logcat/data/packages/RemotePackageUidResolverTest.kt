package `in`.hridayan.ashell.logcat.data.packages

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceShell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Answers every command with the prepared lines and counts the commands it was given. */
private class FakeShell(var output: List<String>) : ExternalDeviceShell {
    var commands = 0

    override fun execute(command: String): Flow<String> {
        commands++
        return output.asFlow()
    }

    override val isConnected: Boolean = true
}

private val PM_OUTPUT = listOf(
    "package:com.android.chrome uid:10077",
    "package:com.android.phone uid:1001",
    "package:com.android.server.telecom uid:1001",
    "",
)

class RemotePackageUidResolverTest {

    @Test
    fun `installed packages resolve to their uids`() = runTest {
        val resolver = RemotePackageUidResolver(FakeShell(PM_OUTPUT))
        assertEquals(
            mapOf("com.android.chrome" to "10077"),
            resolver.uidsOf(setOf("com.android.chrome", "com.missing")),
        )
    }

    @Test
    fun `packages sharing a uid are all listed`() = runTest {
        val resolver = RemotePackageUidResolver(FakeShell(PM_OUTPUT))
        assertEquals(
            listOf("com.android.phone", "com.android.server.telecom"),
            resolver.packagesOf("1001"),
        )
    }

    @Test
    fun `the package table is read once`() = runTest {
        val shell = FakeShell(PM_OUTPUT)
        val resolver = RemotePackageUidResolver(shell)
        resolver.uidsOf(setOf("com.android.chrome"))
        resolver.packagesOf("10077")
        assertEquals(1, shell.commands)
    }

    @Test
    fun `an empty answer is not cached, so a later lookup retries`() = runTest {
        val shell = FakeShell(emptyList())
        val resolver = RemotePackageUidResolver(shell)
        assertTrue(resolver.uidsOf(setOf("com.android.chrome")).isEmpty())
        shell.output = PM_OUTPUT
        assertEquals(mapOf("com.android.chrome" to "10077"), resolver.uidsOf(setOf("com.android.chrome")))
    }
}
