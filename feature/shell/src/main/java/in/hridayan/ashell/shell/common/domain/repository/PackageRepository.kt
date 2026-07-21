package `in`.hridayan.ashell.shell.common.domain.repository

import `in`.hridayan.ashell.shell.common.domain.model.PackageInfo

interface PackageRepository {
    suspend fun getInstalledPackages(): List<PackageInfo>
    suspend fun refreshPackages()
}
