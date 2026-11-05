package `in`.hridayan.ashell.shell.common.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceShell
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgConnection
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.shell.common.data.parser.RemotePackageParser
import `in`.hridayan.ashell.shell.common.domain.model.PackageInfo
import `in`.hridayan.ashell.shell.common.domain.repository.PackageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

private const val REMOTE_TIMEOUT_MS = 10_000L

@Singleton
class PackageRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Named("otg") private val otgShell: ExternalDeviceShell,
    @param:Named("wifiAdb") private val wifiAdbShell: ExternalDeviceShell,
    private val remotePackageParser: RemotePackageParser
) : PackageRepository {

    private val localMutex = Mutex()
    private var cachedPackages: List<PackageInfo>? = null

    private val remoteMutex = Mutex()
    private var cachedOtgKey: String? = null
    private var cachedOtgPackages: List<PackageInfo>? = null
    private var cachedWifiKey: String? = null
    private var cachedWifiPackages: List<PackageInfo>? = null

    override suspend fun getInstalledPackages(): List<PackageInfo> = localMutex.withLock {
        cachedPackages ?: loadPackages().also { cachedPackages = it }
    }

    override suspend fun refreshPackages() = localMutex.withLock {
        cachedPackages = loadPackages()
    }

    override suspend fun getPackagesForTransport(transport: ExternalDeviceTransport): List<PackageInfo> =
        remoteMutex.withLock {
            getPackagesForTransportLocked(transport)
        }

    override suspend fun refreshPackagesForTransport(transport: ExternalDeviceTransport) =
        remoteMutex.withLock {
            when (transport) {
                ExternalDeviceTransport.OTG -> {
                    cachedOtgKey = null
                    cachedOtgPackages = null
                }

                ExternalDeviceTransport.WIFI_ADB -> {
                    cachedWifiKey = null
                    cachedWifiPackages = null
                }
            }
            getPackagesForTransportLocked(transport)
            Unit
        }

    private suspend fun getPackagesForTransportLocked(transport: ExternalDeviceTransport): List<PackageInfo> {
        val deviceKey = currentDeviceKey(transport) ?: return emptyList()

        return when (transport) {
            ExternalDeviceTransport.OTG -> {
                if (cachedOtgKey == deviceKey && cachedOtgPackages != null) {
                    cachedOtgPackages.orEmpty()
                } else {
                    val packages = fetchRemotePackages(otgShell)
                    if (packages.isNotEmpty()) {
                        cachedOtgKey = deviceKey
                        cachedOtgPackages = packages
                    }
                    packages
                }
            }

            ExternalDeviceTransport.WIFI_ADB -> {
                if (cachedWifiKey == deviceKey && cachedWifiPackages != null) {
                    cachedWifiPackages.orEmpty()
                } else {
                    val packages = fetchRemotePackages(wifiAdbShell)
                    if (packages.isNotEmpty()) {
                        cachedWifiKey = deviceKey
                        cachedWifiPackages = packages
                    }
                    packages
                }
            }
        }
    }

    private fun currentDeviceKey(transport: ExternalDeviceTransport): String? = when (transport) {
        ExternalDeviceTransport.OTG -> {
            val state = OtgConnection.currentState
            if (state is OtgState.Connected && otgShell.isConnected) state.deviceName else null
        }

        ExternalDeviceTransport.WIFI_ADB -> {
            val device = WifiAdbConnection.currentDevice.value
            if (device != null && !device.isOwnDevice && wifiAdbShell.isConnected) device.id else null
        }
    }

    private suspend fun fetchRemotePackages(shell: ExternalDeviceShell): List<PackageInfo> =
        withContext(Dispatchers.IO) {
            try {
                val lines = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                    shell.execute(remotePackageParser.fetchCommand).toList()
                }.orEmpty()
                remotePackageParser.parse(lines)
            } catch (_: Exception) {
                emptyList()
            }
        }

    private suspend fun loadPackages(): List<PackageInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        apps.map { app ->
            val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            PackageInfo(
                packageName = app.packageName,
                isSystemApp = isSystemApp
            )
        }.sortedBy { it.packageName }
    }
}
