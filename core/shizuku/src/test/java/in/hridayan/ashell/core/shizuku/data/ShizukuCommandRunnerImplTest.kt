package `in`.hridayan.ashell.core.shizuku.data

import android.os.IBinder
import android.os.ParcelFileDescriptor
import ashell.core.shizuku.IShellProcess
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuClientToken
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceError
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShizukuCommandRunnerImplTest {

    private class FakeRemoteProcess : IShellProcess {
        override fun asBinder(): IBinder? = null
        override fun getInputStream(): ParcelFileDescriptor? = null
        override fun getErrorStream(): ParcelFileDescriptor? = null
        override fun getOutputStream(): ParcelFileDescriptor? = null
        override fun waitFor(): Int = 0
        override fun exitValue(): Int = 0
        override fun alive(): Boolean = false
        override fun destroy() = Unit
    }

    private class FakeProcess : Process() {
        override fun getOutputStream() = throw UnsupportedOperationException()
        override fun getInputStream() = throw UnsupportedOperationException()
        override fun getErrorStream() = throw UnsupportedOperationException()
        override fun waitFor(): Int = 0
        override fun exitValue(): Int = 0
        override fun destroy() = Unit
    }

    private val gateway = FakeShizukuGateway()
    private val policy = FakeHelperPolicy()
    private val connector =
        ShizukuUserServiceConnector(gateway, policy, TestDispatchers(Dispatchers.Unconfined))
    private val createdProcess = FakeProcess()
    private val factory = RemoteProcessFactory { createdProcess }
    private var legacyCalls = 0
    private var legacyFailure: Throwable? = null
    private val legacyStarter = LegacyProcessStarter { _, _, _ ->
        legacyCalls++
        legacyFailure?.let { throw it }
        FakeRemoteProcess()
    }

    private fun runner(dispatcher: CoroutineDispatcher) =
        ShizukuCommandRunnerImpl(
            connector,
            factory,
            legacyStarter,
            ShizukuClientToken(FakeBinder()),
            TestDispatchers(dispatcher)
        )

    private fun TestScope.standardRunner() = runner(StandardTestDispatcher(testScheduler))

    private suspend fun TestScope.connectHelper(service: FakeShellUserService) {
        val warmUp = async { standardRunner().warmUp() }
        runCurrent()
        gateway.connect(service)
        warmUp.await()
    }

    @Test
    fun `starts through the helper once it is ready`() = runTest {
        var receivedCommand: Array<String>? = null
        connectHelper(FakeShellUserService { cmd ->
            receivedCommand = cmd
            FakeRemoteProcess()
        })

        val result = standardRunner().start(arrayOf("sh", "-c", "id"), null, null)

        assertSame(createdProcess, result.getOrThrow())
        assertArrayEquals(arrayOf("sh", "-c", "id"), receivedCommand)
        assertEquals(0, legacyCalls)
    }

    @Test
    fun `starts through the legacy call without waiting while the helper binds`() = runTest {
        val result = standardRunner().start(arrayOf("id"), null, null)

        assertSame(createdProcess, result.getOrThrow())
        assertEquals(1, legacyCalls)
        assertEquals(ShizukuServiceState.Binding, connector.state.value)
    }

    @Test
    fun `binds the helper only once for concurrent commands`() = runTest {
        val runner = standardRunner()

        runner.start(arrayOf("id"), null, null)
        runner.start(arrayOf("id"), null, null)

        assertEquals(2, legacyCalls)
        assertEquals(1, gateway.bindCount)
    }

    @Test
    fun `switches to the helper after the background bind completes`() = runTest {
        val runner = standardRunner()
        runner.start(arrayOf("id"), null, null)

        gateway.connect(FakeShellUserService { FakeRemoteProcess() })
        runner.start(arrayOf("id"), null, null)

        assertEquals(1, legacyCalls)
    }

    @Test
    fun `treats a helper that stopped answering as not ready`() = runTest {
        connectHelper(FakeShellUserService { FakeRemoteProcess() })
        gateway.serviceAlive = false

        standardRunner().start(arrayOf("id"), null, null)

        assertEquals(1, legacyCalls)
    }

    @Test
    fun `waits for the helper when the legacy call fails`() = runTest {
        legacyFailure = SecurityException("newProcess is not allowed")

        val pending = async { standardRunner().start(arrayOf("id"), null, null) }
        runCurrent()
        gateway.connect(FakeShellUserService { FakeRemoteProcess() })

        assertSame(createdProcess, pending.await().getOrThrow())
    }

    @Test
    fun `skips the legacy call after it failed once on this server`() = runTest {
        legacyFailure = SecurityException("newProcess is not allowed")
        val first = async { standardRunner().start(arrayOf("id"), null, null) }
        runCurrent()
        gateway.connect(FakeShellUserService { FakeRemoteProcess() })
        first.await()
        gateway.disconnect()

        val second = async { standardRunner().start(arrayOf("id"), null, null) }
        runCurrent()
        gateway.connect(FakeShellUserService { FakeRemoteProcess() })

        assertSame(createdProcess, second.await().getOrThrow())
        assertEquals(1, legacyCalls)
    }

    @Test
    fun `maps a null remote process to ProcessStartFailed`() = runTest {
        connectHelper(FakeShellUserService { null })

        val result = standardRunner().start(arrayOf("true"), null, null)

        assertTrue(result.exceptionOrNull() is ShizukuServiceError.ProcessStartFailed)
    }

    @Test
    fun `wraps helper exceptions as ProcessStartFailed`() = runTest {
        connectHelper(FakeShellUserService { throw IllegalStateException("Could not start missing") })

        val error = standardRunner().start(arrayOf("missing"), null, null).exceptionOrNull()

        assertTrue(error is ShizukuServiceError.ProcessStartFailed)
        assertTrue(requireNotNull(error).cause is IllegalStateException)
    }

    @Test
    fun `returns a missing binder without bridging`() = runTest {
        gateway.binderAlive = false

        val result = standardRunner().start(arrayOf("id"), null, null)

        assertSame(ShizukuServiceError.BinderMissing, result.exceptionOrNull())
        assertEquals(0, legacyCalls)
        assertEquals(0, gateway.bindCount)
    }

    @Test
    fun `returns denied permission without bridging`() = runTest {
        gateway.permissionGranted = false

        val result = standardRunner().start(arrayOf("id"), null, null)

        assertSame(ShizukuServiceError.PermissionDenied, result.exceptionOrNull())
        assertEquals(0, legacyCalls)
        assertEquals(0, gateway.bindCount)
    }
}
