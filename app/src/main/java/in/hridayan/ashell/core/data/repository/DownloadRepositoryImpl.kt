package `in`.hridayan.ashell.core.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.domain.model.DownloadState
import `in`.hridayan.ashell.core.domain.repository.DownloadRepository
import `in`.hridayan.ashell.core.utils.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.net.ConnectException
import javax.inject.Inject
import kotlin.apply
import kotlin.collections.forEach
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.use
import kotlin.text.endsWith

class DownloadRepositoryImpl @Inject constructor(
    private val context: Context
) : DownloadRepository {

    private var downloadJob: Job? = null
    private var downloadId: Long = -1L

    override suspend fun downloadApk(
        url: String,
        fileName: String,
        onProgress: (DownloadState) -> Unit
    ) {
        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                checkNetwork()

                onProgress(DownloadState.Started)
                cleanOldApks()

                val file = File(context.getExternalFilesDir(null), fileName)
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadId = dm.enqueue(createRequest(url, file))

                monitorDownload(dm, file, onProgress, isActive)

            } catch (e: CancellationException) {
                cancelDownload()
                onProgress(DownloadState.Cancelled)
            } catch (e: ConnectException) {
                onProgress(DownloadState.Error(e.message ?: "Network error"))
            } catch (e: Exception) {
                onProgress(DownloadState.Error(e.message ?: "Unknown error"))
            }
        }
    }

    override fun cancelDownload() {
        downloadJob?.cancel()
        if (downloadId != -1L) {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.remove(downloadId)
        }
    }

    private fun checkNetwork() {
        if (!isNetworkAvailable(context)) {
            throw ConnectException(context.getString(R.string.network_error))
        }
    }

    private fun cleanOldApks() {
        context.getExternalFilesDir(null)?.listFiles()?.forEach {
            if (it.name.endsWith(".apk")) it.delete()
        }
    }

    private fun createRequest(url: String, file: File): DownloadManager.Request {
        return DownloadManager.Request(url.toUri()).apply {
            setTitle("Downloading update")
            setDescription("aShellYou update is downloading...")
            setDestinationUri(Uri.fromFile(file))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
    }

    private suspend fun monitorDownload(
        dm: DownloadManager,
        file: File,
        onProgress: (DownloadState) -> Unit,
        isActive: Boolean
    ) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        var isDownloading = true

        while (isDownloading && isActive) {
            dm.query(query)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val status =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val downloaded =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            isDownloading = false
                            onProgress(DownloadState.Success(file))
                        }

                        DownloadManager.STATUS_FAILED -> {
                            isDownloading = false
                            onProgress(DownloadState.Error(context.getString(R.string.download_failed)))
                        }

                        else -> {
                            if (total > 0) {
                                val progress = downloaded.toFloat() / total
                                onProgress(DownloadState.Progress(progress))
                            }
                        }
                    }
                }
            }
            delay(100L)
        }
    }
}
