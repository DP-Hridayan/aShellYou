package `in`.hridayan.ashell.logcat.data.apps

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.logcat.domain.model.InstalledApp
import `in`.hridayan.ashell.logcat.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Lists apps through the package manager; the app declares `QUERY_ALL_PACKAGES` to see them all. */
class InstalledAppsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : InstalledAppsRepository {

    override suspend fun installedApps(): Result<List<InstalledApp>> = withContext(Dispatchers.IO) {
        try {
            Result.success(readInstalledApps())
        } catch (e: SecurityException) {
            Result.failure(e)
        }
    }

    private fun readInstalledApps(): List<InstalledApp> {
        val packageManager = context.packageManager
        return packageManager.getInstalledApplications(0)
            .map { InstalledApp(it.packageName, packageManager.getApplicationLabel(it).toString()) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }
}
