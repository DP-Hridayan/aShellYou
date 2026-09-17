package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.screens

import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.PairingDiscoveryMode
import org.junit.Assert.assertEquals
import org.junit.Test

class PairingDiscoveryModeTest {

    @Test
    fun `qr tab scans for qr pairing`() {
        assertEquals(
            PairingDiscoveryMode.Qr,
            discoveryModeFor(PairingTab.QrPair, isWifiConnected = true)
        )
    }

    @Test
    fun `code tab scans for code pairing`() {
        assertEquals(
            PairingDiscoveryMode.Code,
            discoveryModeFor(PairingTab.CodePair, isWifiConnected = true)
        )
    }

    @Test
    fun `saved devices tab does not scan`() {
        assertEquals(
            PairingDiscoveryMode.None,
            discoveryModeFor(PairingTab.SavedDevices, isWifiConnected = true)
        )
    }

    @Test
    fun `no wifi means no scan on any tab`() {
        PairingTab.entries.forEach { tab ->
            assertEquals(
                PairingDiscoveryMode.None,
                discoveryModeFor(tab, isWifiConnected = false)
            )
        }
    }
}
