package `in`.hridayan.ashell.shell.common.domain.usecase

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgConnection
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.shell.common.domain.model.PackageInfo
import `in`.hridayan.ashell.shell.common.domain.model.ShellMode
import `in`.hridayan.ashell.shell.common.domain.repository.PackageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveShellPackagesUseCase @Inject constructor(
    private val packageRepository: PackageRepository
) {

    operator fun invoke(
        shellMode: Flow<ShellMode>,
        otgState: Flow<OtgState> = OtgConnection.state,
        wifiAdbState: Flow<WifiAdbState> = WifiAdbConnection.state,
        wifiAdbCurrentDevice: Flow<WifiAdbDevice?> = WifiAdbConnection.currentDevice
    ): Flow<List<PackageInfo>> =
        combine(
            shellMode,
            otgState,
            wifiAdbState,
            wifiAdbCurrentDevice
        ) { mode, otg, wifi, device ->
            PackageFetchTarget(mode, otg, wifi, device)
        }.distinctUntilChanged().map { target ->
            resolvePackages(target)
        }

    private suspend fun resolvePackages(target: PackageFetchTarget): List<PackageInfo> =
        when (target.mode) {
            ShellMode.LOCAL -> packageRepository.getInstalledPackages()

            ShellMode.OTG -> {
                if (target.otgState is OtgState.Connected) {
                    packageRepository.getPackagesForTransport(ExternalDeviceTransport.OTG)
                } else {
                    emptyList()
                }
            }

            ShellMode.WIFI -> {
                if (target.wifiAdbState is WifiAdbState.Connected && target.device != null) {
                    if (target.device.isOwnDevice) {
                        packageRepository.getInstalledPackages()
                    } else {
                        packageRepository.getPackagesForTransport(ExternalDeviceTransport.WIFI_ADB)
                    }
                } else {
                    emptyList()
                }
            }
        }

    private data class PackageFetchTarget(
        val mode: ShellMode,
        val otgState: OtgState,
        val wifiAdbState: WifiAdbState,
        val device: WifiAdbDevice?
    )
}
