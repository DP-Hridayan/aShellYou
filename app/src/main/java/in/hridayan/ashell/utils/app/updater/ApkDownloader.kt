package `in`.hridayan.ashell.utils.app.updater

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.android.material.progressindicator.LinearProgressIndicator
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.ui.ToastUtils
import java.io.File
import androidx.core.net.toUri

object ApkDownloader {
    @JvmStatic
    fun downloadApk(
        activity: Activity,
        url: String,
        fileName: String,
        progressBar: LinearProgressIndicator,
        callback: DownloadCallback
    ) {
        val dir = activity.getExternalFilesDir(null)
        if (dir != null) {
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.name.endsWith(".apk")) {
                        file.delete() // Delete old APK file
                    }
                }
            }
        }

        val apkFile = File(dir, fileName)
        val request =
            DownloadManager.Request(url.toUri())
                .setTitle("Downloading Update")
                .setDescription("Downloading latest APK...")
                .setDestinationUri(Uri.fromFile(apkFile))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager =
            activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        Thread {
            var downloading = true
            val query = DownloadManager.Query().setFilterById(downloadId)
            while (downloading) {
                val cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val status =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val bytesDownloaded =
                        cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
                            )
                        )
                    val totalBytes =
                        cursor.getInt(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        )

                    if (totalBytes > 0) {
                        val progress = ((bytesDownloaded * 100L) / totalBytes).toInt()
                        Handler(Looper.getMainLooper())
                            .post { progressBar.setProgressCompat(progress, true) }
                    }

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false
                        cursor.close()
                        Handler(Looper.getMainLooper())
                            .post { callback.onDownloadComplete(apkFile) }
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        downloading = false
                        cursor.close()
                        Handler(Looper.getMainLooper())
                            .post {
                                callback.onDownloadFailed()
                                ToastUtils.showToast(
                                    activity,
                                    activity.getString(R.string.failed),
                                    ToastUtils.LENGTH_SHORT
                                )
                            }
                    }
                }
                try {
                    Thread.sleep(100)
                } catch (ignored: InterruptedException) {
                }
            }
        }
            .start()
    }

    interface DownloadCallback {
        fun onDownloadComplete(apkFile: File?)

        fun onDownloadFailed()
    }
}
