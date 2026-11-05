package `in`.hridayan.ashell.shell.common.domain.repository

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.shell.common.domain.model.PackageInfo

interface PackageRepository {
    suspend fun getInstalledPackages(): List<PackageInfo>
    suspend fun getPackagesForTransport(transport: ExternalDeviceTransport): List<PackageInfo>
    suspend fun refreshPackages()
    suspend fun refreshPackagesForTransport(transport: ExternalDeviceTransport)
}
