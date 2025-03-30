package `in`.hridayan.ashell.utils.app.updater

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import `in`.hridayan.ashell.config.Preferences
import java.io.File
import androidx.core.net.toUri

object ApkInstaller {
    @JvmStatic
    fun installApk(activity: Activity, apkFile: File) {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            Toast.makeText(
                activity,
                "Download completed, but APK file is missing!",
                Toast.LENGTH_SHORT
            )
                .show()
            return
        }

        Preferences.setUpdateApkFileName(apkFile.name)

        val apkUri =
            FileProvider.getUriForFile(activity, activity.packageName + ".fileprovider", apkFile)
        val installIntent =
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(apkUri, "application/vnd.android.package-archive")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (!activity.packageManager.canRequestPackageInstalls()) {
            Preferences.setUnknownSourcePermAskStatus(true)
            activity.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    ("package:" + activity.packageName).toUri()
                )
            )
            return
        }
        activity.startActivity(installIntent)
    }
}
