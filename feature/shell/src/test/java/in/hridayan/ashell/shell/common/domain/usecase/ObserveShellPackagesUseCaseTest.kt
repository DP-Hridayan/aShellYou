package `in`.hridayan.ashell.shell.common.domain.usecase

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.shell.common.domain.model.PackageInfo
import `in`.hridayan.ashell.shell.common.domain.model.ShellMode
import `in`.hridayan.ashell.shell.common.domain.repository.PackageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakePackageRepository : PackageRepository {
    var localPackages = listOf(PackageInfo("com.local.app", false))
    var otgPackages = listOf(PackageInfo("com.otg.app", false))
    var wifiPackages = listOf(PackageInfo("com.wifi.app", false))

    override suspend fun getInstalledPackages(): List<PackageInfo> = localPackages

    override suspend fun getPackagesForTransport(transport: ExternalDeviceTransport): List<PackageInfo> =
        when (transport) {
            ExternalDeviceTransport.OTG -> otgPackages
            ExternalDeviceTransport.WIFI_ADB -> wifiPackages
        }

    override suspend fun refreshPackages() = Unit

    override suspend fun refreshPackagesForTransport(transport: ExternalDeviceTransport) = Unit
}

class ObserveShellPackagesUseCaseTest {

    private val fakeRepo = FakePackageRepository()
    private val useCase = ObserveShellPackagesUseCase(fakeRepo)

    private val shellModeFlow = MutableStateFlow(ShellMode.LOCAL)
    private val otgStateFlow = MutableStateFlow<OtgState>(OtgState.Idle)
    private val wifiAdbStateFlow = MutableStateFlow<WifiAdbState>(WifiAdbState.Idle)
    private val wifiDeviceFlow = MutableStateFlow<WifiAdbDevice?>(null)

    @Test
    fun localModeEmitsLocalPackages() = runTest {
        shellModeFlow.value = ShellMode.LOCAL

        val result = useCase(
            shellMode = shellModeFlow,
            otgState = otgStateFlow,
            wifiAdbState = wifiAdbStateFlow,
            wifiAdbCurrentDevice = wifiDeviceFlow
        ).first()

        assertEquals(fakeRepo.localPackages, result)
    }

    @Test
    fun otgModeDisconnectedEmitsEmptyList() = runTest {
        shellModeFlow.value = ShellMode.OTG
        otgStateFlow.value = OtgState.Idle

        val result = useCase(
            shellMode = shellModeFlow,
            otgState = otgStateFlow,
            wifiAdbState = wifiAdbStateFlow,
            wifiAdbCurrentDevice = wifiDeviceFlow
        ).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun otgModeConnectedEmitsOtgPackages() = runTest {
        shellModeFlow.value = ShellMode.OTG
        otgStateFlow.value = OtgState.Connected("test-otg")

        val result = useCase(
            shellMode = shellModeFlow,
            otgState = otgStateFlow,
            wifiAdbState = wifiAdbStateFlow,
            wifiAdbCurrentDevice = wifiDeviceFlow
        ).first()

        assertEquals(fakeRepo.otgPackages, result)
    }

    @Test
    fun wifiModeOwnDeviceEmitsLocalPackages() = runTest {
        shellModeFlow.value = ShellMode.WIFI
        val ownDevice = WifiAdbDevice(id = "127.0.0.1:5555", ip = "127.0.0.1", port = 5555, isOwnDevice = true)
        wifiDeviceFlow.value = ownDevice
        wifiAdbStateFlow.value = WifiAdbState.Connected(deviceId = ownDevice.id, address = ownDevice.ip)

        val result = useCase(
            shellMode = shellModeFlow,
            otgState = otgStateFlow,
            wifiAdbState = wifiAdbStateFlow,
            wifiAdbCurrentDevice = wifiDeviceFlow
        ).first()

        assertEquals(fakeRepo.localPackages, result)
    }

    @Test
    fun wifiModeOtherDeviceEmitsRemotePackages() = runTest {
        shellModeFlow.value = ShellMode.WIFI
        val otherDevice = WifiAdbDevice(id = "192.168.1.100:5555", ip = "192.168.1.100", port = 5555, isOwnDevice = false)
        wifiDeviceFlow.value = otherDevice
        wifiAdbStateFlow.value = WifiAdbState.Connected(deviceId = otherDevice.id, address = otherDevice.ip)

        val result = useCase(
            shellMode = shellModeFlow,
            otgState = otgStateFlow,
            wifiAdbState = wifiAdbStateFlow,
            wifiAdbCurrentDevice = wifiDeviceFlow
        ).first()

        assertEquals(fakeRepo.wifiPackages, result)
    }

    @Test
    fun wifiModeDisconnectedEmitsEmptyList() = runTest {
        shellModeFlow.value = ShellMode.WIFI
        wifiDeviceFlow.value = null
        wifiAdbStateFlow.value = WifiAdbState.Idle

        val result = useCase(
            shellMode = shellModeFlow,
            otgState = otgStateFlow,
            wifiAdbState = wifiAdbStateFlow,
            wifiAdbCurrentDevice = wifiDeviceFlow
        ).first()

        assertTrue(result.isEmpty())
    }
}
