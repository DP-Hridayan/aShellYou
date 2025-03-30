package `in`.hridayan.ashell.utils.app.updater

import android.util.Log
import `in`.hridayan.ashell.config.Const
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ReleaseFetcher {
    private const val GITHUB_API_URL = ("https://api.github.com/repos/"
            + Const.GITHUB_OWNER
            + "/"
            + Const.GITHUB_REPOSITORY
            + "/releases/latest")

    fun fetchLatestRelease(callback: ReleaseCallback) {
        Thread(
            Runnable {
                try {
                    val connection =
                        URL(GITHUB_API_URL).openConnection() as HttpURLConnection
                    connection.setRequestMethod("GET")
                    connection.connect()

                    val response = StringBuilder()
                    connection.getInputStream().use { inputStream ->
                        val buffer = ByteArray(1024)
                        var bytesRead: Int
                        while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                            response.append(String(buffer, 0, bytesRead))
                        }
                    }
                    val jsonResponse = JSONObject(response.toString())
                    val assets = jsonResponse.getJSONArray("assets")

                    var apkUrl: String? = null
                    var apkFileName: String? = null
                    for (i in 0..<assets.length()) {
                        val asset = assets.getJSONObject(i)
                        if (asset.getString("name").endsWith(".apk")) {
                            apkFileName = asset.getString("name")
                            apkUrl = asset.getString("browser_download_url")
                            break
                        }
                    }

                    if (apkUrl == null) throw Exception("No APK file found.")
                    callback.onReleaseFound(apkUrl, apkFileName)
                } catch (e: Exception) {
                    Log.e("ReleaseFetcher", "Error fetching update", e)
                    callback.onError("Failed: " + e.message)
                }
            })
            .start()
    }

    interface ReleaseCallback {
        fun onReleaseFound(apkUrl: String?, apkFileName: String?)

        fun onError(errorMessage: String?)
    }
}
