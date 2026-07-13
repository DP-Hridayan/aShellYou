package `in`.hridayan.fastboot

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Manages discovery and connection to fastboot devices over USB.
 *
 * Fastboot devices are identified by their USB interface descriptors:
 * - Interface Class: 0xFF (Vendor Specific)
 * - Interface SubClass: 0x42
 * - Interface Protocol: 0x03
 */
object FastbootDeviceManager {
    private const val TAG = "FastbootDeviceManager"
    private const val ACTION_USB_PERMISSION = "com.google.android.fastbootmobile.USB_PERMISSION"

    /** Fastboot USB interface identifiers */
    private const val FASTBOOT_INTERFACE_CLASS = 0xFF    // Vendor Specific
    private const val FASTBOOT_INTERFACE_SUBCLASS = 0x42 // Android Fastboot
    private const val FASTBOOT_INTERFACE_PROTOCOL = 0x03 // Fastboot Protocol

    private var context: Context? = null
    private var usbManager: UsbManager? = null
    private val listeners = mutableListOf<FastbootDeviceManagerListener>()
    private val connectedDevices = mutableMapOf<DeviceId, FastbootDeviceContext>()
    private var isRegistered = false

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action != ACTION_USB_PERMISSION) return
            val device = getUsbDeviceFromIntent(intent) ?: return
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

            if (granted) {
                connectToUsbDevice(device)
            } else {
                Log.w(TAG, "USB permission denied for ${device.deviceName}")
            }
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val device = getUsbDeviceFromIntent(intent ?: return) ?: return
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    if (isFastbootDevice(device)) {
                        val deviceId = DeviceId(device.deviceName)
                        listeners.forEach { it.onFastbootDeviceAttached(deviceId) }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val deviceId = DeviceId(device.deviceName)
                    connectedDevices.remove(deviceId)?.close()
                    listeners.forEach { it.onFastbootDeviceDetached(deviceId) }
                }
            }
        }
    }

    /**
     * Initialize the manager with a Context. Must be called before any other method.
     */
    fun init(context: Context) {
        this.context = context.applicationContext
        this.usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
        registerReceivers()
    }

    /**
     * Get IDs of all physically attached fastboot devices.
     */
    fun getAttachedDeviceIds(): List<DeviceId> {
        val manager = usbManager ?: return emptyList()
        return manager.deviceList.values
            .filter { isFastbootDevice(it) }
            .map { DeviceId(it.deviceName) }
    }

    /**
     * Get IDs of devices with active connections.
     */
    fun getConnectedDeviceIds(): List<DeviceId> = connectedDevices.keys.toList()

    /**
     * Initiate a connection to a fastboot device.
     * On success, [FastbootDeviceManagerListener.onFastbootDeviceConnected] will be called.
     */
    fun connectToDevice(deviceId: DeviceId) {
        val manager = usbManager ?: return
        val device = manager.deviceList.values.firstOrNull { it.deviceName == deviceId.id } ?: run {
            Log.e(TAG, "Device not found: $deviceId")
            return
        }

        if (manager.hasPermission(device)) {
            connectToUsbDevice(device)
        } else {
            requestPermission(device)
        }
    }

    /**
     * Disconnect from a device.
     */
    fun disconnectDevice(deviceId: DeviceId) {
        connectedDevices.remove(deviceId)?.close()
        listeners.forEach { it.onFastbootDeviceDisconnected(deviceId) }
    }

    /**
     * Get the device context for an already-connected device.
     */
    fun getDeviceContext(deviceId: DeviceId): FastbootDeviceContext? = connectedDevices[deviceId]

    fun addFastbootDeviceManagerListener(listener: FastbootDeviceManagerListener) {
        listeners.add(listener)
    }

    fun removeFastbootDeviceManagerListener(listener: FastbootDeviceManagerListener) {
        listeners.remove(listener)
    }

    /**
     * Release all resources. Call when the manager is no longer needed.
     */
    fun release() {
        connectedDevices.values.forEach { it.close() }
        connectedDevices.clear()
        unregisterReceivers()
        context = null
        usbManager = null
    }

    // --- Internal ---

    private fun registerReceivers() {
        val ctx = context ?: return
        if (isRegistered) return

        ContextCompat.registerReceiver(
            ctx, permissionReceiver,
            IntentFilter(ACTION_USB_PERMISSION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val usbFilter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        ContextCompat.registerReceiver(
            ctx, usbReceiver, usbFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        isRegistered = true
    }

    private fun unregisterReceivers() {
        val ctx = context ?: return
        if (!isRegistered) return
        try {
            ctx.unregisterReceiver(permissionReceiver)
            ctx.unregisterReceiver(usbReceiver)
        } catch (_: Exception) {}
        isRegistered = false
    }

    private fun requestPermission(device: UsbDevice) {
        val ctx = context ?: return
        val manager = usbManager ?: return
        val pendingIntent = PendingIntent.getBroadcast(
            ctx, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
        )
        manager.requestPermission(device, pendingIntent)
    }

    private fun connectToUsbDevice(device: UsbDevice) {
        val manager = usbManager ?: return
        val intf = findFastbootInterface(device) ?: run {
            Log.e(TAG, "No fastboot interface found on ${device.deviceName}")
            return
        }

        val connection = manager.openDevice(device) ?: run {
            Log.e(TAG, "Failed to open USB device ${device.deviceName}")
            return
        }

        if (!connection.claimInterface(intf, true)) {
            Log.e(TAG, "Failed to claim interface on ${device.deviceName}")
            connection.close()
            return
        }

        // Find bulk IN and OUT endpoints
        var inEndpoint: UsbEndpoint? = null
        var outEndpoint: UsbEndpoint? = null

        for (i in 0 until intf.endpointCount) {
            val ep = intf.getEndpoint(i)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.direction == UsbConstants.USB_DIR_IN) {
                    inEndpoint = ep
                } else {
                    outEndpoint = ep
                }
            }
        }

        if (inEndpoint == null || outEndpoint == null) {
            Log.e(TAG, "Could not find bulk endpoints on ${device.deviceName}")
            connection.releaseInterface(intf)
            connection.close()
            return
        }

        val deviceId = DeviceId(device.deviceName)
        val deviceContext = FastbootDeviceContext(connection, intf, inEndpoint, outEndpoint)
        connectedDevices[deviceId] = deviceContext

        listeners.forEach { it.onFastbootDeviceConnected(deviceId, deviceContext) }
        Log.d(TAG, "Connected to fastboot device: $deviceId")
    }

    private fun findFastbootInterface(device: UsbDevice): UsbInterface? {
        for (i in 0 until device.interfaceCount) {
            val intf = device.getInterface(i)
            if (isFastbootInterface(intf)) return intf
        }
        return null
    }

    internal fun isFastbootDevice(device: UsbDevice): Boolean {
        for (i in 0 until device.interfaceCount) {
            if (isFastbootInterface(device.getInterface(i))) return true
        }
        return false
    }

    private fun isFastbootInterface(intf: UsbInterface): Boolean =
        intf.interfaceClass == FASTBOOT_INTERFACE_CLASS &&
        intf.interfaceSubclass == FASTBOOT_INTERFACE_SUBCLASS &&
        intf.interfaceProtocol == FASTBOOT_INTERFACE_PROTOCOL

    private fun getUsbDeviceFromIntent(intent: Intent): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }
}
