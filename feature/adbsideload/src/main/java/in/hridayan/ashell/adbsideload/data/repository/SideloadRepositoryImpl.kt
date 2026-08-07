@file:Suppress("BlockingMethodInNonBlockingContext")

package `in`.hridayan.ashell.adbsideload.data.repository

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
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import com.cgutman.adblib.AdbBase64
import com.cgutman.adblib.AdbConnection
import com.cgutman.adblib.AdbCrypto
import com.cgutman.adblib.AdbStream
import com.cgutman.adblib.UsbChannel
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadConnection
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus
import `in`.hridayan.ashell.adbsideload.domain.repository.SideloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class SideloadRepositoryImpl(private val context: Context) : SideloadRepository {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager

    private val permissionAction = "in.hridayan.ashell.SIDELOAD_USB_PERMISSION"

    private var currentDevice: UsbDevice? = null
    private var adbConnection: AdbConnection? = null
    private var adbCrypto: AdbCrypto? = null
    private var sideloadStream: AdbStream? = null
    private var sideloadJob: Job? = null
    @Volatile
    private var isConnecting = false

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != permissionAction) return
            val device = extractUsbDevice(intent) ?: return
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            if (granted && usbManager?.hasPermission(device) == true) {
                connectToDevice(device)
            } else {
                SideloadConnection.updateState(SideloadState.PermissionDenied)
            }
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            val device = extractUsbDevice(intent) ?: return
            when (action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> handleDeviceAttach(device)
                UsbManager.ACTION_USB_DEVICE_DETACHED -> handleDeviceDetach(device)
            }
        }
    }

    init {
        if (usbManager == null) {
            SideloadConnection.updateState(SideloadState.UsbManagerUnavailable)
        } else {
            registerReceivers()
            initAdbCrypto()
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
            context, usbReceiver, usbFilter, ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun initAdbCrypto() {
        val base64 = AdbBase64 { data -> Base64.encodeToString(data, Base64.NO_WRAP) }
        try {
            val priv = File(context.filesDir, "private_key")
            val pub = File(context.filesDir, "public_key")
            adbCrypto = if (priv.exists() && pub.exists()) {
                AdbCrypto.loadAdbKeyPair(base64, priv, pub)
            } else {
                AdbCrypto.generateAdbKeyPair(base64).apply { saveAdbKeyPair(priv, pub) }
            }
        } catch (e: Exception) {
            Log.e("SideloadRepository", "Failed to init AdbCrypto", e)
        }
    }

    private fun extractUsbDevice(intent: Intent): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }

    private fun handleDeviceAttach(device: UsbDevice) {
        if (!isAdbDevice(device)) return
        currentDevice = device
        val manager = usbManager ?: run {
            SideloadConnection.updateState(SideloadState.UsbManagerUnavailable)
            return
        }
        val name = device.productName ?: device.manufacturerName ?: device.deviceName
        if (manager.hasPermission(device)) {
            connectToDevice(device)
        } else {
            SideloadConnection.updateState(SideloadState.DeviceFound(name))
            requestPermission(device)
        }
    }

    private fun handleDeviceDetach(device: UsbDevice) {
        if (device != currentDevice) return
        CoroutineScope(Dispatchers.IO).launch {
            disconnect()
            delay(500.milliseconds)
            SideloadConnection.updateState(SideloadState.Idle)
        }
    }

    override fun searchDevices() {
        if (usbManager == null) {
            SideloadConnection.updateState(SideloadState.UsbManagerUnavailable)
            return
        }
        SideloadConnection.updateState(SideloadState.Searching)
        checkConnectedDevices()
    }

    private fun checkConnectedDevices() {
        val manager = usbManager ?: run {
            SideloadConnection.updateState(SideloadState.UsbManagerUnavailable)
            return
        }
        val adbDevice = manager.deviceList.values.firstOrNull { isAdbDevice(it) }
        if (adbDevice != null) {
            handleDeviceAttach(adbDevice)
        }
    }

    private fun requestPermission(device: UsbDevice) {
        val manager = usbManager ?: return
        if (manager.hasPermission(device)) return
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(permissionAction), PendingIntent.FLAG_IMMUTABLE
        )
        manager.requestPermission(device, pendingIntent)
    }

    private fun connectToDevice(device: UsbDevice) {
        val currentState = SideloadConnection.currentState
        if (isConnecting || currentState is SideloadState.Connected || currentState is SideloadState.Connecting) return
        isConnecting = true
        SideloadConnection.updateState(SideloadState.Connecting)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                runCatching { adbConnection?.close() }
                adbConnection = null
                val intf = findAdbInterface(device) ?: run {
                    onConnectError("No ADB interface found")
                    return@launch
                }
                val manager = usbManager ?: run {
                    isConnecting = false
                    SideloadConnection.updateState(SideloadState.UsbManagerUnavailable)
                    return@launch
                }
                val usbConn = manager.openDevice(device) ?: run {
                    onConnectError("Failed to open USB connection")
                    return@launch
                }
                if (!usbConn.claimInterface(intf, true)) {
                    onConnectError("Failed to claim USB interface")
                    return@launch
                }
                val channel = UsbChannel(usbConn, intf)
                val crypto = adbCrypto ?: run {
                    onConnectError("ADB crypto not initialized")
                    return@launch
                }
                withTimeout(CONNECT_TIMEOUT_MS) {
                    runInterruptible {
                        adbConnection = AdbConnection.create(channel, crypto).apply { connect() }
                    }
                }
                val name = device.productName ?: device.manufacturerName ?: device.deviceName
                isConnecting = false
                SideloadConnection.updateState(SideloadState.Connected(name))
            } catch (e: TimeoutCancellationException) {
                isConnecting = false
                SideloadConnection.updateState(SideloadState.Error("Connection timed out. Check recovery screen for RSA auth prompt."))
            } catch (e: Exception) {
                isConnecting = false
                SideloadConnection.updateState(SideloadState.Error("Connection failed: ${e.message}"))
                Log.e("SideloadRepository", "ADB connection error", e)
            }
        }
    }

    private fun onConnectError(message: String) {
        isConnecting = false
        SideloadConnection.updateState(SideloadState.Error(message))
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

    override fun sideload(
        uri: Uri,
        onProgress: (SideloadOperation) -> Unit
    ): Flow<SideloadOperation> = flow {
        val connection = adbConnection ?: run {
            val err =
                SideloadOperation(status = SideloadStatus.ERROR, message = "No device connected")
            onProgress(err)
            emit(err)
            return@flow
        }

        val fileName = resolveFileName(uri)
        val fileSize = resolveFileSize(uri)

        if (fileSize <= 0L) {
            val err = SideloadOperation(
                fileName = fileName,
                status = SideloadStatus.ERROR,
                message = "Cannot determine file size"
            )
            onProgress(err)
            emit(err)
            return@flow
        }

        onProgress(
            SideloadOperation(
                fileName = fileName,
                status = SideloadStatus.READING_FILE,
                message = "Preparing file..."
            )
        )

        try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                ?: throw IOException("Cannot open file descriptor")

            pfd.use { descriptor ->
                val fileChannel = FileInputStream(descriptor.fileDescriptor).channel
                fileChannel.use { channel ->
                    val result = executeSideloadProtocol(
                        connection = connection,
                        fileChannel = channel,
                        fileSize = fileSize,
                        fileName = fileName,
                        onProgress = onProgress
                    )
                    onProgress(result)
                    emit(result)
                }
            }
        } catch (e: Exception) {
            val err = SideloadOperation(
                fileName = fileName,
                status = SideloadStatus.ERROR,
                message = e.message ?: "Unknown error"
            )
            onProgress(err)
            emit(err)
            Log.e("SideloadRepository", "Sideload error", e)
        }
    }.flowOn(Dispatchers.IO)

    private fun resolveFileName(uri: Uri): String {
        if (uri.scheme == "content") {
            context.contentResolver.query(
                uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (col >= 0) return cursor.getString(col)
                }
            }
        }
        return uri.path?.substringAfterLast('/') ?: "package.zip"
    }

    private fun resolveFileSize(uri: Uri): Long {
        return context.contentResolver.query(
            uri, arrayOf(OpenableColumns.SIZE), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else -1L
        } ?: -1L
    }

    private suspend fun executeSideloadProtocol(
        connection: AdbConnection,
        fileChannel: java.nio.channels.FileChannel,
        fileSize: Long,
        fileName: String,
        onProgress: (SideloadOperation) -> Unit
    ): SideloadOperation {
        val blockSize = 65536
        val totalBlocks = ((fileSize + blockSize - 1) / blockSize).toInt()
        val serviceString = "sideload-host:$fileSize:$blockSize"

        val stream = try {
            withTimeout(OPEN_TIMEOUT_MS) {
                runInterruptible { connection.open(serviceString) }
            }
        } catch (e: TimeoutCancellationException) {
            return SideloadOperation(
                fileName = fileName,
                status = SideloadStatus.ERROR,
                message = "Recovery not responding. Select 'Apply from ADB' in OrangeFox/TWRP first."
            )
        } catch (e: Exception) {
            return SideloadOperation(
                fileName = fileName,
                status = SideloadStatus.ERROR,
                message = "Failed to open sideload stream: ${e.message}"
            )
        }

        sideloadStream = stream

        onProgress(
            SideloadOperation(
                fileName = fileName,
                status = SideloadStatus.READING_FILE,
                message = "Recovery connected, starting transfer..."
            )
        )

        return try {
            serveBlockRequests(
                stream = stream,
                fileChannel = fileChannel,
                fileSize = fileSize,
                blockSize = blockSize,
                totalBlocks = totalBlocks,
                fileName = fileName,
                onProgress = onProgress
            )
        } finally {
            runCatching { stream.close() }
            sideloadStream = null
        }
    }

    private suspend fun serveBlockRequests(
        stream: AdbStream,
        fileChannel: java.nio.channels.FileChannel,
        fileSize: Long,
        blockSize: Int,
        totalBlocks: Int,
        fileName: String,
        onProgress: (SideloadOperation) -> Unit
    ): SideloadOperation {
        val blockBuffer = java.nio.ByteBuffer.allocate(blockSize)
        val requestBuffer = StringBuilder()
        var startTime = System.currentTimeMillis()
        var bytesSent = 0L

        while (true) {
            val packet = try {
                withTimeout(READ_TIMEOUT_MS) {
                    runInterruptible { stream.read() }
                }
            } catch (e: TimeoutCancellationException) {
                return SideloadOperation(
                    fileName = fileName,
                    status = SideloadStatus.ERROR,
                    message = "Device stopped responding. Re-enter sideload mode and retry."
                )
            } ?: break
            requestBuffer.append(String(packet, Charsets.UTF_8))

            val response = requestBuffer.toString()

            if (response.contains("DONEDONE")) {
                return SideloadOperation(
                    fileName = fileName,
                    status = SideloadStatus.COMPLETE,
                    progress = 1f,
                    bytesSent = fileSize,
                    totalBytes = fileSize,
                    currentBlock = totalBlocks,
                    totalBlocks = totalBlocks,
                    message = "Sideload complete"
                )
            }

            if (response.contains("FAILFAIL")) {
                return SideloadOperation(
                    fileName = fileName,
                    status = SideloadStatus.ERROR,
                    message = "Recovery reported failure"
                )
            }

            val blockNumStr = response.trim()
            if (blockNumStr.length >= 8) {
                val blockIndex = blockNumStr.substring(0, 8).trim().toIntOrNull()
                if (blockIndex != null) {
                    requestBuffer.clear()
                    if (blockIndex < requestBuffer.length) {
                        requestBuffer.delete(0, 9.coerceAtMost(requestBuffer.length))
                    } else {
                        requestBuffer.clear()
                    }

                    val offset = blockIndex.toLong() * blockSize
                    blockBuffer.clear()
                    fileChannel.position(offset)
                    val bytesRead = fileChannel.read(blockBuffer)
                    if (bytesRead > 0) {
                        writeBlockInChunks(stream, blockBuffer.array().copyOf(bytesRead))
                        bytesSent += bytesRead

                        val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
                        val rateMBps = (bytesSent.toFloat() / (1024f * 1024f)) / (elapsed / 1000f)
                        val progress = bytesSent.toFloat() / fileSize.toFloat()

                        onProgress(
                            SideloadOperation(
                                fileName = fileName,
                                status = SideloadStatus.SENDING,
                                progress = progress.coerceIn(0f, 1f),
                                bytesSent = bytesSent,
                                totalBytes = fileSize,
                                transferRateMBps = rateMBps,
                                currentBlock = blockIndex + 1,
                                totalBlocks = totalBlocks,
                                message = "Sending block ${blockIndex + 1} / $totalBlocks"
                            )
                        )
                    }
                }
            }
        }

        return SideloadOperation(
            fileName = fileName,
            status = SideloadStatus.ERROR,
            message = "Stream closed unexpectedly"
        )
    }

    private fun writeBlockInChunks(stream: AdbStream, data: ByteArray) {
        var offset = 0
        while (offset < data.size) {
            val end = minOf(offset + WRITE_CHUNK_SIZE, data.size)
            stream.write(data.copyOfRange(offset, end))
            offset = end
        }
    }

    override fun cancelSideload() {
        sideloadJob?.cancel()
        sideloadJob = null
        runCatching { sideloadStream?.close() }
        sideloadStream = null
    }

    override fun disconnect() {
        isConnecting = false
        runCatching { adbConnection?.close() }
        adbConnection = null
        currentDevice = null
        SideloadConnection.updateState(SideloadState.Disconnected)
        SideloadConnection.updateState(SideloadState.Idle)
    }

    override fun unRegister() {
        runCatching { context.unregisterReceiver(permissionReceiver) }
        runCatching { context.unregisterReceiver(usbReceiver) }
    }

    companion object {
        private const val WRITE_CHUNK_SIZE = 4096
        private const val OPEN_TIMEOUT_MS = 30_000L
        private const val CONNECT_TIMEOUT_MS = 30_000L
        private const val READ_TIMEOUT_MS = 60_000L
    }
}
