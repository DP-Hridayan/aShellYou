package `in`.hridayan.ashell.shell.otg_adb_shell.data.repository

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import com.cgutman.adblib.AdbBase64
import com.cgutman.adblib.AdbConnection
import com.cgutman.adblib.AdbCrypto
import com.cgutman.adblib.AdbStream
import com.cgutman.adblib.UsbChannel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.shell.common.domain.model.OutputLine
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.model.OtgConnection
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.model.OtgState
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.repository.OtgRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Singleton

@Singleton
class OtgRepositoryImpl(private val context: Context) : OtgRepository {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private val permissionAction = "in.hridayan.ashell.USB_PERMISSION"

    private var currentDevice: UsbDevice? = null
    private var adbConnection: AdbConnection? = null
    private var adbCrypto: AdbCrypto? = null

    private var adbStream: AdbStream? = null

    // region Receivers
    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != permissionAction) return
            val device = getUsbDeviceFromIntent(intent) ?: return
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

            if (granted && usbManager.hasPermission(device)) {
                connectToDevice(device)
            } else {
                OtgConnection.updateState(OtgState.PermissionDenied)
            }
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            val device = getUsbDeviceFromIntent(intent) ?: return

            when (action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> handleDeviceAttach(device)
                UsbManager.ACTION_USB_DEVICE_DETACHED -> handleDeviceDetach(device)
            }
        }
    }

    init {
        registerReceivers()
        initAdbCrypto()
        checkConnectedDevices()
    }

    private fun registerReceivers() {
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
    }

    private fun getUsbDeviceFromIntent(intent: Intent): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }

    private fun initAdbCrypto() {
        val base64 = AdbBase64 { data ->
            Base64.encodeToString(data, Base64.NO_WRAP)
        }

        try {
            val priv = File(context.filesDir, "private_key")
            val pub = File(context.filesDir, "public_key")

            adbCrypto = if (priv.exists() && pub.exists()) {
                AdbCrypto.loadAdbKeyPair(base64, priv, pub)
            } else {
                AdbCrypto.generateAdbKeyPair(base64).apply {
                    saveAdbKeyPair(priv, pub)
                }
            }
        } catch (e: Exception) {
            Log.e("OtgRepository", "Failed to initialize AdbCrypto", e)
        }
    }

    private fun handleDeviceAttach(device: UsbDevice) {
        currentDevice = device
        if (isAdbDevice(device)) {
            val friendlyName = device.productName ?: device.manufacturerName ?: device.deviceName
            if (usbManager.hasPermission(device)) {
                connectToDevice(device)
            } else {
                OtgConnection.updateState(OtgState.DeviceFound(friendlyName))
                requestPermission(device)
            }
        } else {
            OtgConnection.updateState(OtgState.Error(context.getString(R.string.no_adb_device_error)))
        }
    }

    private fun handleDeviceDetach(device: UsbDevice) {
        if (device == currentDevice) {
            CoroutineScope(Dispatchers.IO).launch {
                disconnect()
                kotlinx.coroutines.delay(500)
                OtgConnection.updateState(OtgState.Idle)
            }
        }
    }


    override fun searchDevices() {
        OtgConnection.updateState(OtgState.Searching)
        checkConnectedDevices()
    }

    private fun checkConnectedDevices() {
        val devices = usbManager.deviceList.values
        if (devices.isEmpty()) {
            OtgConnection.updateState(OtgState.Idle)
            return
        }

        val adbDevice = devices.firstOrNull { isAdbDevice(it) }
        if (adbDevice != null) handleDeviceAttach(adbDevice)
        else OtgConnection.updateState(OtgState.Error(context.getString(R.string.no_adb_device_error)))
    }

    private fun requestPermission(device: UsbDevice) {
        if (usbManager.hasPermission(device)) return

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(permissionAction), PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, pendingIntent)
    }

    private fun connectToDevice(device: UsbDevice) {
        OtgConnection.updateState(OtgState.Connecting)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val intf = findAdbInterface(device) ?: run {
                    OtgConnection.updateState(OtgState.Error("No ADB interface found"))
                    return@launch
                }

                val connection = usbManager.openDevice(device) ?: run {
                    OtgConnection.updateState(OtgState.Error("Failed to open USB connection"))
                    return@launch
                }

                if (!connection.claimInterface(intf, true)) {
                    OtgConnection.updateState(OtgState.Error("Failed to claim interface"))
                    return@launch
                }

                val channel = UsbChannel(connection, intf)
                val crypto = adbCrypto ?: run {
                    OtgConnection.updateState(OtgState.Error("ADB Crypto not initialized"))
                    return@launch
                }

                adbConnection = AdbConnection.create(channel, crypto).apply { connect() }

                withContext(Dispatchers.Main) {
                    val name = device.productName ?: device.manufacturerName ?: device.deviceName
                    OtgConnection.updateState(OtgState.Connected(name))
                    Log.d("OtgRepository", "ADB connected to $name")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    OtgConnection.updateState(OtgState.Error("ADB Connection failed: ${e.message}"))
                }
                Log.e("OtgRepository", "ADB Connection error", e)
            }
        }
    }

    private fun findAdbInterface(device: UsbDevice): UsbInterface? {
        for (i in 0 until device.interfaceCount) {
            val intf = device.getInterface(i)
            if (intf.interfaceClass == 255 &&
                intf.interfaceSubclass == 66 &&
                intf.interfaceProtocol == 1
            ) return intf
        }
        return null
    }

    override fun runOtgCommand(command: String): Flow<OutputLine> = flow {
        val cmd = sanitizeCommand(command)

        val connection = adbConnection ?: run {
            emit(OutputLine("No OTG ADB connection", isError = true))
            return@flow
        }

        try {
            adbStream = connection.open("shell:$cmd")

            val buffer = StringBuilder()

            while (true) {
                val data = adbStream?.read() ?: break
                val text = String(data, Charsets.UTF_8)
                buffer.append(text)


                val lines = buffer.split("\n")
                for (i in 0 until lines.size - 1) {
                    emit(OutputLine(lines[i].trimEnd(), isError = false))
                }

                buffer.clear()
                buffer.append(lines.last())
            }

            if (buffer.isNotEmpty()) {
                emit(OutputLine(buffer.toString().trimEnd(), isError = false))
            }

        } catch (e: IOException) {
            emit(OutputLine("OTG shell: ${e.message}", isError = true))
        } finally {
            try {
                adbStream?.close()
            } catch (_: Exception) {
            }
            adbStream = null
        }
    }.flowOn(Dispatchers.IO)

    private fun sanitizeCommand(cmd: String): String {
        return cmd.removePrefix("adb shell")
    }

    override fun stopCommand() {
        try {
            adbStream?.close()
        } catch (e: IOException) {
            Log.e("OtgRepository", "Error closing ADB stream", e)
        }
    }

    override fun disconnect() {
        try {
            adbConnection?.close()
        } catch (e: IOException) {
            Log.e("OtgRepository", "Error closing ADB connection", e)
        }
        adbConnection = null
        currentDevice = null
        OtgConnection.updateState(OtgState.Disconnected)
        OtgConnection.updateState(OtgState.Idle)
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
