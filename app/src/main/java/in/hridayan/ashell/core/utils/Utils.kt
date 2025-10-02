@file:Suppress("DEPRECATION")

package `in`.hridayan.ashell.core.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.ui.utils.ToastUtils

fun openUrl(url: String, context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())

        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    } catch (ignored: ActivityNotFoundException) {
    }
}

fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val actNw = cm.getNetworkCapabilities(network) ?: return false
    return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/**
 * Check if device is connected to Wi-Fi (even if no internet).
 */
fun Context.isConnectedToWifi(): Boolean {
    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    val wifiInfo = wifiManager?.connectionInfo

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    } else {
        wifiInfo != null && wifiInfo.networkId != -1
    }
}

/**
 * Ask user to enable Wi-Fi.
 * - Android 10+ shows system panel
 * - Below Android 10 tries enabling directly (needs CHANGE_WIFI_STATE permission).
 */
fun Context.askUserToEnableWifi() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
        startActivity(panelIntent)
    } else {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        if (wifiManager != null && !wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
    }
}

/**
 * Check if an app is installed by package name.
 */
fun Context.isAppInstalled(packageName: String): Boolean {
    return packageManager.getLaunchIntentForPackage(packageName) != null
}

fun Context.launchApp(packageName: String) {
    val pm = this.packageManager
    val launchIntent = pm.getLaunchIntentForPackage(packageName)

    if (launchIntent != null) {
        startActivity(launchIntent)
    } else {
        showToast(this, this.getString(R.string.shizuku_not_installed))
        // Optionally redirect to Play Store or GitHub
        // val playStoreIntent = Intent(
        //     Intent.ACTION_VIEW,
        //     Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        // )
        // activity.startActivity(playStoreIntent)
    }
}

fun openDeveloperOptions(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        putExtra(":settings:fragment_args_key", "toggle_adb_wireless")
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        showToast(context, context.getString(R.string.developer_options_not_available))
    }
}


fun showToast(context: Context, message: String) {
    ToastUtils.makeToast(context, message)
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}



