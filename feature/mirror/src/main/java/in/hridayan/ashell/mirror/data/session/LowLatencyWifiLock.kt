package `in`.hridayan.ashell.mirror.data.session

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val LOCK_TAG = "aShellYou:mirror"

/**
 * Keeps this phone's Wi-Fi out of power save while mirroring over Wireless Debugging. Power save
 * holds packets back to batch them, which adds latency spikes of a hundred milliseconds or more to
 * every video frame and touch.
 */
class LowLatencyWifiLock @Inject constructor(@param:ApplicationContext private val context: Context) {

    suspend fun <T> holdWhile(block: suspend () -> T): T {
        val lock = createLock()
        lock?.acquire()
        try {
            return block()
        } finally {
            if (lock?.isHeld == true) lock.release()
        }
    }

    private fun createLock(): WifiManager.WifiLock? {
        val wifiManager = context.getSystemService(WifiManager::class.java) ?: return null
        return wifiManager.createWifiLock(lockMode(), LOCK_TAG).apply { setReferenceCounted(false) }
    }

    @Suppress("DEPRECATION")
    private fun lockMode(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        WifiManager.WIFI_MODE_FULL_LOW_LATENCY
    } else {
        WifiManager.WIFI_MODE_FULL_HIGH_PERF
    }
}
