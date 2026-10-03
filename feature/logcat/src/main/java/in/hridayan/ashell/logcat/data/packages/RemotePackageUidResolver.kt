package `in`.hridayan.ashell.logcat.data.packages

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceShell
import `in`.hridayan.ashell.logcat.domain.repository.PackageUidResolver
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val LIST_PACKAGES_WITH_UIDS = "pm list packages -U"
private val PACKAGE_LINE = Regex("""package:(\S+)\s+uid:(\d+)""")

/**
 * Resolves packages on an external device by reading `pm list packages -U` over its ADB shell.
 * The table is read once and cached, so an instance must serve only one connection.
 */
class RemotePackageUidResolver(
    private val shell: ExternalDeviceShell,
) : PackageUidResolver {

    private val mutex = Mutex()
    private var uidByPackage: Map<String, String>? = null

    override suspend fun uidsOf(packages: Set<String>): Map<String, String> =
        table().filterKeys { it in packages }

    override suspend fun packagesOf(uid: String): List<String> =
        table().filterValues { it == uid }.keys.sorted()

    /** An empty answer usually means the shell was not ready, so it is not cached. */
    private suspend fun table(): Map<String, String> = mutex.withLock {
        uidByPackage ?: readTable().also { if (it.isNotEmpty()) uidByPackage = it }
    }

    private suspend fun readTable(): Map<String, String> =
        shell.execute(LIST_PACKAGES_WITH_UIDS)
            .mapNotNull { line -> PACKAGE_LINE.find(line)?.destructured?.let { (name, uid) -> name to uid } }
            .toList()
            .toMap()
}
