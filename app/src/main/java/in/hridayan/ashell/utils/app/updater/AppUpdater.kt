package `in`.hridayan.ashell.utils.app.updater

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.utils.app.updater.ApkInstaller.installApk
import java.io.File

object AppUpdater {
    @JvmStatic
    fun fetchLatestReleaseAndInstall(
        activity: Activity,
        progressBar: LinearProgressIndicator,
        description: MaterialTextView?,
        loadingDots: LottieAnimationView?,
        downloadButton: Button?
    ) {
        handleViews(
            DownloadStatus.STARTED, activity, progressBar, description, loadingDots, downloadButton
        )

        ReleaseFetcher.fetchLatestRelease(
            object : ReleaseFetcher.ReleaseCallback {
                override fun onReleaseFound(apkUrl: String?, apkFileName: String?) {
                    ApkDownloader.downloadApk(
                        activity,
                        apkUrl.toString(),
                        apkFileName.toString(),
                        progressBar,
                        object : ApkDownloader.DownloadCallback {
                            override fun onDownloadComplete(apkFile: File?) {
                                handleViews(
                                    DownloadStatus.COMPLETED,
                                    activity,
                                    progressBar,
                                    description,
                                    loadingDots,
                                    downloadButton
                                )
                                apkFile?.let { installApk(activity, it) }
                            }

                            override fun onDownloadFailed() {
                                showError(
                                    activity,
                                    progressBar,
                                    description,
                                    loadingDots,
                                    downloadButton
                                )
                            }
                        })
                }

                override fun onError(errorMessage: String?) {
                    showError(activity, progressBar, description, loadingDots, downloadButton)
                }
            })
    }

    private fun showError(
        activity: Activity,
        progressBar: LinearProgressIndicator?,
        description: MaterialTextView?,
        loadingDots: LottieAnimationView?,
        downloadButton: Button?
    ) {
        activity.runOnUiThread(
            Runnable {
                handleViews(
                    DownloadStatus.ERROR,
                    activity,
                    progressBar,
                    description,
                    loadingDots,
                    downloadButton
                )
                Toast.makeText(activity, activity.getString(R.string.failed), Toast.LENGTH_SHORT)
                    .show()
            })
    }

    private fun handleViews(
        status: DownloadStatus,
        activity: Activity,
        progressBar: LinearProgressIndicator?,
        description: MaterialTextView?,
        loadingDots: LottieAnimationView?,
        downloadButton: Button?
    ) {
        when (status) {
            DownloadStatus.STARTED -> {
                if (description != null) description.visibility = View.GONE
                if (downloadButton != null) downloadButton.text = null
                if (loadingDots != null) loadingDots.visibility = View.VISIBLE
                if (progressBar != null) progressBar.visibility = View.VISIBLE
            }

            DownloadStatus.COMPLETED, DownloadStatus.FAILED, DownloadStatus.ERROR -> {
                if (progressBar != null) progressBar.visibility = View.GONE
                if (downloadButton != null) downloadButton.text = activity.getString(R.string.download)
                if (loadingDots != null) loadingDots.setVisibility(View.GONE)
                if (description != null) description.visibility = View.VISIBLE
            }

            else -> return
        }
    }

    enum class DownloadStatus {
        STARTED,
        COMPLETED,
        FAILED,
        ERROR
    }
}
