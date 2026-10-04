package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class CodePairStatusTest {

    @Test
    fun `no wifi asks for wifi even before any device is found`() {
        assertEquals(
            CodePairStatus.WifiRequired,
            codePairStatusFor(isWifiConnected = false, hasDevices = false)
        )
    }

    @Test
    fun `no wifi outranks a device that is still listed`() {
        assertEquals(
            CodePairStatus.WifiRequired,
            codePairStatusFor(isWifiConnected = false, hasDevices = true)
        )
    }

    @Test
    fun `wifi with nothing found yet is waiting`() {
        assertEquals(
            CodePairStatus.Waiting,
            codePairStatusFor(isWifiConnected = true, hasDevices = false)
        )
    }

    @Test
    fun `wifi with a device found reports it`() {
        assertEquals(
            CodePairStatus.DeviceFound,
            codePairStatusFor(isWifiConnected = true, hasDevices = true)
        )
    }
}
