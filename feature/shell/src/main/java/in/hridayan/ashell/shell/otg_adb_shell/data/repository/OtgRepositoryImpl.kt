package `in`.hridayan.ashell.shell.otg_adb_shell.data.repository

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
import android.util.Base64
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.cgutman.adblib.AdbBase64
import com.cgutman.adblib.AdbConnection
import com.cgutman.adblib.AdbCrypto
import com.cgutman.adblib.AdbStream
import com.cgutman.adblib.UsbChannel
import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgConnection
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.repository.OtgRepository
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class OtgRepositoryImpl(private val context: Context) : OtgRepository {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager

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
    private var adbConnection: AdbConnection? = null
    private var adbCrypto: AdbCrypto? = null
    private var connectJob: Job? = null

    private var adbStream: AdbStream? = null

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
        if (usbManager == null) {
            OtgConnection.updateState(OtgState.UsbManagerUnavailable)
        } else {
            registerReceivers()
            initAdbCrypto()
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

    private fun initAdbCrypto() {
        val base64 = AdbBase64 { data ->
            Base64.encodeToString(data, Base64.NO_WRAP)
        }

        try {
            val priv = File(context.filesDir, PRIVATE_KEY_FILE)
            val pub = File(context.filesDir, PUBLIC_KEY_FILE)

            adbCrypto = if (priv.exists() && pub.exists()) {
                AdbCrypto.loadAdbKeyPair(base64, priv, pub)
            } else {
                AdbCrypto.generateAdbKeyPair(base64).apply {
                    saveAdbKeyPair(priv, pub)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdbCrypto", e)
        }
    }

    private fun handleDeviceAttach(device: UsbDevice) {
        currentDevice = device
        val manager = usbManager ?: run {
            OtgConnection.updateState(OtgState.UsbManagerUnavailable)
            return
        }
        if (isAdbDevice(device)) {
            if (manager.hasPermission(device)) {
                connectToDevice(device)
            } else {
                OtgConnection.updateState(OtgState.DeviceFound(device.displayName()))
                requestPermission(device)
            }
        } else {
            OtgConnection.updateState(OtgState.Error(context.getString(R.string.no_adb_device_error)))
        }
    }

    private fun handleDeviceDetach(device: UsbDevice) {
        if (device.deviceName != currentDevice?.deviceName) return
        currentDevice = null
        dropConnection()
    }

    override fun searchDevices() {
        if (usbManager == null) {
            OtgConnection.updateState(OtgState.UsbManagerUnavailable)
            return
        }
        registerReceivers()
        if (isScanRedundant()) return
        if (OtgConnection.currentState !is OtgState.DeviceFound) {
            OtgConnection.updateState(OtgState.Searching)
        }
        checkConnectedDevices()
    }

    private fun isScanRedundant(): Boolean {
        if (connectJob?.isActive == true) return true
        return when (OtgConnection.currentState) {
            is OtgState.Connecting -> true
            is OtgState.Connected -> isConnected()
            else -> false
        }
    }

    private fun checkConnectedDevices() {
        val manager = usbManager ?: run {
            OtgConnection.updateState(OtgState.UsbManagerUnavailable)
            return
        }
        val devices = manager.deviceList.values
        if (devices.isEmpty()) {
            OtgConnection.updateState(OtgState.Idle)
            return
        }

        val adbDevice = devices.firstOrNull { isAdbDevice(it) }
        if (adbDevice != null) {
            handleDeviceAttach(adbDevice)
        } else {
            OtgConnection.updateState(OtgState.Error(context.getString(R.string.no_adb_device_error)))
        }
    }

    private fun requestPermission(device: UsbDevice) {
        val manager = usbManager ?: run {
            OtgConnection.updateState(OtgState.UsbManagerUnavailable)
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

    private fun connectToDevice(device: UsbDevice) {
        if (connectJob?.isActive == true) return
        connectJob = scope.launch {
            connectMutex.withLock { connectLocked(device) }
        }
    }

    private suspend fun connectLocked(device: UsbDevice) {
        if (isConnected()) {
            OtgConnection.updateState(OtgState.Connected(device.displayName()))
            return
        }
        closeConnection()
        OtgConnection.updateState(OtgState.Connecting)

        val crypto = adbCrypto ?: run {
            OtgConnection.updateState(OtgState.Error(context.getString(R.string.adb_key_unavailable)))
            return
        }
        val channel = openChannel(device) ?: return
        val connection = AdbConnection.create(channel, crypto)
        val established = handshake(connection)
        if (established) {
            connection.setConnectionListener(::onConnectionClosed)
            adbConnection = connection
            OtgConnection.updateState(OtgState.Connected(device.displayName()))
            Log.d(TAG, "ADB connected to ${device.displayName()}")
        }
    }

    private fun openChannel(device: UsbDevice): UsbChannel? {
        val manager = usbManager
        val intf = findAdbInterface(device)
        return when {
            manager == null -> {
                OtgConnection.updateState(OtgState.UsbManagerUnavailable)
                null
            }

            intf == null -> failConnect(R.string.no_adb_interface_found)
            else -> manager.openDevice(device)?.let { claimChannel(it, intf) }
                ?: failConnect(R.string.usb_open_failed)
        }
    }

    private fun claimChannel(usbConnection: UsbDeviceConnection, intf: UsbInterface): UsbChannel? {
        if (!usbConnection.claimInterface(intf, true)) {
            usbConnection.close()
            return failConnect(R.string.usb_claim_failed)
        }
        return try {
            UsbChannel(usbConnection, intf)
        } catch (_: IllegalArgumentException) {
            usbConnection.releaseInterface(intf)
            usbConnection.close()
            failConnect(R.string.no_adb_interface_found)
        }
    }

    private fun failConnect(@StringRes message: Int): UsbChannel? {
        OtgConnection.updateState(OtgState.Error(context.getString(message)))
        return null
    }

    private suspend fun handshake(connection: AdbConnection): Boolean {
        var established = false
        try {
            withTimeout(CONNECT_TIMEOUT_MS) {
                runInterruptible(Dispatchers.IO) { connection.connect() }
            }
            established = true
        } catch (e: TimeoutCancellationException) {
            OtgConnection.updateState(
                OtgState.Error(context.getString(R.string.sideload_connection_timed_out))
            )
            Log.e(TAG, "ADB connection timed out", e)
        } catch (e: IOException) {
            OtgConnection.updateState(OtgState.Error(context.getString(R.string.connection_failed)))
            Log.e(TAG, "ADB connection error", e)
        } finally {
            if (!established) runCatching { connection.close() }
        }
        return established
    }

    private fun onConnectionClosed(closed: AdbConnection) {
        if (closed !== adbConnection) return
        adbConnection = null
        val state = OtgConnection.currentState
        if (state is OtgState.Connected || state is OtgState.Connecting) {
            dropConnection()
        }
    }

    private fun dropConnection() {
        connectJob?.cancel()
        scope.launch {
            connectMutex.withLock {
                closeConnection()
                OtgConnection.updateState(OtgState.Disconnected)
                delay(DISCONNECT_SETTLE_DELAY)
                OtgConnection.updateState(OtgState.Idle)
            }
        }
    }

    private fun closeConnection() {
        val connection = adbConnection ?: return
        adbConnection = null
        runCatching { connection.close() }
    }

    private fun findAdbInterface(device: UsbDevice): UsbInterface? =
        (0 until device.interfaceCount).map(device::getInterface).firstOrNull(::isAdbInterface)

    private fun isAdbInterface(intf: UsbInterface): Boolean =
        intf.interfaceClass == UsbConstants.USB_CLASS_VENDOR_SPEC &&
            intf.interfaceSubclass == ADB_INTERFACE_SUBCLASS &&
            intf.interfaceProtocol == ADB_INTERFACE_PROTOCOL

    private fun UsbDevice.displayName(): String = productName ?: manufacturerName ?: deviceName
    // endregion

    private var currentDir = "/storage/emulated/0/"

    /**
     * Handle cd command and return the command to actually execute.
     * - If it's a pure "cd" command, updates currentDir and returns null.
     * - If it's a compound command starting with cd (e.g., "cd /data && ls"),
     *   updates currentDir and returns the remaining commands.
     */
    private fun handleCdCommand(commandText: String): String? {
        val trimmedCommand = commandText.trim()

        if (trimmedCommand.startsWith("cd ") || trimmedCommand == "cd") {
            // Check for compound command separators (&& or ;)
            val andAndIndex = trimmedCommand.indexOf(" && ")
            val semicolonIndex = trimmedCommand.indexOf("; ")

            val separatorIndex = when {
                andAndIndex >= 0 && semicolonIndex >= 0 -> minOf(andAndIndex, semicolonIndex)
                andAndIndex >= 0 -> andAndIndex
                semicolonIndex >= 0 -> semicolonIndex
                else -> -1
            }

            val cdPart: String
            val remainingCommand: String?

            if (separatorIndex > 0) {
                cdPart = trimmedCommand.substring(0, separatorIndex).trim()
                remainingCommand = trimmedCommand.substring(
                    separatorIndex + if (trimmedCommand.substring(separatorIndex)
                            .startsWith(" && ")
                    ) {
                        4
                    } else {
                        2
                    }
                ).trim()
            } else {
                cdPart = trimmedCommand
                remainingCommand = null
            }

            val parts = cdPart.split("\\s+".toRegex(), limit = 2)
            val targetDir = if (parts.size > 1) parts[1] else "/"

            currentDir = when {
                targetDir == "/" || targetDir == "~" -> "/"
                targetDir == ".." -> {
                    val parent = currentDir.removeSuffix("/").substringBeforeLast("/", "")
                    if (parent.isEmpty()) "/" else "$parent/"
                }

                targetDir.startsWith("/") -> {
                    if (targetDir.endsWith("/")) targetDir else "$targetDir/"
                }

                else -> {
                    val newPath = currentDir + targetDir
                    if (newPath.endsWith("/")) newPath else "$newPath/"
                }
            }
            return remainingCommand
        }

        return trimmedCommand
    }

    /**
     * Build command with cd prefix if not in root.
     */
    private fun buildOtgCommand(commandText: String): String {
        return if (currentDir != "/") {
            "cd '$currentDir' && $commandText"
        } else {
            commandText
        }
    }

    override fun runOtgCommand(command: String): Flow<OutputLine> = flow {
        val sanitized = sanitizeCommand(command)
        val actualCommand = handleCdCommand(sanitized)

        if (actualCommand == null) {
            emit(OutputLine("Changed directory to: $currentDir", isError = false))
            return@flow
        }

        val fullCommand = buildOtgCommand(actualCommand)

        val connection = getAdbConnection() ?: run {
            emit(OutputLine("No OTG ADB connection", isError = true))
            return@flow
        }

        try {
            adbStream = connection.open("shell:$fullCommand")

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
            Log.e(TAG, "Error closing ADB stream", e)
        }
    }

    override fun disconnect() {
        currentDevice = null
        dropConnection()
    }

    private fun isAdbDevice(device: UsbDevice): Boolean = findAdbInterface(device) != null

    // File browser support methods
    override fun isConnected(): Boolean = adbConnection?.isConnected() == true

    override fun getAdbConnection(): AdbConnection? = adbConnection?.takeIf { it.isConnected() }

    private companion object {
        const val TAG = "OtgRepository"
        const val PERMISSION_ACTION = "in.hridayan.ashell.USB_PERMISSION"
        const val PERMISSION_REQUEST_CODE = 0
        const val PERMISSION_COOLDOWN_MS = 20_000L
        const val PRIVATE_KEY_FILE = "private_key"
        const val PUBLIC_KEY_FILE = "public_key"
        const val ADB_INTERFACE_SUBCLASS = 66
        const val ADB_INTERFACE_PROTOCOL = 1
        const val CONNECT_TIMEOUT_MS = 30_000L
        val DISCONNECT_SETTLE_DELAY = 500.milliseconds
    }
}
