package `in`.hridayan.ashell.shell.fastboot.data.repository

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import `in`.hridayan.fastboot.FastbootCommand
import `in`.hridayan.fastboot.FastbootDeviceContext
import `in`.hridayan.fastboot.FastbootException
import `in`.hridayan.fastboot.ResponseStatus
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootCommandResult
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootConnection
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootDeviceInfo
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootState
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashStatus
import `in`.hridayan.ashell.shell.fastboot.domain.model.RebootMode
import `in`.hridayan.ashell.shell.fastboot.domain.repository.FastbootRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Singleton
class FastbootRepositoryImpl(private val context: Context) : FastbootRepository {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
    private val permissionAction = "in.hridayan.ashell.FASTBOOT_USB_PERMISSION"

    private var currentDevice: UsbDevice? = null
    private var deviceContext: FastbootDeviceContext? = null

    // Fastboot USB interface identifiers
    companion object {
        private const val TAG = "FastbootRepository"
        private const val FASTBOOT_INTERFACE_CLASS = 0xFF
        private const val FASTBOOT_INTERFACE_SUBCLASS = 0x42
        private const val FASTBOOT_INTERFACE_PROTOCOL = 0x03
    }

    // region Receivers
    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != permissionAction) return
            val device = getUsbDeviceFromIntent(intent) ?: return
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

            if (granted && usbManager?.hasPermission(device) == true) {
                connectToDevice(device)
            } else {
                FastbootConnection.updateState(FastbootState.PermissionDenied)
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
    // endregion

    init {
        if (usbManager == null) {
            FastbootConnection.updateState(FastbootState.UsbManagerUnavailable)
        } else {
            registerReceivers()
            checkConnectedDevices()
        }
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

    private fun handleDeviceAttach(device: UsbDevice) {
        currentDevice = device
        val manager = usbManager ?: run {
            FastbootConnection.updateState(FastbootState.UsbManagerUnavailable)
            return
        }
        if (isFastbootDevice(device)) {
            val friendlyName = device.productName ?: device.manufacturerName ?: device.deviceName
            if (manager.hasPermission(device)) {
                connectToDevice(device)
            } else {
                FastbootConnection.updateState(FastbootState.DeviceFound(friendlyName))
                requestPermission(device)
            }
        } else {
            FastbootConnection.updateState(
                FastbootState.Error(context.getString(R.string.no_fastboot_device_error))
            )
        }
    }

    private fun handleDeviceDetach(device: UsbDevice) {
        if (device == currentDevice) {
            CoroutineScope(Dispatchers.IO).launch {
                disconnect()
                kotlinx.coroutines.delay(500)
                FastbootConnection.updateState(FastbootState.Idle)
            }
        }
    }

    override fun searchDevices() {
        if (usbManager == null) {
            FastbootConnection.updateState(FastbootState.UsbManagerUnavailable)
            return
        }
        FastbootConnection.updateState(FastbootState.Searching)
        checkConnectedDevices()
    }

    private fun checkConnectedDevices() {
        val manager = usbManager ?: run {
            FastbootConnection.updateState(FastbootState.UsbManagerUnavailable)
            return
        }
        val devices = manager.deviceList.values
        if (devices.isEmpty()) {
            FastbootConnection.updateState(FastbootState.Idle)
            return
        }

        val fastbootDevice = devices.firstOrNull { isFastbootDevice(it) }
        if (fastbootDevice != null) handleDeviceAttach(fastbootDevice)
        else FastbootConnection.updateState(
            FastbootState.Error(context.getString(R.string.no_fastboot_device_error))
        )
    }

    private fun requestPermission(device: UsbDevice) {
        val manager = usbManager ?: run {
            FastbootConnection.updateState(FastbootState.UsbManagerUnavailable)
            return
        }
        if (manager.hasPermission(device)) return

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(permissionAction), PendingIntent.FLAG_IMMUTABLE
        )
        manager.requestPermission(device, pendingIntent)
    }

    private fun connectToDevice(device: UsbDevice) {
        FastbootConnection.updateState(FastbootState.Connecting)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val intf = findFastbootInterface(device) ?: run {
                    FastbootConnection.updateState(FastbootState.Error("No fastboot interface found"))
                    return@launch
                }

                val manager = usbManager ?: run {
                    FastbootConnection.updateState(FastbootState.UsbManagerUnavailable)
                    return@launch
                }

                val connection = manager.openDevice(device) ?: run {
                    FastbootConnection.updateState(FastbootState.Error("Failed to open USB connection"))
                    return@launch
                }

                if (!connection.claimInterface(intf, true)) {
                    FastbootConnection.updateState(FastbootState.Error("Failed to claim interface"))
                    return@launch
                }

                // Find bulk IN and OUT endpoints
                var inEndpoint: android.hardware.usb.UsbEndpoint? = null
                var outEndpoint: android.hardware.usb.UsbEndpoint? = null

                for (i in 0 until intf.endpointCount) {
                    val ep = intf.getEndpoint(i)
                    if (ep.type == android.hardware.usb.UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (ep.direction == android.hardware.usb.UsbConstants.USB_DIR_IN) {
                            inEndpoint = ep
                        } else {
                            outEndpoint = ep
                        }
                    }
                }

                if (inEndpoint == null || outEndpoint == null) {
                    FastbootConnection.updateState(FastbootState.Error("Could not find bulk endpoints"))
                    connection.releaseInterface(intf)
                    connection.close()
                    return@launch
                }

                deviceContext = FastbootDeviceContext(connection, intf, inEndpoint, outEndpoint)

                withContext(Dispatchers.Main) {
                    val name = device.productName ?: device.manufacturerName ?: device.deviceName
                    FastbootConnection.updateState(
                        FastbootState.Connected(name, device.deviceName)
                    )
                    Log.d(TAG, "Fastboot connected to $name")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    FastbootConnection.updateState(
                        FastbootState.Error("Connection failed: ${e.message}")
                    )
                }
                Log.e(TAG, "Fastboot connection error", e)
            }
        }
    }

    private fun findFastbootInterface(device: UsbDevice): UsbInterface? {
        for (i in 0 until device.interfaceCount) {
            val intf = device.getInterface(i)
            if (intf.interfaceClass == FASTBOOT_INTERFACE_CLASS &&
                intf.interfaceSubclass == FASTBOOT_INTERFACE_SUBCLASS &&
                intf.interfaceProtocol == FASTBOOT_INTERFACE_PROTOCOL
            ) return intf
        }
        return null
    }

    private fun isFastbootDevice(device: UsbDevice): Boolean {
        for (i in 0 until device.interfaceCount) {
            val intf = device.getInterface(i)
            if (intf.interfaceClass == FASTBOOT_INTERFACE_CLASS &&
                intf.interfaceSubclass == FASTBOOT_INTERFACE_SUBCLASS &&
                intf.interfaceProtocol == FASTBOOT_INTERFACE_PROTOCOL
            ) return true
        }
        return false
    }

    override fun sendCommand(command: String): Flow<FastbootCommandResult> = flow {
        val ctx = deviceContext ?: run {
            emit(FastbootCommandResult(
                command = command,
                status = ResponseStatus.FAIL,
                data = "No fastboot device connected"
            ))
            return@flow
        }

        try {
            val fbCommand = when {
                command.startsWith("getvar:") -> FastbootCommand.getVar(command.removePrefix("getvar:"))
                command == "reboot" -> FastbootCommand.reboot()
                command == "reboot-bootloader" || command == "reboot bootloader" -> FastbootCommand.rebootBootloader()
                command == "reboot-recovery" || command == "reboot recovery" -> FastbootCommand.rebootRecovery()
                command == "reboot-fastboot" || command == "reboot fastboot" -> FastbootCommand.rebootFastboot()
                command.startsWith("erase:") -> FastbootCommand.erase(command.removePrefix("erase:"))
                command.startsWith("oem ") -> FastbootCommand.oem(command.removePrefix("oem "))
                command == "continue" -> FastbootCommand.continueBooting()
                else -> FastbootCommand.raw(command)
            }

            val response = ctx.sendCommand(fbCommand)
            emit(FastbootCommandResult(
                command = command,
                status = response.status,
                data = response.data
            ))
        } catch (e: FastbootException) {
            emit(FastbootCommandResult(
                command = command,
                status = ResponseStatus.FAIL,
                data = e.message ?: "Unknown error"
            ))
        } catch (e: Exception) {
            emit(FastbootCommandResult(
                command = command,
                status = ResponseStatus.FAIL,
                data = "Error: ${e.message}"
            ))
        }
    }.flowOn(Dispatchers.IO)

    override fun getDeviceInfo(): Flow<FastbootDeviceInfo> = flow {
        val ctx = deviceContext ?: run {
            emit(FastbootDeviceInfo())
            return@flow
        }

        try {
            fun queryVar(name: String): String? {
                return try {
                    val response = ctx.sendCommand(FastbootCommand.getVar(name))
                    if (response.isOkay) response.data.takeIf { it.isNotBlank() } else null
                } catch (_: Exception) { null }
            }

            emit(FastbootDeviceInfo(
                product = queryVar("product"),
                serialNo = queryVar("serialno"),
                variant = queryVar("variant"),
                bootloaderVersion = queryVar("version-bootloader"),
                basebandVersion = queryVar("version-baseband"),
                isUnlocked = queryVar("unlocked")?.let { it == "yes" || it == "true" },
                currentSlot = queryVar("current-slot"),
                batteryLevel = queryVar("battery-voltage") ?: queryVar("battery-soc-ok"),
                maxDownloadSize = queryVar("max-download-size"),
                securityPatchLevel = queryVar("security-patch-level")
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Error querying device info", e)
            emit(FastbootDeviceInfo())
        }
    }.flowOn(Dispatchers.IO)

    override fun reboot(mode: RebootMode) {
        val ctx = deviceContext ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val command = when (mode) {
                    RebootMode.NORMAL -> FastbootCommand.reboot()
                    RebootMode.BOOTLOADER -> FastbootCommand.rebootBootloader()
                    RebootMode.RECOVERY -> FastbootCommand.rebootRecovery()
                    RebootMode.FASTBOOTD -> FastbootCommand.rebootFastboot()
                }
                ctx.sendCommand(command)
            } catch (e: Exception) {
                Log.e(TAG, "Reboot failed", e)
            }
        }
    }

    override fun getAllVariables(): Flow<List<Pair<String, String>>> = flow {
        val ctx = deviceContext ?: run {
            emit(emptyList())
            return@flow
        }

        try {
            val response = ctx.sendCommand(FastbootCommand.getVar("all"))
            val variables = mutableListOf<Pair<String, String>>()

            if (response.data.isNotBlank()) {
                response.data.lines().forEach { line ->
                    // INFO responses for getvar:all typically come as "key: value" or "key:value"
                    val colonIndex = line.indexOf(':')
                    if (colonIndex > 0) {
                        val key = line.substring(0, colonIndex).trim()
                        val value = line.substring(colonIndex + 1).trim()
                        if (key.isNotBlank()) {
                            variables.add(key to value)
                        }
                    }
                }
            }

            emit(variables.sortedBy { it.first })
        } catch (e: Exception) {
            Log.e(TAG, "Error querying all variables", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    override fun disconnect() {
        try {
            deviceContext?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing fastboot connection", e)
        }
        deviceContext = null
        currentDevice = null
        FastbootConnection.updateState(FastbootState.Disconnected)
        FastbootConnection.updateState(FastbootState.Idle)
    }

    override fun unRegister() {
        try {
            context.unregisterReceiver(permissionReceiver)
            context.unregisterReceiver(usbReceiver)
        } catch (_: Exception) {}
    }

    override fun flashPartition(
        partition: String,
        imageUri: Uri,
        onProgress: (FlashOperation) -> Unit
    ): Flow<FastbootCommandResult> = flow {
        val ctx = deviceContext ?: run {
            onProgress(FlashOperation(partition = partition, status = FlashStatus.ERROR, message = "No device connected"))
            emit(FastbootCommandResult(command = "flash:$partition", status = ResponseStatus.FAIL, data = "No device connected"))
            return@flow
        }

        try {
            // Step 1: Read the image file
            onProgress(FlashOperation(partition = partition, status = FlashStatus.READING_FILE, message = "Reading image file..."))
            val imageData = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: throw FastbootException("Cannot open file")

            val fileSizeMB = String.format("%.1f", imageData.size / (1024.0 * 1024.0))
            onProgress(FlashOperation(
                partition = partition,
                status = FlashStatus.DOWNLOADING,
                progress = 0f,
                message = "Downloading ${fileSizeMB}MB to device..."
            ))

            // Step 2: Flash with progress
            val command = FastbootCommand.flash(partition, imageData)
            val response = ctx.sendCommand(command) { bytesSent, totalBytes ->
                val progress = bytesSent.toFloat() / totalBytes.toFloat()
                onProgress(FlashOperation(
                    partition = partition,
                    status = if (progress < 1f) FlashStatus.DOWNLOADING else FlashStatus.FLASHING,
                    progress = progress,
                    message = if (progress < 1f) "Sending ${(progress * 100).toInt()}% ($fileSizeMB MB)"
                             else "Writing to $partition..."
                ))
            }

            if (response.isOkay) {
                onProgress(FlashOperation(partition = partition, status = FlashStatus.COMPLETE, progress = 1f, message = "Flash complete"))
            } else {
                onProgress(FlashOperation(partition = partition, status = FlashStatus.ERROR, message = "Flash failed: ${response.data}"))
            }

            emit(FastbootCommandResult(command = "flash:$partition", status = response.status, data = response.data))
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown error"
            onProgress(FlashOperation(partition = partition, status = FlashStatus.ERROR, message = msg))
            emit(FastbootCommandResult(command = "flash:$partition", status = ResponseStatus.FAIL, data = msg))
            Log.e(TAG, "Flash $partition failed", e)
        }
    }.flowOn(Dispatchers.IO)

    override fun erasePartition(
        partition: String,
        onProgress: (FlashOperation) -> Unit
    ): Flow<FastbootCommandResult> = flow {
        val ctx = deviceContext ?: run {
            onProgress(FlashOperation(partition = partition, status = FlashStatus.ERROR, message = "No device connected"))
            emit(FastbootCommandResult(command = "erase:$partition", status = ResponseStatus.FAIL, data = "No device connected"))
            return@flow
        }

        try {
            onProgress(FlashOperation(partition = partition, status = FlashStatus.ERASING, message = "Erasing $partition..."))

            val response = ctx.sendCommand(FastbootCommand.erase(partition))

            if (response.isOkay) {
                onProgress(FlashOperation(partition = partition, status = FlashStatus.COMPLETE, progress = 1f, message = "Erase complete"))
            } else {
                onProgress(FlashOperation(partition = partition, status = FlashStatus.ERROR, message = "Erase failed: ${response.data}"))
            }

            emit(FastbootCommandResult(command = "erase:$partition", status = response.status, data = response.data))
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown error"
            onProgress(FlashOperation(partition = partition, status = FlashStatus.ERROR, message = msg))
            emit(FastbootCommandResult(command = "erase:$partition", status = ResponseStatus.FAIL, data = msg))
            Log.e(TAG, "Erase $partition failed", e)
        }
    }.flowOn(Dispatchers.IO)

    override fun bootImage(
        imageUri: Uri,
        onProgress: (FlashOperation) -> Unit
    ): Flow<FastbootCommandResult> = flow {
        val ctx = deviceContext ?: run {
            onProgress(FlashOperation(partition = "boot", status = FlashStatus.ERROR, message = "No device connected"))
            emit(FastbootCommandResult(command = "boot", status = ResponseStatus.FAIL, data = "No device connected"))
            return@flow
        }

        try {
            onProgress(FlashOperation(partition = "boot", status = FlashStatus.READING_FILE, message = "Reading boot image..."))
            val imageData = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: throw FastbootException("Cannot open file")

            val fileSizeMB = String.format("%.1f", imageData.size / (1024.0 * 1024.0))
            onProgress(FlashOperation(
                partition = "boot",
                status = FlashStatus.DOWNLOADING,
                progress = 0f,
                message = "Sending ${fileSizeMB}MB..."
            ))

            val command = FastbootCommand.boot(imageData)
            val response = ctx.sendCommand(command) { bytesSent, totalBytes ->
                val progress = bytesSent.toFloat() / totalBytes.toFloat()
                onProgress(FlashOperation(
                    partition = "boot",
                    status = FlashStatus.DOWNLOADING,
                    progress = progress,
                    message = "Sending ${(progress * 100).toInt()}%"
                ))
            }

            if (response.isOkay) {
                onProgress(FlashOperation(partition = "boot", status = FlashStatus.COMPLETE, progress = 1f, message = "Boot image sent"))
            } else {
                onProgress(FlashOperation(partition = "boot", status = FlashStatus.ERROR, message = "Boot failed: ${response.data}"))
            }

            emit(FastbootCommandResult(command = "boot", status = response.status, data = response.data))
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown error"
            onProgress(FlashOperation(partition = "boot", status = FlashStatus.ERROR, message = msg))
            emit(FastbootCommandResult(command = "boot", status = ResponseStatus.FAIL, data = msg))
            Log.e(TAG, "Boot image failed", e)
        }
    }.flowOn(Dispatchers.IO)
}
