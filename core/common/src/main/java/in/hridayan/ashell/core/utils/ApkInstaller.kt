package `in`.hridayan.ashell.core.utils

import android.app.Activity
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun Activity.installApk(file: File) {
    val apkUri = FileProvider.getUriForFile(
        this,
        "$packageName.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    startActivity(intent)
}
