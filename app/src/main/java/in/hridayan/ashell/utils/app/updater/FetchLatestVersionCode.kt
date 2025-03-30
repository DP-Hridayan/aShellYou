package `in`.hridayan.ashell.utils.app.updater

import android.os.Handler
import android.os.Looper
import `in`.hridayan.ashell.config.Const
import `in`.hridayan.ashell.config.Preferences
import `in`.hridayan.ashell.utils.DeviceUtils
import `in`.hridayan.ashell.utils.DeviceUtils.extractVersionCode
import `in`.hridayan.ashell.utils.DeviceUtils.extractVersionName
import `in`.hridayan.ashell.utils.DeviceUtils.isUpdateAvailable
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FetchLatestVersionCode(
    callback: DeviceUtils.FetchLatestVersionCodeCallback?
) {
    private val callback: DeviceUtils.FetchLatestVersionCodeCallback?
    private val executor: ExecutorService
    private val handler: Handler

    init {
        this.callback = callback
        this.executor = Executors.newSingleThreadExecutor()
        this.handler = Handler(Looper.getMainLooper())
    }

    fun execute(url: String?) {
        executor.execute(
            Runnable {
                val result = fetchVersionCode(url)
                handler.post(
                    Runnable {
                        if (callback != null) {
                            this.callback.onResult(result)
                        }
                    })
            })
    }

    private fun fetchVersionCode(urlString: String?): Int {
        val result = StringBuilder()
        try {
            val url = URL(urlString)
            val urlConnection = url.openConnection() as HttpURLConnection
            try {
                BufferedReader(InputStreamReader(urlConnection.getInputStream())).use { reader ->
                    var line: String?
                    while ((reader.readLine().also { line = it }) != null) {
                        result.append(line).append("\n")
                    }
                }
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Const.CONNECTION_ERROR // Error occurred
        }

        val latestVersionCode = extractVersionCode(result.toString())
        if (isUpdateAvailable(latestVersionCode)) {
            Preferences.setLatestVersionName(extractVersionName(result.toString()))
            return Const.UPDATE_AVAILABLE // Update available
        } else {
            return Const.UPDATE_NOT_AVAILABLE // No update available
        }
    }
}
