package `in`.hridayan.ashell.logcat.data.permission

import android.content.Context
import android.content.pm.PackageManager

object LogcatPermissionHelper {
    private const val READ_LOGS = "android.permission.READ_LOGS"

    fun hasReadLogsPermission(context: Context): Boolean =
        context.checkSelfPermission(READ_LOGS) == PackageManager.PERMISSION_GRANTED

    const val GRANT_COMMAND = "adb shell pm grant in.hridayan.ashell android.permission.READ_LOGS"
}
