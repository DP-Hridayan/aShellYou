package `in`.hridayan.ashell.shell.common.data.channel

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import `in`.hridayan.ashell.shell.file_browser.data.protocol.LibadbSyncTransport
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncTransport
import io.github.muntashirakon.adb.AdbStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reaches another device over Wireless Debugging. A connection to this phone itself is reported as
 * not connected, because nothing that needs a channel makes sense against the device it runs on.
 */
@Singleton
class WifiAdbDeviceChannel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SyncBackedDeviceChannel() {

    override val isConnected: Boolean
        get() = isManagerConnected() && WifiAdbConnection.currentDevice.value?.isOwnDevice == false

    override suspend fun openRaw(service: String): AdbDuplexStream =
        LibadbDuplexStream(openLibadbStream(service))

    override suspend fun openSyncTransport(service: String): SyncTransport =
        LibadbSyncTransport(openLibadbStream(service))

    /**
     * The library's open waits once without a timeout, so a rejection that lands before the wait
     * starts would park it forever while it holds the manager lock. Interruptible, so the caller's
     * timeout can always get it back.
     */
    private suspend fun openLibadbStream(service: String): AdbStream =
        runInterruptible(Dispatchers.IO) {
            AdbConnectionManager.getInstance(context).openStream(service)
        }

    private fun isManagerConnected(): Boolean =
        runCatching { AdbConnectionManager.getInstance(context).isConnected }.getOrDefault(false)
}
