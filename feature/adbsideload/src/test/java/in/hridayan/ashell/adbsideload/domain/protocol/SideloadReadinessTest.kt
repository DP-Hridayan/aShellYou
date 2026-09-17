package `in`.hridayan.ashell.adbsideload.domain.protocol

import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SideloadReadinessTest {

    @Test
    fun `a booted device is not ready to sideload`() {
        val state = SideloadReadiness.stateFor(DEVICE, "device::ro.product.name=atoll;features=cmd")

        assertEquals(SideloadState.WrongMode(DEVICE), state)
    }

    @Test
    fun `a sideload banner is ready`() {
        assertEquals(SideloadState.Connected(DEVICE), SideloadReadiness.stateFor(DEVICE, "sideload::"))
    }

    @Test
    fun `a recovery banner is ready`() {
        assertEquals(SideloadState.Connected(DEVICE), SideloadReadiness.stateFor(DEVICE, "recovery::"))
    }

    @Test
    fun `an unknown or missing banner is allowed through`() {
        assertEquals(SideloadState.Connected(DEVICE), SideloadReadiness.stateFor(DEVICE, ""))
        assertEquals(SideloadState.Connected(DEVICE), SideloadReadiness.stateFor(DEVICE, "rescue::"))
    }

    @Test
    fun `banner matching ignores case and spacing`() {
        assertEquals(SideloadState.WrongMode(DEVICE), SideloadReadiness.stateFor(DEVICE, " Device ::x"))
    }

    @Test
    fun `a scan is skipped while a connection attempt is running`() {
        assertTrue(
            SideloadReadiness.isScanRedundant(SideloadState.Idle, isConnecting = true, hasLiveConnection = false)
        )
        assertTrue(
            SideloadReadiness.isScanRedundant(
                SideloadState.Connecting,
                isConnecting = false,
                hasLiveConnection = false
            )
        )
    }

    @Test
    fun `a scan is skipped only while the connection is genuinely alive`() {
        assertTrue(
            SideloadReadiness.isScanRedundant(
                SideloadState.Connected(DEVICE),
                isConnecting = false,
                hasLiveConnection = true
            )
        )
        assertFalse(
            SideloadReadiness.isScanRedundant(
                SideloadState.Connected(DEVICE),
                isConnecting = false,
                hasLiveConnection = false
            )
        )
    }

    @Test
    fun `a pending permission or wrong mode is rescanned`() {
        assertFalse(
            SideloadReadiness.isScanRedundant(
                SideloadState.DeviceFound(DEVICE),
                isConnecting = false,
                hasLiveConnection = false
            )
        )
        assertFalse(
            SideloadReadiness.isScanRedundant(
                SideloadState.WrongMode(DEVICE),
                isConnecting = false,
                hasLiveConnection = false
            )
        )
    }

    private companion object {
        const val DEVICE = "realme atoll"
    }
}
