package `in`.hridayan.ashell.shell.common.data.channel

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import `in`.hridayan.ashell.shell.file_browser.data.protocol.LibadbSyncTransport
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncTransport
import io.github.muntashirakon.adb.AdbStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val isConnected: Boolean
        get() = isManagerConnected() && WifiAdbConnection.currentDevice.value?.isOwnDevice == false

    override val connectionState: StateFlow<ExternalDeviceState> = WifiAdbConnection.currentDevice
        .map { it.toExternalDeviceState() }
        .stateIn(scope, SharingStarted.Eagerly, WifiAdbConnection.currentDevice.value.toExternalDeviceState())

    override suspend fun openRaw(service: String, ioDispatcher: CoroutineDispatcher): AdbDuplexStream =
        LibadbDuplexStream(openLibadbStream(service), ioDispatcher)

    override suspend fun openSyncTransport(service: String): SyncTransport =
        LibadbSyncTransport(openLibadbStream(service))

    /** The library's open blocks until the device answers, so it is interruptible for callers' timeouts. */
    private suspend fun openLibadbStream(service: String): AdbStream =
        runInterruptible(Dispatchers.IO) {
            AdbConnectionManager.getInstance(context).openStream(service)
        }

    private fun WifiAdbDevice?.toExternalDeviceState(): ExternalDeviceState =
        if (this == null || isOwnDevice) {
            ExternalDeviceState.Disconnected
        } else {
            ExternalDeviceState.Connected(deviceId = serialNumber, displayName = deviceName)
        }

    private fun isManagerConnected(): Boolean =
        runCatching { AdbConnectionManager.getInstance(context).isConnected }.getOrDefault(false)
}
