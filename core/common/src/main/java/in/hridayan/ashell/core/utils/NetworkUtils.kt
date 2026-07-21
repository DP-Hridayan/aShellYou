package `in`.hridayan.ashell.core.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings

fun registerNetworkCallback(
    context: Context,
    onWifiAvailable: (Boolean) -> Unit
): ConnectivityManager.NetworkCallback {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val isWifi = connectivityManager
                .getNetworkCapabilities(network)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            onWifiAvailable(isWifi)
        }

        override fun onLost(network: Network) {
            onWifiAvailable(false)
        }
    }

    connectivityManager.registerDefaultNetworkCallback(networkCallback)
    return networkCallback
}

fun unregisterNetworkCallback(
    context: Context,
    callback: ConnectivityManager.NetworkCallback
) {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    connectivityManager.unregisterNetworkCallback(callback)
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
    @Suppress("DEPRECATION")
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
            @Suppress("DEPRECATION")
            wifiManager.isWifiEnabled = true
        }
    }
}

