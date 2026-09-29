package `in`.hridayan.ashell.ui.home

import `in`.hridayan.ashell.adbsideload.domain.model.SideloadError
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import `in`.hridayan.ashell.core.common.domain.model.FastbootState
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.UNKNOWN_DEVICE
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.home.presentation.model.DeviceLinkStatus
import `in`.hridayan.ashell.home.presentation.model.WifiAdbCardStatus
import org.junit.Assert.assertEquals
import org.junit.Test

private const val DEVICE_NAME = "Pixel 7"
private const val DEVICE_ID = "serial123"
private const val DEVICE_ADDRESS = "192.168.1.5:5555"
private const val DEVICE_IP = "192.168.1.5"
private const val DEVICE_PORT = 5555

class HomeCardStatusMapperTest {

    @Test
    fun `otg connected maps to connected with device name`() {
        assertEquals(
            DeviceLinkStatus.Connected(DEVICE_NAME),
            OtgState.Connected(DEVICE_NAME).toLinkStatus()
        )
    }

    @Test
    fun `otg in progress states map to connecting`() {
        listOf(
            OtgState.Searching,
            OtgState.DeviceFound(DEVICE_NAME),
            OtgState.Connecting
        ).forEach { assertEquals(DeviceLinkStatus.Connecting, it.toLinkStatus()) }
    }

    @Test
    fun `otg inactive and failure states map to idle`() {
        listOf(
            OtgState.Idle,
            OtgState.Disconnected,
            OtgState.PermissionDenied,
            OtgState.UsbManagerUnavailable,
            OtgState.Error("boom")
        ).forEach { assertEquals(DeviceLinkStatus.Idle, it.toLinkStatus()) }
    }

    @Test
    fun `fastboot connected maps to connected with device name`() {
        assertEquals(
            DeviceLinkStatus.Connected(DEVICE_NAME),
            FastbootState.Connected(DEVICE_NAME, DEVICE_ID).toLinkStatus()
        )
    }

    @Test
    fun `fastboot in progress states map to connecting`() {
        listOf(
            FastbootState.Searching,
            FastbootState.DeviceFound(DEVICE_NAME),
            FastbootState.Connecting
        ).forEach { assertEquals(DeviceLinkStatus.Connecting, it.toLinkStatus()) }
    }

    @Test
    fun `fastboot inactive and failure states map to idle`() {
        listOf(
            FastbootState.Idle,
            FastbootState.Disconnected,
            FastbootState.PermissionDenied,
            FastbootState.UsbManagerUnavailable,
            FastbootState.Error("boom")
        ).forEach { assertEquals(DeviceLinkStatus.Idle, it.toLinkStatus()) }
    }

    @Test
    fun `sideload connected maps to connected with device name`() {
        assertEquals(
            DeviceLinkStatus.Connected(DEVICE_NAME),
            SideloadState.Connected(DEVICE_NAME).toLinkStatus()
        )
    }

    @Test
    fun `sideload in progress states map to connecting`() {
        listOf(
            SideloadState.Searching,
            SideloadState.DeviceFound(DEVICE_NAME),
            SideloadState.Connecting
        ).forEach { assertEquals(DeviceLinkStatus.Connecting, it.toLinkStatus()) }
    }

    @Test
    fun `sideload inactive, wrong mode and failure states map to idle`() {
        listOf(
            SideloadState.Idle,
            SideloadState.Disconnected,
            SideloadState.WrongMode(DEVICE_NAME),
            SideloadState.PermissionDenied,
            SideloadState.UsbManagerUnavailable,
            SideloadState.Error(SideloadError.NO_DEVICE_CONNECTED)
        ).forEach { assertEquals(DeviceLinkStatus.Idle, it.toLinkStatus()) }
    }

    @Test
    fun `wifi connected to own device reports device name and own mode`() {
        assertEquals(
            WifiAdbCardStatus(DeviceLinkStatus.Connected(DEVICE_NAME), isOwnDevice = true),
            toWifiAdbCardStatus(connectedWifiState(), wifiDevice(isOwnDevice = true))
        )
    }

    @Test
    fun `wifi connected to other device reports other mode`() {
        assertEquals(
            WifiAdbCardStatus(DeviceLinkStatus.Connected(DEVICE_NAME), isOwnDevice = false),
            toWifiAdbCardStatus(connectedWifiState(), wifiDevice(isOwnDevice = false))
        )
    }

    @Test
    fun `wifi connected without current device falls back to unknown device`() {
        assertEquals(
            WifiAdbCardStatus(DeviceLinkStatus.Connected(UNKNOWN_DEVICE)),
            toWifiAdbCardStatus(connectedWifiState(), null)
        )
    }

    @Test
    fun `wifi connecting and reconnecting map to connecting`() {
        listOf(
            WifiAdbState.Connecting(DEVICE_ID, DEVICE_ADDRESS),
            WifiAdbState.Reconnecting(DEVICE_ID)
        ).forEach {
            assertEquals(
                WifiAdbCardStatus(DeviceLinkStatus.Connecting),
                toWifiAdbCardStatus(it, null)
            )
        }
    }

    @Test
    fun `wifi idle, disconnected, pairing and discovering map to idle`() {
        listOf(
            WifiAdbState.Idle,
            WifiAdbState.Disconnected(DEVICE_ID),
            WifiAdbState.Pairing(),
            WifiAdbState.Discovering()
        ).forEach {
            assertEquals(
                WifiAdbCardStatus(DeviceLinkStatus.Idle),
                toWifiAdbCardStatus(it, wifiDevice(isOwnDevice = true))
            )
        }
    }

    private fun connectedWifiState() = WifiAdbState.Connected(DEVICE_ID, DEVICE_ADDRESS)

    private fun wifiDevice(isOwnDevice: Boolean) = WifiAdbDevice(
        ip = DEVICE_IP,
        port = DEVICE_PORT,
        deviceName = DEVICE_NAME,
        isOwnDevice = isOwnDevice
    )
}
