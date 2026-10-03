package `in`.hridayan.ashell.logcat.data.packages

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.logcat.domain.repository.PackageUidResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Resolves packages on this device through the package manager. */
@Singleton
class LocalPackageUidResolver @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : PackageUidResolver {

    override suspend fun uidsOf(packages: Set<String>): Map<String, String> =
        withContext(Dispatchers.IO) {
            packages.mapNotNull { name -> uidOf(name)?.let { name to it.toString() } }.toMap()
        }

    override suspend fun packagesOf(uid: String): List<String> = withContext(Dispatchers.IO) {
        val numericUid = uid.toIntOrNull() ?: return@withContext emptyList()
        context.packageManager.getPackagesForUid(numericUid)?.sorted().orEmpty()
    }

    private fun uidOf(packageName: String): Int? = try {
        context.packageManager.getApplicationInfo(packageName, 0).uid
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
}
