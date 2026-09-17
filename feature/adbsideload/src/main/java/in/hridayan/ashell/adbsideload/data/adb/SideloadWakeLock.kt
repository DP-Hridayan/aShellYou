package `in`.hridayan.ashell.adbsideload.data.adb

import android.content.Context
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds a partial wake lock while a package is being streamed. A sideload runs for minutes, and
 * letting the CPU sleep stalls the USB transfer until one side times out.
 */
@Singleton
class SideloadWakeLock @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val powerManager = context.getSystemService(PowerManager::class.java)

    private var wakeLock: PowerManager.WakeLock? = null

    @Synchronized
    fun acquire() {
        if (wakeLock?.isHeld == true) return
        val lock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG) ?: return
        lock.setReferenceCounted(false)
        runCatching { lock.acquire(MAX_HOLD_MS) }
        wakeLock = lock
    }

    @Synchronized
    fun release() {
        runCatching { wakeLock?.takeIf { it.isHeld }?.release() }
        wakeLock = null
    }

    private companion object {
        const val TAG = "aShellYou:sideload"
        const val MAX_HOLD_MS = 60L * 60L * 1000L
    }
}
