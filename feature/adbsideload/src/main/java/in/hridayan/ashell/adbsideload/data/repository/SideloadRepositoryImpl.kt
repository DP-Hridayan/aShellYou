package `in`.hridayan.ashell.adbsideload.data.repository

import android.content.Context
import android.hardware.usb.UsbDevice
import android.net.Uri
import com.cgutman.adblib.AdbConnection
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.adbsideload.data.file.SideloadPackage
import `in`.hridayan.ashell.adbsideload.data.protocol.SideloadStreamer
import `in`.hridayan.ashell.adbsideload.data.usb.AdbUsbConnector
import `in`.hridayan.ashell.adbsideload.data.usb.UsbAdbDeviceMonitor
import `in`.hridayan.ashell.adbsideload.data.usb.displayName
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadError
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadPackageInfo
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus
import `in`.hridayan.ashell.adbsideload.domain.repository.SideloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SideloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val monitor: UsbAdbDeviceMonitor,
    private val connector: AdbUsbConnector,
    private val streamer: SideloadStreamer,
) : SideloadRepository, UsbAdbDeviceMonitor.Listener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectMutex = Mutex()

    private val _connectionState = MutableStateFlow<SideloadState>(SideloadState.Idle)
    override val connectionState: StateFlow<SideloadState> = _connectionState.asStateFlow()

    @Volatile
    private var currentDevice: UsbDevice? = null

    @Volatile
    private var adbConnection: AdbConnection? = null

    private var connectJob: Job? = null

    init {
        if (monitor.usbManager == null) {
            _connectionState.value = SideloadState.UsbManagerUnavailable
        } else {
            monitor.start(this)
        }
    }

    override fun searchDevices() {
        if (monitor.usbManager == null) {
            _connectionState.value = SideloadState.UsbManagerUnavailable
            return
        }
        if (isScanRedundant()) return
        val device = monitor.findAdbDevice() ?: run {
            _connectionState.value = SideloadState.Idle
            return
        }
        if (_connectionState.value !is SideloadState.DeviceFound &&
            _connectionState.value !is SideloadState.WrongMode
        ) {
            _connectionState.value = SideloadState.Searching
        }
        onDeviceAttached(device, monitor.hasPermission(device))
    }

    override fun retryPermission() {
        monitor.clearPermissionCooldown()
        searchDevices()
    }

    private fun isScanRedundant(): Boolean {
        if (connectJob?.isActive == true) return true
        return when (_connectionState.value) {
            is SideloadState.Connecting -> true
            is SideloadState.Connected -> adbConnection?.isConnected() == true
            else -> false
        }
    }

    private companion object {
        const val BANNER_SEPARATOR = "::"
        const val BOOTED_DEVICE_MODE = "device"
    }

    override fun onDeviceAttached(device: UsbDevice, hasPermission: Boolean) {
        currentDevice = device
        if (hasPermission) {
            connect(device)
        } else {
            _connectionState.value = SideloadState.DeviceFound(device.displayName())
            monitor.requestPermission(device)
        }
    }

    override fun onDeviceDetached(device: UsbDevice) {
        if (device.deviceName != currentDevice?.deviceName) return
        currentDevice = null
        dropConnection(SideloadState.Disconnected)
    }

    override fun onPermissionResult(device: UsbDevice, granted: Boolean) {
        if (granted) {
            currentDevice = device
            connect(device)
        } else {
            _connectionState.value = SideloadState.PermissionDenied
        }
    }

    override fun disconnect() {
        currentDevice = null
        dropConnection(SideloadState.Idle)
    }

    override suspend fun inspectPackage(uri: Uri): SideloadPackageInfo? = withContext(Dispatchers.IO) {
        SideloadPackage.inspect(context, uri)
    }

    override fun sideload(uri: Uri): Flow<SideloadOperation> {
        val connection = adbConnection?.takeIf { it.isConnected() }
            ?: return flowOf(
                SideloadOperation(status = SideloadStatus.ERROR, error = SideloadError.NO_DEVICE_CONNECTED)
            )
        return streamer.stream(connection, uri)
    }

    override fun cancelSideload() {
        streamer.cancel()
    }

    private fun connect(device: UsbDevice) {
        if (connectJob?.isActive == true) return
        connectJob = scope.launch {
            connectMutex.withLock { connectLocked(device) }
        }
    }

    private suspend fun connectLocked(device: UsbDevice) {
        val live = adbConnection?.takeIf { it.isConnected() }
        if (live != null) {
            publishReadiness(live, device)
            return
        }
        closeConnection()
        _connectionState.value = SideloadState.Connecting
        when (val result = connector.connect(device)) {
            is AdbUsbConnector.ConnectResult.Success -> attach(result.connection, device)
            is AdbUsbConnector.ConnectResult.Failure ->
                _connectionState.value = SideloadState.Error(result.error, result.detail)
        }
    }

    private fun attach(connection: AdbConnection, device: UsbDevice) {
        connection.setConnectionListener(::onConnectionClosed)
        adbConnection = connection
        publishReadiness(connection, device)
    }

    /**
     * Sideload mode and ordinary USB debugging expose the same USB interface, so the connect
     * banner decides: adbd on a booted device reports "device", recovery reports "recovery" or
     * "sideload".
     */
    private fun publishReadiness(connection: AdbConnection, device: UsbDevice) {
        val name = device.displayName()
        val mode = connection.banner.substringBefore(BANNER_SEPARATOR).trim()
        _connectionState.value = if (mode.equals(BOOTED_DEVICE_MODE, ignoreCase = true)) {
            SideloadState.WrongMode(name)
        } else {
            SideloadState.Connected(name)
        }
    }

    private fun onConnectionClosed(closed: AdbConnection) {
        if (closed !== adbConnection) return
        adbConnection = null
        streamer.cancel()
        val state = _connectionState.value
        if (state is SideloadState.Connected ||
            state is SideloadState.Connecting ||
            state is SideloadState.WrongMode
        ) {
            _connectionState.value = SideloadState.Disconnected
        }
    }

    private fun dropConnection(next: SideloadState) {
        connectJob?.cancel()
        scope.launch {
            connectMutex.withLock {
                closeConnection()
                _connectionState.value = next
            }
        }
    }

    private fun closeConnection() {
        val connection = adbConnection ?: return
        adbConnection = null
        streamer.cancel()
        runCatching { connection.close() }
    }
}
