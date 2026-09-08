package `in`.hridayan.ashell.logcat.domain.model

object ReadLogsPermission {
    const val NAME = "android.permission.READ_LOGS"

    fun grantCommand(packageName: String): String = "adb shell pm grant $packageName $NAME"
}
