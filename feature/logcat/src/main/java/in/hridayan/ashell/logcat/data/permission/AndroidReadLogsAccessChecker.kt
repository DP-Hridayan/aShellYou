package `in`.hridayan.ashell.logcat.data.permission

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.logcat.domain.model.ReadLogsPermission
import `in`.hridayan.ashell.logcat.domain.permission.ReadLogsAccessChecker
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val PROC_SELF_STATUS = "/proc/self/status"
private const val GROUPS_PREFIX = "Groups:"
private const val AID_LOG = 1007
private val WHITESPACE = Regex("\\s+")

/**
 * READ_LOGS grants the `log` group at process fork time, so a process started
 * before `pm grant` reports the permission as granted yet still cannot read
 * system logs. The group probe reads the kernel's view of this process.
 */
@Singleton
class AndroidReadLogsAccessChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReadLogsAccessChecker {

    override fun isPermissionGranted(): Boolean =
        context.checkSelfPermission(ReadLogsPermission.NAME) == PackageManager.PERMISSION_GRANTED

    override fun hasLogGroup(): Result<Boolean> = runCatching {
        File(PROC_SELF_STATUS).useLines { lines -> lines.any(::isLogGroupLine) }
    }

    private fun isLogGroupLine(line: String): Boolean =
        line.startsWith(GROUPS_PREFIX) && parseGroupIds(line).contains(AID_LOG)

    private fun parseGroupIds(line: String): List<Int> =
        line.removePrefix(GROUPS_PREFIX).trim().split(WHITESPACE).mapNotNull(String::toIntOrNull)
}
