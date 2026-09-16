package `in`.hridayan.ashell.adbsideload.data.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Watches USB host events for devices exposing an ADB interface and brokers the runtime USB
 * permission. Receivers are registered once for the life of the process, so the state keeps
 * updating no matter how many times the sideload screen is opened and closed.
 */
@Singleton
class UsbAdbDeviceMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    interface Listener {
        fun onDeviceAttached(device: UsbDevice, hasPermission: Boolean)
        fun onDeviceDetached(device: UsbDevice)
        fun onPermissionResult(device: UsbDevice, granted: Boolean)
    }

    val usbManager: UsbManager? = context.getSystemService(UsbManager::class.java)

    private val registered = AtomicBoolean(false)

    @Volatile
    private var listener: Listener? = null

    @Volatile
    private var pendingPermissionDevice: UsbDevice? = null

    @Volatile
    private var lastPermissionRequestAt = 0L

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PERMISSION_ACTION) handlePermissionResult(intent)
        }
    }

    private val attachmentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val device = intent?.let(::extractDevice) ?: return
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> notifyAttached(device)
                UsbManager.ACTION_USB_DEVICE_DETACHED -> listener?.onDeviceDetached(device)
            }
        }
    }

    fun start(listener: Listener) {
        this.listener = listener
        if (registered.compareAndSet(false, true)) registerReceivers()
    }

    fun findAdbDevice(): UsbDevice? = usbManager?.deviceList?.values?.firstOrNull(::isAdbDevice)

    fun hasPermission(device: UsbDevice): Boolean = usbManager?.hasPermission(device) == true

    fun openDevice(device: UsbDevice): UsbDeviceConnection? = usbManager?.openDevice(device)

    fun isAdbDevice(device: UsbDevice): Boolean = findAdbInterface(device) != null

    fun findAdbInterface(device: UsbDevice): UsbInterface? =
        (0 until device.interfaceCount).map(device::getInterface).firstOrNull(::isAdbInterface)

    fun requestPermission(device: UsbDevice) {
        val manager = usbManager ?: return
        if (manager.hasPermission(device)) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastPermissionRequestAt < PERMISSION_COOLDOWN_MS) return
        lastPermissionRequestAt = now
        pendingPermissionDevice = device
        val intent = Intent(PERMISSION_ACTION).setPackage(context.packageName)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            PERMISSION_REQUEST_CODE,
            intent,
            mutableFlag() or PendingIntent.FLAG_CANCEL_CURRENT
        )
        manager.requestPermission(device, pendingIntent)
    }

    /** Lets an explicit user retry ask again without waiting out the cooldown. */
    fun clearPermissionCooldown() {
        lastPermissionRequestAt = 0L
    }

    private fun mutableFlag(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0

    private fun notifyAttached(device: UsbDevice) {
        if (isAdbDevice(device)) listener?.onDeviceAttached(device, hasPermission(device))
    }

    private fun handlePermissionResult(intent: Intent) {
        val device = extractDevice(intent) ?: pendingPermissionDevice ?: return
        pendingPermissionDevice = null
        lastPermissionRequestAt = 0L
        val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) ||
            hasPermission(device)
        listener?.onPermissionResult(device, granted)
    }

    private fun registerReceivers() {
        ContextCompat.registerReceiver(
            context,
            permissionReceiver,
            IntentFilter(PERMISSION_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        val attachmentFilter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        ContextCompat.registerReceiver(
            context,
            attachmentReceiver,
            attachmentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun extractDevice(intent: Intent): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }

    private fun isAdbInterface(intf: UsbInterface): Boolean =
        intf.interfaceClass == UsbConstants.USB_CLASS_VENDOR_SPEC &&
            intf.interfaceSubclass == ADB_INTERFACE_SUBCLASS &&
            intf.interfaceProtocol == ADB_INTERFACE_PROTOCOL

    private companion object {
        const val PERMISSION_ACTION = "in.hridayan.ashell.SIDELOAD_USB_PERMISSION"
        const val PERMISSION_REQUEST_CODE = 0
        const val PERMISSION_COOLDOWN_MS = 20_000L
        const val ADB_INTERFACE_SUBCLASS = 66
        const val ADB_INTERFACE_PROTOCOL = 1
    }
}

fun UsbDevice.displayName(): String = productName ?: manufacturerName ?: deviceName
