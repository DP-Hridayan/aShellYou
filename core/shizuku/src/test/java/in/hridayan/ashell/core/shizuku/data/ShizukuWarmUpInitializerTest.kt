package `in`.hridayan.ashell.core.shizuku.data

import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceState
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

class ShizukuWarmUpInitializerTest {

    private val gateway = FakeShizukuGateway()
    private val policy = FakeHelperPolicy(warmUpWanted = true)
    private val dispatchers = TestDispatchers(Dispatchers.Unconfined)
    private val connector = ShizukuUserServiceConnector(gateway, policy, dispatchers)
    private val initializer = ShizukuWarmUpInitializer(connector, gateway, policy, dispatchers)

    @Test
    fun `binds the helper on start when warm-up is wanted`() {
        initializer.start()

        assertEquals(1, gateway.bindCount)
    }

    @Test
    fun `does not bind when warm-up is not wanted`() {
        policy.warmUpWanted.value = false

        initializer.start()

        assertEquals(0, gateway.bindCount)
    }

    @Test
    fun `binds once warm-up becomes wanted`() {
        policy.warmUpWanted.value = false
        initializer.start()

        policy.warmUpWanted.value = true

        assertEquals(1, gateway.bindCount)
    }

    @Test
    fun `binds after permission is granted`() {
        gateway.permissionGranted = false
        initializer.start()
        assertEquals(0, gateway.bindCount)

        gateway.grantPermission()

        assertEquals(1, gateway.bindCount)
    }

    @Test
    fun `binds again after the server changes`() {
        initializer.start()
        gateway.connect(FakeShellUserService())

        gateway.serverChanged()

        assertEquals(2, gateway.bindCount)
    }

    @Test
    fun `stops the helper and binds a fresh one when keep-alive is turned off`() {
        policy.keepAlive.value = true
        initializer.start()
        gateway.connect(FakeShellUserService())

        policy.keepAlive.value = false

        assertEquals(1, gateway.unbindCount)
        assertEquals(2, gateway.bindCount)
        assertEquals(false, gateway.lastKeepAlive)
        assertEquals(ShizukuServiceState.Binding, connector.state.value)
    }

    @Test
    fun `leaves the helper running when keep-alive is turned on`() {
        initializer.start()
        gateway.connect(FakeShellUserService())

        policy.keepAlive.value = true

        assertEquals(0, gateway.unbindCount)
        assertEquals(1, gateway.bindCount)
    }
}
