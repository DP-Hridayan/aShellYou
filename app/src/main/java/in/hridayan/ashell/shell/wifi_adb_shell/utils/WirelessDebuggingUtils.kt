package `in`.hridayan.ashell.shell.wifi_adb_shell.utils

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.shell.wifi_adb_shell.utils.WirelessDebuggingUtils.grantWriteSecureSettingsViaAdb
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Helper object for wireless debugging operations.
 *
 * After first successful own-device connection via wireless debugging,
 * [grantWriteSecureSettingsViaAdb] should be called to enable
 * programmatic wireless debugging toggle for future reconnects.
 */
object WirelessDebuggingUtils {

    private const val ADB_WIFI_ENABLED = "adb_wifi_enabled"
    private const val SETTINGS_GLOBAL_URI = "content://settings/global"
    private const val PERM_WRITE_SECURE_SETTINGS = "android.permission.WRITE_SECURE_SETTINGS"
    private const val TAG = "WirelessDebuggingHelper"

    /**
     * Opens the Wireless Debugging settings page directly via SubSettings intent.
     * This triggers the "Allow wireless debugging on this network?" system popup
     * when the user enables it, useful for reconnecting own device without manually
     * navigating through Developer Options.
     *
     * Only available on Android 11 (API 30) and above. Falls back to Developer Options
     * on older versions or if the intent fails.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun openWirelessDebuggingSettings(context: Context) {
        try {
            // Try to open WirelessDebuggingFragment directly via SubSettings
            val intent = Intent().apply {
                component = ComponentName(
                    "com.android.settings",
                    "com.android.settings.SubSettings"
                )
                putExtra(
                    ":settings:show_fragment",
                    "com.android.settings.development.WirelessDebuggingFragment"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())

            // We try to auto scroll to the wireless debugging section
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(":settings:fragment_args_key", "toggle_adb_wireless")
            }

            openDeveloperOptions(context, intent)
        }
    }

    fun openDeveloperOptions(
        context: Context,
        intent: Intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
    ) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showToast(context, context.getString(R.string.developer_options_not_available))
        }
    }

    /**
     * Enables wireless debugging programmatically using WRITE_SECURE_SETTINGS permission.
     *
     * @return true if wireless debugging was enabled successfully, false otherwise
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun enableWirelessDebugging(context: Context): Boolean {
        if (!hasWriteSecureSettingsPermission(context)) {
            return false
        }

        return try {
            if (isWirelessDebuggingEnabled(context)) {
                return true
            }

            val devSettingsEnabled = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) != 0

            if (!devSettingsEnabled) {
                val devValues = ContentValues(2).apply {
                    put("name", Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)
                    put("value", 1)
                }
                context.contentResolver.insert(SETTINGS_GLOBAL_URI.toUri(), devValues)
            }

            val wifiAdbValues = ContentValues(2).apply {
                put("name", ADB_WIFI_ENABLED)
                put("value", 1)
            }
            context.contentResolver.insert(SETTINGS_GLOBAL_URI.toUri(), wifiAdbValues)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun ensureWirelessDebuggingAndReconnect(
        context: Context,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) {
        val requested = enableWirelessDebugging(context)

        if (!requested) {
            openWirelessDebuggingSettings(context)
        }

        waitForWirelessDebuggingState(context) { enabled ->
            if (enabled) {
                // Add delay to allow Android to:
                // 1. Fully start the ADB daemon on the new port
                // 2. Broadcast fresh mDNS records
                // Without this delay, reconnect may use stale cached port info
                Handler(Looper.getMainLooper()).postDelayed({
                    onSuccess()
                }, 1500)
            } else {
                Log.d("WirelessDebugging", "User did not enable wireless debugging")
                onFailed()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun waitForWirelessDebuggingState(
        context: Context,
        timeoutMs: Long = 10_000,
        pollIntervalMs: Long = 300,
        onResult: (Boolean) -> Unit
    ) {
        val startTime = System.currentTimeMillis()
        val handler = Handler(Looper.getMainLooper())

        fun poll() {
            if (isWirelessDebuggingEnabled(context)) {
                onResult(true)
                return
            }

            if (System.currentTimeMillis() - startTime >= timeoutMs) {
                onResult(false)
                return
            }

            handler.postDelayed({ poll() }, pollIntervalMs)
        }

        poll()
    }

    /**
     * Grants WRITE_SECURE_SETTINGS permission to this app via an active ADB connection.
     * Should be called after successful own-device connection.
     *
     * @param context Application context
     * @param adbConnectionManager The active ADB connection manager
     * @return true if permission was granted successfully, false otherwise
     */
    fun grantWriteSecureSettingsViaAdb(
        context: Context,
        adbConnectionManager: AbsAdbConnectionManager
    ): Boolean {
        if (!adbConnectionManager.isConnected) {
            return false
        }

        return try {
            val command = getGrantPermissionCommand(context, PERM_WRITE_SECURE_SETTINGS)
            val stream = adbConnectionManager.openStream("shell:$command")

            val output = StringBuilder()
            try {
                val input = stream.openInputStream()
                val reader = BufferedReader(InputStreamReader(input))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.appendLine(line)
                }
                reader.close()
            } catch (e: IOException) {
                Log.d(TAG, "Stream closed - command completed")
            }

            try {
                stream.close()
            } catch (_: Exception) {
            }

            Thread.sleep(100)

            val granted = hasWriteSecureSettingsPermission(context)
            val outputStr = output.toString().trim()
            Log.d(
                TAG, "Permission grant result: $granted" +
                        if (outputStr.isNotEmpty()) ", output: $outputStr" else ""
            )
            granted
        } catch (e: Exception) {
            Log.e(TAG, "Failed to grant WRITE_SECURE_SETTINGS via ADB", e)
            false
        }
    }

    /**
     * Checks if wireless debugging is currently enabled.
     * Only available on Android 11+.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun isWirelessDebuggingEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, ADB_WIFI_ENABLED, 0) != 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if WRITE_SECURE_SETTINGS permission is granted.
     */
    fun hasWriteSecureSettingsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_SECURE_SETTINGS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns the ADB command to grant WRITE_SECURE_SETTINGS permission to this app.
     */
    fun getGrantPermissionCommand(context: Context, permission: String): String {
        return "pm grant ${context.packageName} $permission"
    }
}