package `in`.hridayan.ashell.settings.data.repository

import android.content.Context
import android.util.Log
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.isNetworkAvailable
import `in`.hridayan.ashell.settings.domain.model.DownloadState
import `in`.hridayan.ashell.settings.domain.repository.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class DownloadRepositoryImpl @Inject constructor(
    private val context: Context
) : DownloadRepository {

    private var downloadJob: Job? = null
    private var activeConnection: HttpURLConnection? = null

    @Volatile
    private var cancelled = false

    override suspend fun downloadApk(
        url: String,
        fileName: String,
        onProgress: (DownloadState) -> Unit
    ) {
        cancelled = false
        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                checkNetwork()

                onProgress(DownloadState.Started)
                cleanOldApks()

                val file = File(context.getExternalFilesDir(null), fileName)
                downloadFile(url, file, onProgress)
            } catch (e: CancellationException) {
                onProgress(DownloadState.Cancelled)
            } catch (e: ConnectException) {
                if (cancelled) {
                    onProgress(DownloadState.Cancelled)
                } else {
                    Log.e(TAG, "Network error during download", e)
                    onProgress(DownloadState.Error(e.message ?: "Network error"))
                }
            } catch (e: Exception) {
                if (cancelled) {
                    onProgress(DownloadState.Cancelled)
                } else {
                    Log.e(TAG, "Error during download", e)
                    onProgress(DownloadState.Error(e.message ?: "Unknown error"))
                }
            }
        }
    }

    override fun cancelDownload() {
        cancelled = true
        // Disconnect first — this interrupts the blocking input.read() immediately
        activeConnection?.disconnect()
        activeConnection = null
        downloadJob?.cancel()
        downloadJob = null
        cleanOldApks()
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

    private fun downloadFile(
        url: String,
        file: File,
        onProgress: (DownloadState) -> Unit
    ) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = true
            }
            activeConnection = connection

            val responseCode = connection.responseCode

            if (responseCode != HttpURLConnection.HTTP_OK) {
                onProgress(
                    DownloadState.Error(
                        context.getString(R.string.download_failed) + " (HTTP $responseCode)"
                    )
                )
                return
            }

            val totalBytes = connection.contentLength.toLong()

            connection.inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesDownloaded = 0L
                    var lastProgressUpdate = 0L

                    while (true) {
                        val bytesRead = input.read(buffer)
                        if (bytesRead == -1) break

                        output.write(buffer, 0, bytesRead)
                        bytesDownloaded += bytesRead

                        // Throttle progress updates to avoid overwhelming the UI
                        val now = System.currentTimeMillis()
                        if (totalBytes > 0 && now - lastProgressUpdate > 100) {
                            val progress = bytesDownloaded.toFloat() / totalBytes
                            onProgress(DownloadState.Progress(progress))
                            lastProgressUpdate = now
                        }
                    }
                }
            }

            onProgress(DownloadState.Success(file))
        } finally {
            activeConnection = null
            connection?.disconnect()
        }
    }

    companion object {
        private const val TAG = "DownloadRepo"
    }
}

