package `in`.hridayan.ashell.core.shizuku.data

import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceError
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShizukuUserServiceConnectorTest {

    private val gateway = FakeShizukuGateway()
    private val connector = ShizukuUserServiceConnector(gateway, TestDispatchers(Dispatchers.Unconfined))

    @Test
    fun `fails fast when binder is missing and no manager answers`() = runTest {
        gateway.binderAlive = false

        val result = connector.service()

        assertSame(ShizukuServiceError.BinderMissing, result.exceptionOrNull())
        assertEquals(1, gateway.requestCount)
        assertEquals(0, gateway.bindCount)
    }

    @Test
    fun `requests a binder from the managers and binds once it arrives`() = runTest {
        gateway.binderAlive = false
        gateway.binderAfterRequest = true
        val service = FakeShellUserService()

        val pending = async { connector.service() }
        runCurrent()
        gateway.connect(service)

        assertSame(service, pending.await().getOrThrow())
        assertEquals(1, gateway.requestCount)
    }

    @Test
    fun `remembers a helper start failure until the server changes`() = runTest {
        assertSame(ShizukuServiceError.BindTimeout, connector.service().exceptionOrNull())
        assertSame(ShizukuServiceError.BindTimeout, connector.service().exceptionOrNull())
        assertEquals(1, gateway.bindCount)

        gateway.serverChanged()
        val pending = async { connector.service() }
        runCurrent()
        gateway.connect(FakeShellUserService())

        assertTrue(pending.await().isSuccess)
        assertEquals(2, gateway.bindCount)
    }

    @Test
    fun `drops the bound helper when a different server delivers a binder`() = runTest {
        val first = async { connector.service() }
        runCurrent()
        gateway.connect(FakeShellUserService())
        first.await()

        gateway.serverChanged()

        assertEquals(ShizukuServiceState.Idle, connector.state.value)
        assertEquals(1, gateway.unbindCount)
        val second = async { connector.service() }
        runCurrent()
        gateway.connect(FakeShellUserService())
        assertTrue(second.await().isSuccess)
        assertEquals(2, gateway.bindCount)
    }

    @Test
    fun `fails fast when permission is denied`() = runTest {
        gateway.permissionGranted = false

        val result = connector.service()

        assertSame(ShizukuServiceError.PermissionDenied, result.exceptionOrNull())
    }

    @Test
    fun `binds and returns the connected service`() = runTest {
        val service = FakeShellUserService(uid = 0)
        val pending = async { connector.service() }
        runCurrent()
        assertEquals(ShizukuServiceState.Binding, connector.state.value)

        gateway.connect(service)

        assertSame(service, pending.await().getOrThrow())
        assertEquals(ShizukuServiceState.Ready(0), connector.state.value)
    }

    @Test
    fun `reuses a live service without rebinding`() = runTest {
        val service = FakeShellUserService()
        val first = async { connector.service() }
        runCurrent()
        gateway.connect(service)
        first.await()

        val second = connector.service()

        assertSame(service, second.getOrThrow())
        assertEquals(1, gateway.bindCount)
    }

    @Test
    fun `rebinds after the service disconnects`() = runTest {
        val first = async { connector.service() }
        runCurrent()
        gateway.connect(FakeShellUserService())
        first.await()

        gateway.disconnect()
        val replacement = FakeShellUserService()
        val second = async { connector.service() }
        runCurrent()
        gateway.connect(replacement)

        assertSame(replacement, second.await().getOrThrow())
        assertEquals(2, gateway.bindCount)
    }

    @Test
    fun `times out when the service never connects`() = runTest {
        val result = connector.service()

        assertSame(ShizukuServiceError.BindTimeout, result.exceptionOrNull())
        assertEquals(
            ShizukuServiceState.Unavailable(ShizukuServiceError.BindTimeout),
            connector.state.value
        )
    }

    @Test
    fun `reports bind exceptions as BindFailed`() = runTest {
        gateway.throwOnBind = IllegalStateException("binder not received")

        val result = connector.service()

        assertTrue(result.exceptionOrNull() is ShizukuServiceError.BindFailed)
    }

    @Test
    fun `binder death fails the pending bind`() = runTest {
        val pending = async { connector.service() }
        runCurrent()

        requireNotNull(gateway.deadListener).invoke()

        assertSame(ShizukuServiceError.BinderDied, pending.await().exceptionOrNull())
    }

    @Test
    fun `binder death asks the managers for a replacement binder`() = runTest {
        requireNotNull(gateway.deadListener).invoke()

        assertEquals(1, gateway.requestCount)
    }
}
