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
 *
 * Note: This manager does NOT hold a static reference to Context.
 * Context is passed per-call to avoid memory leaks.
 */
object FastbootDeviceManager {
    private const val TAG = "FastbootDeviceManager"
    private const val ACTION_USB_PERMISSION = "in.hridayan.fastboot.USB_PERMISSION"

    /** Fastboot USB interface identifiers */
    private const val FASTBOOT_INTERFACE_CLASS = 0xFF    // Vendor Specific
    private const val FASTBOOT_INTERFACE_SUBCLASS = 0x42 // Android Fastboot
    private const val FASTBOOT_INTERFACE_PROTOCOL = 0x03 // Fastboot Protocol

    private val listeners = mutableListOf<FastbootDeviceManagerListener>()
    private val connectedDevices = mutableMapOf<DeviceId, FastbootDeviceContext>()
    private var isRegistered = false

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_USB_PERMISSION) return
            val device = getUsbDeviceFromIntent(intent) ?: return
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            val usbManager = context?.getSystemService(Context.USB_SERVICE) as? UsbManager

            if (granted && usbManager != null) {
                connectToUsbDevice(usbManager, device)
            } else {
                Log.w(TAG, "USB permission denied for ${device.deviceName}")
            }
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
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
     * Register USB broadcast receivers. Call with application context.
     * @param context Application context (not stored)
     */
    fun registerReceivers(context: Context) {
        val appContext = context.applicationContext
        if (isRegistered) return

        ContextCompat.registerReceiver(
            appContext, permissionReceiver,
            IntentFilter(ACTION_USB_PERMISSION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val usbFilter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        ContextCompat.registerReceiver(
            appContext, usbReceiver, usbFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        isRegistered = true
    }

    /**
     * Unregister USB broadcast receivers.
     * @param context Application context (not stored)
     */
    fun unregisterReceivers(context: Context) {
        val appContext = context.applicationContext
        if (!isRegistered) return
        try {
            appContext.unregisterReceiver(permissionReceiver)
            appContext.unregisterReceiver(usbReceiver)
        } catch (_: Exception) {}
        isRegistered = false
    }

    /**
     * Get IDs of all physically attached fastboot devices.
     * @param context Used to access UsbManager (not stored)
     */
    fun getAttachedDeviceIds(context: Context): List<DeviceId> {
        val manager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
            ?: return emptyList()
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
     * @param context Used to access UsbManager (not stored)
     */
    fun connectToDevice(context: Context, deviceId: DeviceId) {
        val manager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return
        val device = manager.deviceList.values.firstOrNull { it.deviceName == deviceId.id } ?: run {
            Log.e(TAG, "Device not found: $deviceId")
            return
        }

        if (manager.hasPermission(device)) {
            connectToUsbDevice(manager, device)
        } else {
            requestPermission(context, device)
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
     * @param context Used to unregister receivers (not stored)
     */
    fun release(context: Context) {
        connectedDevices.values.forEach { it.close() }
        connectedDevices.clear()
        unregisterReceivers(context)
    }

    // --- Internal ---

    private fun requestPermission(context: Context, device: UsbDevice) {
        val appContext = context.applicationContext
        val manager = appContext.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return
        val pendingIntent = PendingIntent.getBroadcast(
            appContext, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
        )
        manager.requestPermission(device, pendingIntent)
    }

    private fun connectToUsbDevice(manager: UsbManager, device: UsbDevice) {
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
