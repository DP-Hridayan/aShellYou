package `in`.hridayan.ashell.shell.common.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.shell.common.domain.model.PackageInfo
import `in`.hridayan.ashell.shell.common.domain.repository.PackageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PackageRepository {

    private val mutex = Mutex()
    private var cachedPackages: List<PackageInfo>? = null

    override suspend fun getInstalledPackages(): List<PackageInfo> = mutex.withLock {
        cachedPackages ?: loadPackages().also { cachedPackages = it }
    }

    override suspend fun refreshPackages() = mutex.withLock {
        cachedPackages = loadPackages()
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
