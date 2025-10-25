package `in`.hridayan.ashell.shell.otg_adb_shell.data.repository

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.model.OtgState
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.repository.OtgRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OtgRepositoryImpl(private val context: Context) : OtgRepository {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private val _state = MutableStateFlow<OtgState>(OtgState.Idle)
    override val state: StateFlow<OtgState> = _state.asStateFlow()

    private val permissionAction = "in.hridayan.ashell.USB_PERMISSION"

    private var currentDevice: UsbDevice? = null

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != permissionAction) return

            val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            }

            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

            if (granted && device != null) {
                connectToDevice(device)
            } else {
                _state.value = OtgState.PermissionDenied
            }
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return

            val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            }

            if (device == null) return

            when (action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> handleDeviceAttach(device)
                UsbManager.ACTION_USB_DEVICE_DETACHED -> handleDeviceDetach(device)
            }
        }
    }


    init {
        ContextCompat.registerReceiver(
            context, permissionReceiver,
            IntentFilter(permissionAction), ContextCompat.RECEIVER_NOT_EXPORTED
        )
        val usbFilter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        ContextCompat.registerReceiver(
            context, usbReceiver, usbFilter, ContextCompat.RECEIVER_NOT_EXPORTED
        )

        checkConnectedDevices()
    }

    private fun handleDeviceAttach(device: UsbDevice) {
        currentDevice = device
        if (isAdbDevice(device)) {
            val friendlyName = device.productName ?: device.manufacturerName ?: device.deviceName
            if (usbManager.hasPermission(device)) {
                connectToDevice(device)
            } else {
                _state.value = OtgState.DeviceFound(friendlyName)
                requestPermission(device)
            }
        } else {
            _state.value =
                OtgState.Error(context.getString(R.string.no_adb_device_error))
        }
    }

    private fun handleDeviceDetach(device: UsbDevice) {
        if (device == currentDevice) {
            currentDevice = null
            _state.value = OtgState.Disconnected
            _state.value = OtgState.Idle
        }
    }

    override fun searchDevices() {
        _state.value = OtgState.Searching
        checkConnectedDevices()
    }

    private fun checkConnectedDevices() {
        val devices = usbManager.deviceList.values
        if (devices.isEmpty()) {
            _state.value = OtgState.Idle
            return
        }

        val adbDevice = devices.firstOrNull { isAdbDevice(it) }
        if (adbDevice != null) {
            handleDeviceAttach(adbDevice)
        } else {
            _state.value =
                OtgState.Error(context.getString(R.string.no_adb_device_error))
        }
    }

    private fun requestPermission(device: UsbDevice) {
        if (usbManager.hasPermission(device)) return

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(permissionAction), PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, pendingIntent)
    }

    private fun connectToDevice(device: UsbDevice) {
        _state.value = OtgState.Connecting
        val connection = usbManager.openDevice(device)
        if (connection != null) {
            val friendlyName = device.productName ?: device.manufacturerName ?: device.deviceName
            _state.value = OtgState.Connected(friendlyName)
            // TODO: Pass this connection to your ADB executor layer
        } else {
            _state.value = OtgState.Error(context.getString(R.string.failed_to_open_usb_connection))
        }
    }


    override fun disconnect() {
        currentDevice = null
        _state.value = OtgState.Disconnected
        _state.value = OtgState.Idle
    }

    override fun unRegister() {
        try {
            context.unregisterReceiver(permissionReceiver)
            context.unregisterReceiver(usbReceiver)
        } catch (_: Exception) {
        }
    }

    private fun isAdbDevice(device: UsbDevice): Boolean {
        for (i in 0 until device.interfaceCount) {
            val intf = device.getInterface(i)
            if (intf.interfaceClass == 255 &&
                intf.interfaceSubclass == 66 &&
                intf.interfaceProtocol == 1
            ) return true
        }
        return false
    }
}
