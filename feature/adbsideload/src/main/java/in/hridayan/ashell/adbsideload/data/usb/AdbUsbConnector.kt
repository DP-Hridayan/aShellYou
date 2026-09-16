package `in`.hridayan.ashell.adbsideload.data.usb

import android.hardware.usb.UsbDevice
import com.cgutman.adblib.AdbConnection
import com.cgutman.adblib.AdbCrypto
import com.cgutman.adblib.UsbChannel
import `in`.hridayan.ashell.adbsideload.data.adb.AdbKeyStore
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Opens the ADB interface of a USB device and completes the ADB handshake. Every failure path
 * releases the USB interface so the next attempt starts clean.
 */
@Singleton
class AdbUsbConnector @Inject constructor(
    private val monitor: UsbAdbDeviceMonitor,
    private val keyStore: AdbKeyStore,
) {
    sealed interface ConnectResult {
        class Success(val connection: AdbConnection) : ConnectResult
        class Failure(val error: SideloadError, val detail: String? = null) : ConnectResult
    }

    private sealed interface ChannelResult {
        class Ready(val channel: UsbChannel) : ChannelResult
        class Failure(val error: SideloadError) : ChannelResult
    }

    suspend fun connect(device: UsbDevice): ConnectResult {
        val crypto = keyStore.load() ?: return ConnectResult.Failure(SideloadError.ADB_KEY_UNAVAILABLE)
        return when (val channel = openChannel(device)) {
            is ChannelResult.Ready -> handshake(channel.channel, crypto)
            is ChannelResult.Failure -> ConnectResult.Failure(channel.error)
        }
    }

    private fun openChannel(device: UsbDevice): ChannelResult {
        val adbInterface = monitor.findAdbInterface(device)
            ?: return ChannelResult.Failure(SideloadError.NO_ADB_INTERFACE)
        val usbConnection = monitor.openDevice(device)
            ?: return ChannelResult.Failure(SideloadError.USB_OPEN_FAILED)
        if (!usbConnection.claimInterface(adbInterface, true)) {
            usbConnection.close()
            return ChannelResult.Failure(SideloadError.USB_CLAIM_FAILED)
        }
        return try {
            ChannelResult.Ready(UsbChannel(usbConnection, adbInterface))
        } catch (_: IllegalArgumentException) {
            usbConnection.releaseInterface(adbInterface)
            usbConnection.close()
            ChannelResult.Failure(SideloadError.NO_ADB_INTERFACE)
        }
    }

    private suspend fun handshake(channel: UsbChannel, crypto: AdbCrypto): ConnectResult {
        val connection = AdbConnection.create(channel, crypto)
        var established = false
        try {
            withTimeout(CONNECT_TIMEOUT_MS) {
                runInterruptible(Dispatchers.IO) { connection.connect() }
            }
            established = true
            return ConnectResult.Success(connection)
        } catch (e: TimeoutCancellationException) {
            return ConnectResult.Failure(SideloadError.CONNECTION_TIMED_OUT, e.message)
        } catch (e: IOException) {
            return ConnectResult.Failure(SideloadError.CONNECTION_FAILED, e.message)
        } finally {
            if (!established) runCatching { connection.close() }
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 30_000L
    }
}
