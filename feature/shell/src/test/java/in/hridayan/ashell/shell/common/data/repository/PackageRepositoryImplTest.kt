package `in`.hridayan.ashell.shell.common.data.repository

import android.content.Context
import android.content.ContextWrapper
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceShell
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgConnection
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.shell.common.data.parser.RemotePackageParser
import `in`.hridayan.ashell.shell.common.domain.model.PackageInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeTestContext : ContextWrapper(null)

private class FakeExternalDeviceShell(
    var output: List<String> = emptyList(),
    override var isConnected: Boolean = true
) : ExternalDeviceShell {
    var executeCount: Int = 0
    var lastExecutedCommand: String? = null

    override fun execute(command: String): Flow<String> {
        executeCount++
        lastExecutedCommand = command
        return output.asFlow()
    }
}

class PackageRepositoryImplTest {

    private val parser = RemotePackageParser()
    private val otgShell = FakeExternalDeviceShell()
    private val wifiAdbShell = FakeExternalDeviceShell()
    private val fakeContext: Context = FakeTestContext()

    private lateinit var repository: PackageRepositoryImpl

    @Before
    fun setUp() {
        OtgConnection.updateState(OtgState.Idle)
        WifiAdbConnection.reset()
        repository = PackageRepositoryImpl(
            context = fakeContext,
            otgShell = otgShell,
            wifiAdbShell = wifiAdbShell,
            remotePackageParser = parser
        )
    }

    @After
    fun tearDown() {
        OtgConnection.updateState(OtgState.Idle)
        WifiAdbConnection.reset()
    }

    @Test
    fun otgDisconnectedReturnsEmptyList() = runTest {
        otgShell.isConnected = false
        OtgConnection.updateState(OtgState.Idle)

        val packages = repository.getPackagesForTransport(ExternalDeviceTransport.OTG)

        assertTrue(packages.isEmpty())
        assertEquals(0, otgShell.executeCount)
    }

    @Test
    fun otgConnectedFetchesAndCachesPackages() = runTest {
        otgShell.isConnected = true
        OtgConnection.updateState(OtgState.Connected("device_otg_1"))
        otgShell.output = listOf(
            "package:com.android.settings",
            parser.delimiter,
            "package:com.user.app"
        )

        val firstCall = repository.getPackagesForTransport(ExternalDeviceTransport.OTG)
        val secondCall = repository.getPackagesForTransport(ExternalDeviceTransport.OTG)

        val expected = listOf(
            PackageInfo("com.android.settings", isSystemApp = true),
            PackageInfo("com.user.app", isSystemApp = false)
        )
        assertEquals(expected, firstCall)
        assertEquals(expected, secondCall)
        assertEquals(1, otgShell.executeCount)
    }

    @Test
    fun otgDeviceChangeInvalidatesCache() = runTest {
        otgShell.isConnected = true
        OtgConnection.updateState(OtgState.Connected("device_otg_1"))
        otgShell.output = listOf(
            "package:com.device1.app"
        )

        val device1Packages = repository.getPackagesForTransport(ExternalDeviceTransport.OTG)
        assertEquals(listOf(PackageInfo("com.device1.app", false)), device1Packages)

        OtgConnection.updateState(OtgState.Connected("device_otg_2"))
        otgShell.output = listOf(
            "package:com.device2.app"
        )

        val device2Packages = repository.getPackagesForTransport(ExternalDeviceTransport.OTG)
        assertEquals(listOf(PackageInfo("com.device2.app", false)), device2Packages)
        assertEquals(2, otgShell.executeCount)
    }

    @Test
    fun wifiAdbOtherDeviceFetchesAndCachesPackages() = runTest {
        wifiAdbShell.isConnected = true
        val remoteDevice = WifiAdbDevice(id = "192.168.1.50:5555", ip = "192.168.1.50", port = 5555, isOwnDevice = false)
        WifiAdbConnection.setCurrentDevice(remoteDevice)
        WifiAdbConnection.updateState(WifiAdbState.Connected(deviceId = remoteDevice.id, address = remoteDevice.ip))
        wifiAdbShell.output = listOf(
            "package:com.remote.system",
            parser.delimiter,
            "package:com.remote.user"
        )

        val packages = repository.getPackagesForTransport(ExternalDeviceTransport.WIFI_ADB)

        val expected = listOf(
            PackageInfo("com.remote.system", isSystemApp = true),
            PackageInfo("com.remote.user", isSystemApp = false)
        )
        assertEquals(expected, packages)
        assertEquals(1, wifiAdbShell.executeCount)
    }

    @Test
    fun refreshPackagesForTransportForcesReload() = runTest {
        otgShell.isConnected = true
        OtgConnection.updateState(OtgState.Connected("device_otg_1"))
        otgShell.output = listOf("package:com.initial.app")

        repository.getPackagesForTransport(ExternalDeviceTransport.OTG)
        assertEquals(1, otgShell.executeCount)

        otgShell.output = listOf("package:com.updated.app")
        repository.refreshPackagesForTransport(ExternalDeviceTransport.OTG)

        val updated = repository.getPackagesForTransport(ExternalDeviceTransport.OTG)
        assertEquals(listOf(PackageInfo("com.updated.app", false)), updated)
        assertEquals(2, otgShell.executeCount)
    }
}
