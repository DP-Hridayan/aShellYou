package `in`.hridayan.ashell.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.config.Preferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object DeviceUtils {
    private var savedVersionCode: Int = 0

    @JvmStatic
    val deviceDetails: String
        // Method for getting required device details for crash report
        get() = ("""
     
     Brand : ${Build.BRAND}
     Device : ${Build.DEVICE}
     Model : ${Build.MODEL}
     Product : ${Build.PRODUCT}
     SDK : ${Build.VERSION.SDK_INT}
     Release : ${Build.VERSION.RELEASE}
     App version name : ${BuildConfig.VERSION_NAME}
     App version code : ${BuildConfig.VERSION_CODE}
     """.trimIndent())

    // returns android sdk version
    @JvmStatic
    fun androidVersion(): Int {
        return Build.VERSION.SDK_INT
    }

    @JvmStatic
    val deviceName: String
        // returns device model name
        get() = Build.MODEL

    // returns current (installed) app version code
    @JvmStatic
    fun currentVersion(): Int {
        return BuildConfig.VERSION_CODE
    }

    /* returns if app has just been updated , used to perform certain things after opening the app after an update */
    @JvmStatic
    fun isAppUpdated(context: Context?): Boolean {
        savedVersionCode = Preferences.getSavedVersionCode()
        return savedVersionCode != currentVersion() && savedVersionCode != 1
    }

    /* Compare current app version code with the one retrieved from github to see if update available */
    @JvmStatic
    fun isUpdateAvailable(latestVersionCode: Int): Boolean {
        return BuildConfig.VERSION_CODE < latestVersionCode
    }

    // Extracts the version code from the build.gradle file retrieved and converts it to integer
    @JvmStatic
    fun extractVersionCode(text: String): Int {
        val pattern = Pattern.compile("versionCode\\s+(\\d+)")
        val matcher = pattern.matcher(text)

        if (matcher.find()) {
            return try {
                matcher.group(1)?.toInt() ?: -1
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                -1
            }
        }
        return -1
    }

    // Extracts the version name from the build.gradle file retrieved and converts it to string
    @JvmStatic
    fun extractVersionName(text: String): String? {
        val pattern = Pattern.compile("versionName\\s*\"([^\"]*)\"")
        val matcher = pattern.matcher(text)
        if (matcher.find()) return matcher.group(1)

        return ""
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @JvmStatic
    val currentDateTime: String
        /*Using this function to create unique file names for the saved txt files as there are methods which tries to open files based on its name */
        get() {
            // Thread-safe date format
            val threadLocalDateFormat = ThreadLocal<SimpleDateFormat>().apply {
                set(SimpleDateFormat("_yyyyMMddHHmmss", Locale.getDefault()))
            }

            // Get the current date and time
            val now = Date()

            // Format the date and time using the thread-local formatter
            return threadLocalDateFormat.get().format(now)
        }

    interface FetchLatestVersionCodeCallback {
        fun onResult(result: Int)
    }
}
