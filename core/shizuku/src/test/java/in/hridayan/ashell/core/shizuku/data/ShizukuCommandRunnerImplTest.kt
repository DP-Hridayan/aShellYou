package `in`.hridayan.ashell.core.shizuku.data

import android.os.IBinder
import android.os.ParcelFileDescriptor
import ashell.core.shizuku.IShellProcess
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
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
    private val connector = ShizukuUserServiceConnector(gateway, TestDispatchers(Dispatchers.Unconfined))
    private val createdProcess = FakeProcess()
    private val factory = RemoteProcessFactory { createdProcess }
    private var legacyCalls = 0
    private val legacyStarter = LegacyProcessStarter { _, _, _ ->
        legacyCalls++
        FakeRemoteProcess()
    }

    private fun runner(dispatcher: CoroutineDispatcher) =
        ShizukuCommandRunnerImpl(connector, factory, legacyStarter, TestDispatchers(dispatcher))

    @Test
    fun `starts a process through the connected service`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var receivedCommand: Array<String>? = null
        val service = FakeShellUserService { cmd ->
            receivedCommand = cmd
            FakeRemoteProcess()
        }

        val pending = async { runner(dispatcher).start(arrayOf("sh", "-c", "id"), null, null) }
        runCurrent()
        gateway.connect(service)

        assertSame(createdProcess, pending.await().getOrThrow())
        assertArrayEquals(arrayOf("sh", "-c", "id"), receivedCommand)
    }

    @Test
    fun `maps a null remote process to ProcessStartFailed`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val pending = async { runner(dispatcher).start(arrayOf("true"), null, null) }
        runCurrent()
        gateway.connect(FakeShellUserService { null })

        assertTrue(pending.await().exceptionOrNull() is ShizukuServiceError.ProcessStartFailed)
    }

    @Test
    fun `wraps helper exceptions as ProcessStartFailed`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val pending = async { runner(dispatcher).start(arrayOf("missing"), null, null) }
        runCurrent()
        gateway.connect(FakeShellUserService { throw IllegalStateException("Could not start missing") })

        val error = pending.await().exceptionOrNull()
        assertTrue(error is ShizukuServiceError.ProcessStartFailed)
        assertTrue(requireNotNull(error).cause is IllegalStateException)
    }

    @Test
    fun `passes connector errors through unchanged`() = runTest {
        gateway.binderAlive = false

        val result = runner(StandardTestDispatcher(testScheduler)).start(arrayOf("id"), null, null)

        assertSame(ShizukuServiceError.BinderMissing, result.exceptionOrNull())
        assertEquals(0, legacyCalls)
    }

    @Test
    fun `falls back to the legacy newProcess call when the helper cannot start`() = runTest {
        val result = runner(StandardTestDispatcher(testScheduler)).start(arrayOf("id"), null, null)

        assertSame(createdProcess, result.getOrThrow())
        assertEquals(1, legacyCalls)
    }

    @Test
    fun `does not fall back when permission is denied`() = runTest {
        gateway.permissionGranted = false

        val result = runner(StandardTestDispatcher(testScheduler)).start(arrayOf("id"), null, null)

        assertSame(ShizukuServiceError.PermissionDenied, result.exceptionOrNull())
        assertEquals(0, legacyCalls)
    }
}
