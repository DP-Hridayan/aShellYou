package `in`.hridayan.ashell.mirror.data.server

import `in`.hridayan.ashell.mirror.data.FakeExternalDeviceChannel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrcpyServerDeployerTest {

    private val jar = ByteArray(1_000) { it.toByte() }
    private val remotePath = "/data/local/tmp/ashellyou-scrcpy-server-v4.1.jar"
    private val channel = FakeExternalDeviceChannel()

    private fun deployer(dispatcher: TestDispatcher) = ScrcpyServerDeployer(
        jarSource = { jar },
        remotePath = remotePath,
        remotePrefix = "/data/local/tmp/ashellyou-scrcpy-server-v",
        ioDispatcher = dispatcher
    )

    @Test
    fun `pushes when the device has no server`() = runTest {
        val result = deployer(StandardTestDispatcher(testScheduler)).deploy(channel).getOrThrow()

        assertFalse(result.skipped)
        assertEquals(listOf(remotePath), channel.pushedPaths)
    }

    @Test
    fun `skips the push when the same server is already there`() = runTest {
        channel.remoteFiles[remotePath] = jar.size.toLong()

        val result = deployer(StandardTestDispatcher(testScheduler)).deploy(channel).getOrThrow()

        assertTrue(result.skipped)
        assertTrue(channel.pushedPaths.isEmpty())
    }

    @Test
    fun `replaces a server of a different size and removes other versions`() = runTest {
        channel.remoteFiles[remotePath] = 3

        deployer(StandardTestDispatcher(testScheduler)).deploy(channel).getOrThrow()

        assertEquals(listOf(remotePath), channel.pushedPaths)
        val cleanup = channel.openedServices.single { it.startsWith("shell:") }
        assertTrue(cleanup.contains("/data/local/tmp/ashellyou-scrcpy-server-v*.jar"))
        assertTrue(cleanup.contains("\"$remotePath\""))
    }

    @Test
    fun `a failed push is reported`() = runTest {
        channel.pushFails = true

        val result = deployer(StandardTestDispatcher(testScheduler)).deploy(channel)

        assertTrue(result.isFailure)
    }
}
