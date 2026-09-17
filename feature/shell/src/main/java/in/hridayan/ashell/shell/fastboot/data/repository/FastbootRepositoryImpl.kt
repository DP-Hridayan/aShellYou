package `in`.hridayan.ashell.shell.fastboot.data.repository

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import `in`.hridayan.ashell.core.common.domain.model.FastbootState
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootCommandResult
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootConnection
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootDeviceInfo
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.model.RebootMode
import `in`.hridayan.ashell.shell.fastboot.domain.repository.FastbootRepository
import `in`.hridayan.fastboot.FastbootCommand
import `in`.hridayan.fastboot.FastbootDeviceContext
import `in`.hridayan.fastboot.FastbootException
import `in`.hridayan.fastboot.ResponseStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Singleton
class FastbootRepositoryImpl(private val context: Context) : FastbootRepository {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager

    private val flasher = FastbootImageFlasher(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectMutex = Mutex()
    private val receiversRegistered = AtomicBoolean(false)

    @Volatile
    private var currentDevice: UsbDevice? = null

    @Volatile
    private var pendingPermissionDevice: UsbDevice? = null

    @Volatile
    private var lastPermissionRequestAt = 0L

    @Volatile
    private var deviceContext: FastbootDeviceContext? = null

    private var connectJob: Job? = null
    private var rebootWatchJob: Job? = null

    // region Receivers
    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != PERMISSION_ACTION) return
            val device = getUsbDeviceFromIntent(intent) ?: pendingPermissionDevice ?: return
            pendingPermissionDevice = null
            lastPermissionRequestAt = 0L
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) ||
                usbManager?.hasPermission(device) == true

            if (granted) {
                currentDevice = device
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
        if (!receiversRegistered.compareAndSet(false, true)) return
        ContextCompat.registerReceiver(
            context,
            permissionReceiver,
            IntentFilter(PERMISSION_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val usbFilter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        ContextCompat.registerReceiver(
            context,
            usbReceiver,
            usbFilter,
            ContextCompat.RECEIVER_EXPORTED
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

    // region Discovery
    private fun handleDeviceAttach(device: UsbDevice) {
        val manager = usbManager ?: run {
            FastbootConnection.updateState(FastbootState.UsbManagerUnavailable)
            return
        }
        if (!isFastbootDevice(device)) {
            FastbootConnection.updateState(
                FastbootState.Error(context.getString(R.string.no_fastboot_device_error))
            )
            return
        }
        currentDevice = device
        if (manager.hasPermission(device)) {
            connectToDevice(device)
        } else {
            FastbootConnection.updateState(FastbootState.DeviceFound(device.displayName()))
            requestPermission(device)
        }
    }

    /**
     * After a mode switch (bootloader → fastbootd) the device re-enumerates under a new
     * address, so a detach for a different name still counts when our device is gone.
     */
    private fun handleDeviceDetach(device: UsbDevice) {
        val current = currentDevice ?: return
        val currentStillPresent = usbManager?.deviceList?.values
            ?.any { it.deviceName == current.deviceName } == true
        if (device.deviceName != current.deviceName && currentStillPresent) return
        currentDevice = null
        dropConnection()
    }

    override fun searchDevices() {
        val manager = usbManager ?: run {
            FastbootConnection.updateState(FastbootState.UsbManagerUnavailable)
            return
        }
        registerReceivers()
        val state = FastbootConnection.currentState
        if (state is FastbootState.Connecting) return
        if (state is FastbootState.Connected && isCurrentDevicePresent(manager)) return
        if (state !is FastbootState.DeviceFound) {
            FastbootConnection.updateState(FastbootState.Searching)
        }
        checkConnectedDevices()
    }

    private fun isCurrentDevicePresent(manager: UsbManager): Boolean {
        val device = currentDevice ?: return false
        if (deviceContext?.isOpen != true) return false
        return manager.deviceList.values.any {
            it.deviceName == device.deviceName && isFastbootDevice(it)
        }
    }

    private fun checkConnectedDevices() {
        val manager = usbManager ?: run {
            FastbootConnection.updateState(FastbootState.UsbManagerUnavailable)
            return
        }
        val fastbootDevice = manager.deviceList.values.firstOrNull(::isFastbootDevice)
        if (fastbootDevice == null) {
            FastbootConnection.updateState(FastbootState.Idle)
        } else {
            handleDeviceAttach(fastbootDevice)
        }
    }

    private fun requestPermission(device: UsbDevice) {
        val manager = usbManager ?: run {
            FastbootConnection.updateState(FastbootState.UsbManagerUnavailable)
            return
        }
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

    private fun mutableFlag(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
    // endregion

    // region Connection
    private fun connectToDevice(device: UsbDevice) {
        connectJob?.cancel()
        connectJob = scope.launch {
            connectMutex.withLock { connectLocked(device) }
        }
    }

    private fun connectLocked(device: UsbDevice) {
        val existing = deviceContext
        if (existing?.isOpen == true && currentDevice?.deviceName == device.deviceName) {
            FastbootConnection.updateState(FastbootState.Connected(device.displayName(), device.deviceName))
            return
        }
        closeContext()
        FastbootConnection.updateState(FastbootState.Connecting)
        val opened = openContext(device) ?: return
        deviceContext = opened
        currentDevice = device
        FastbootConnection.updateState(FastbootState.Connected(device.displayName(), device.deviceName))
        Log.d(TAG, "Connected to fastboot device ${device.displayName()}")
    }

    private fun openContext(device: UsbDevice): FastbootDeviceContext? {
        val manager = usbManager
        val intf = findFastbootInterface(device)
        return when {
            manager == null -> {
                FastbootConnection.updateState(FastbootState.UsbManagerUnavailable)
                null
            }

            intf == null -> failConnect(R.string.no_fastboot_interface_found)
            else -> manager.openDevice(device)?.let { claimContext(it, intf) }
                ?: failConnect(R.string.usb_open_failed)
        }
    }

    private fun claimContext(usbConnection: UsbDeviceConnection, intf: UsbInterface): FastbootDeviceContext? {
        if (!usbConnection.claimInterface(intf, true)) {
            usbConnection.close()
            return failConnect(R.string.usb_claim_failed)
        }
        val endpoints = findBulkEndpoints(intf)
        if (endpoints == null) {
            usbConnection.releaseInterface(intf)
            usbConnection.close()
            return failConnect(R.string.usb_bulk_endpoints_missing)
        }
        return FastbootDeviceContext(usbConnection, intf, endpoints.first, endpoints.second)
    }

    private fun failConnect(@StringRes message: Int): FastbootDeviceContext? {
        FastbootConnection.updateState(FastbootState.Error(context.getString(message)))
        return null
    }

    private fun findBulkEndpoints(intf: UsbInterface): Pair<UsbEndpoint, UsbEndpoint>? {
        val bulk = (0 until intf.endpointCount)
            .map(intf::getEndpoint)
            .filter { it.type == UsbConstants.USB_ENDPOINT_XFER_BULK }
        val input = bulk.firstOrNull { it.direction == UsbConstants.USB_DIR_IN } ?: return null
        val output = bulk.firstOrNull { it.direction == UsbConstants.USB_DIR_OUT } ?: return null
        return input to output
    }

    private fun dropConnection() {
        connectJob?.cancel()
        scope.launch {
            connectMutex.withLock {
                closeContext()
                FastbootConnection.updateState(FastbootState.Disconnected)
                delay(DISCONNECT_SETTLE_DELAY)
                if (FastbootConnection.currentState is FastbootState.Disconnected) {
                    FastbootConnection.updateState(FastbootState.Idle)
                }
            }
        }
    }

    private fun closeContext() {
        val ctx = deviceContext ?: return
        deviceContext = null
        ctx.abort()
        runCatching { ctx.close() }
    }

    override fun disconnect() {
        currentDevice = null
        dropConnection()
    }

    override fun cancelOperation() {
        val ctx = deviceContext ?: return
        ctx.abort()
        scope.launch {
            connectMutex.withLock {
                closeContext()
                FastbootConnection.updateState(FastbootState.Disconnected)
            }
            delay(RECONNECT_DELAY)
            searchDevices()
        }
    }

    override fun reboot(mode: RebootMode) {
        val ctx = deviceContext ?: return
        rebootWatchJob?.cancel()
        rebootWatchJob = scope.launch {
            runCatching { ctx.sendCommand(rebootCommand(mode)) }
                .onFailure { Log.e(TAG, "Reboot failed", it) }
            connectMutex.withLock {
                closeContext()
                currentDevice = null
                FastbootConnection.updateState(FastbootState.Disconnected)
            }
            if (mode == RebootMode.BOOTLOADER || mode == RebootMode.FASTBOOTD) pollForDevice()
        }
    }

    private fun rebootCommand(mode: RebootMode): FastbootCommand = when (mode) {
        RebootMode.NORMAL -> FastbootCommand.reboot()
        RebootMode.BOOTLOADER -> FastbootCommand.rebootBootloader()
        RebootMode.RECOVERY -> FastbootCommand.rebootRecovery()
        RebootMode.FASTBOOTD -> FastbootCommand.rebootFastboot()
    }

    /**
     * Android does not reliably fire USB_DEVICE_ATTACHED when a device reboots between fastboot
     * modes without a physical replug, so poll for it to reappear.
     */
    private suspend fun pollForDevice() {
        repeat(REBOOT_POLL_ATTEMPTS) {
            delay(REBOOT_POLL_INTERVAL)
            if (FastbootConnection.currentState is FastbootState.Connected) return
            searchDevices()
        }
    }

    private fun findFastbootInterface(device: UsbDevice): UsbInterface? =
        (0 until device.interfaceCount).map(device::getInterface).firstOrNull(::isFastbootInterface)

    private fun isFastbootDevice(device: UsbDevice): Boolean = findFastbootInterface(device) != null

    private fun isFastbootInterface(intf: UsbInterface): Boolean =
        intf.interfaceClass == FASTBOOT_INTERFACE_CLASS &&
            intf.interfaceSubclass == FASTBOOT_INTERFACE_SUBCLASS &&
            intf.interfaceProtocol == FASTBOOT_INTERFACE_PROTOCOL

    private fun UsbDevice.displayName(): String = productName ?: manufacturerName ?: deviceName
    // endregion

    // region Commands
    override fun sendCommand(command: String): Flow<FastbootCommandResult> = flow {
        val ctx = deviceContext ?: run {
            emit(failedResult(command, context.getString(R.string.no_device_connected)))
            return@flow
        }

        val stripped = command.trim().let {
            if (it.startsWith("fastboot ", ignoreCase = true)) it.substring(CLI_PREFIX_LENGTH).trimStart() else it
        }

        if (stripped.equals("devices", ignoreCase = true)) {
            val state = FastbootConnection.currentState
            val name = if (state is FastbootState.Connected) state.deviceName else "no device"
            emit(FastbootCommandResult(command = command, status = ResponseStatus.OKAY, data = "$name\tfastboot"))
            return@flow
        }

        try {
            val response = ctx.sendCommand(parseCommand(stripped))
            emit(FastbootCommandResult(command = command, status = response.status, data = response.data))
        } catch (e: FastbootException) {
            emit(failedResult(command, e.message ?: context.getString(R.string.unknown_error)))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Parses user input into a FastbootCommand.
     * Accepts both CLI-style ("getvar unlocked", "erase boot") and
     * protocol-style ("getvar:unlocked", "erase:boot") syntax.
     */
    private fun parseCommand(input: String): FastbootCommand {
        val parts = input.split("\\s+".toRegex(), limit = 2)
        val verb = parts[0].lowercase()
        val arg = parts.getOrNull(1)?.trim()

        return when {
            input.startsWith("getvar:") -> FastbootCommand.getVar(input.removePrefix("getvar:"))
            input.startsWith("erase:") -> FastbootCommand.erase(input.removePrefix("erase:"))
            verb == "getvar" && arg != null -> FastbootCommand.getVar(arg)
            verb == "erase" && arg != null -> FastbootCommand.erase(arg)
            verb == "reboot" && arg == null -> FastbootCommand.reboot()
            verb == "reboot" && arg.equals("bootloader", ignoreCase = true) -> FastbootCommand.rebootBootloader()
            verb == "reboot-bootloader" -> FastbootCommand.rebootBootloader()
            verb == "reboot" && arg.equals("recovery", ignoreCase = true) -> FastbootCommand.rebootRecovery()
            verb == "reboot-recovery" -> FastbootCommand.rebootRecovery()
            verb == "reboot" && arg.equals("fastboot", ignoreCase = true) -> FastbootCommand.rebootFastboot()
            verb == "reboot-fastboot" -> FastbootCommand.rebootFastboot()
            verb == "oem" && arg != null -> FastbootCommand.oem(arg)
            verb == "continue" -> FastbootCommand.continueBooting()
            else -> FastbootCommand.raw(input)
        }
    }

    override fun getDeviceInfo(): Flow<FastbootDeviceInfo> = flow {
        val ctx = deviceContext ?: run {
            emit(FastbootDeviceInfo())
            return@flow
        }

        fun queryVar(name: String): String? = try {
            val response = ctx.sendCommand(FastbootCommand.getVar(name))
            if (response.isOkay) response.data.takeIf { it.isNotBlank() } else null
        } catch (_: FastbootException) {
            null
        }

        emit(
            FastbootDeviceInfo(
                product = queryVar("product"),
                serialNo = queryVar("serialno"),
                variant = queryVar("variant"),
                bootloaderVersion = queryVar("version-bootloader"),
                basebandVersion = queryVar("version-baseband"),
                isUnlocked = queryVar("unlocked")?.let { it == "yes" || it == "true" },
                currentSlot = queryVar("current-slot"),
                batteryLevel = queryVar("battery-level")?.toIntOrNull(),
                batteryVoltage = queryVar("battery-voltage"),
                batterySocOk = queryVar("battery-soc-ok"),
                maxDownloadSize = queryVar("max-download-size"),
                securityPatchLevel = queryVar("security-patch-level")
            )
        )
    }.flowOn(Dispatchers.IO)

    override fun getAllVariables(): Flow<List<Pair<String, String>>> = flow {
        val ctx = deviceContext ?: run {
            emit(emptyList())
            return@flow
        }

        try {
            val response = ctx.sendCommand(FastbootCommand.getVar("all"))
            emit(parseVariables(response.data))
        } catch (e: FastbootException) {
            Log.e(TAG, "Error querying all variables", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    private fun failedResult(command: String, message: String): FastbootCommandResult =
        FastbootCommandResult(command = command, status = ResponseStatus.FAIL, data = message)

    private fun parseVariables(data: String): List<Pair<String, String>> = data.lines()
        .mapNotNull { line ->
            val colonIndex = line.indexOf(':')
            if (colonIndex <= 0) return@mapNotNull null
            val key = line.substring(0, colonIndex).trim()
            val value = line.substring(colonIndex + 1).trim()
            if (key.isBlank()) null else key to value
        }
        .sortedBy { it.first }
    // endregion

    // region Flash, erase, boot
    override fun flashPartition(
        partition: String,
        imageUri: Uri,
        onProgress: (FlashOperation) -> Unit
    ): Flow<FastbootCommandResult> = flasher.flash(deviceContext, partition, imageUri, onProgress)

    override fun bootImage(
        imageUri: Uri,
        onProgress: (FlashOperation) -> Unit
    ): Flow<FastbootCommandResult> = flasher.boot(deviceContext, imageUri, onProgress)

    override fun erasePartition(
        partition: String,
        onProgress: (FlashOperation) -> Unit
    ): Flow<FastbootCommandResult> = flasher.erase(deviceContext, partition, onProgress)
    // endregion

    private companion object {
        const val TAG = "FastbootRepository"
        const val PERMISSION_ACTION = "in.hridayan.ashell.FASTBOOT_USB_PERMISSION"
        const val PERMISSION_REQUEST_CODE = 0
        const val PERMISSION_COOLDOWN_MS = 20_000L
        const val FASTBOOT_INTERFACE_CLASS = 0xFF
        const val FASTBOOT_INTERFACE_SUBCLASS = 0x42
        const val FASTBOOT_INTERFACE_PROTOCOL = 0x03
        const val BOOT_PARTITION = "boot"
        const val BOOT_COMMAND = "boot"
        const val MAX_DOWNLOAD_SIZE_VAR = "max-download-size"
        const val HEX_PREFIX = "0x"
        const val HEX_RADIX = 16
        const val CLI_PREFIX_LENGTH = 9
        const val BYTES_PER_MEGABYTE = 1024.0 * 1024.0
        const val REBOOT_POLL_ATTEMPTS = 15
        val REBOOT_POLL_INTERVAL = 2.seconds
        val DISCONNECT_SETTLE_DELAY = 300.milliseconds
        val RECONNECT_DELAY = 1.seconds
    }
}
