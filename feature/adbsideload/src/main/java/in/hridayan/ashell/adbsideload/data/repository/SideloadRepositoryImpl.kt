package `in`.hridayan.ashell.adbsideload.data.repository

import android.hardware.usb.UsbDevice
import android.net.Uri
import com.cgutman.adblib.AdbConnection
import `in`.hridayan.ashell.adbsideload.data.protocol.SideloadStreamer
import `in`.hridayan.ashell.adbsideload.data.usb.AdbUsbConnector
import `in`.hridayan.ashell.adbsideload.data.usb.UsbAdbDeviceMonitor
import `in`.hridayan.ashell.adbsideload.data.usb.displayName
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadError
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus
import `in`.hridayan.ashell.adbsideload.domain.protocol.SideloadReadiness
import `in`.hridayan.ashell.adbsideload.domain.repository.SideloadRepository
import `in`.hridayan.ashell.adbsideload.domain.session.SideloadForegroundSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SideloadRepositoryImpl @Inject constructor(
    private val monitor: UsbAdbDeviceMonitor,
    private val connector: AdbUsbConnector,
    private val streamer: SideloadStreamer,
    private val foregroundSession: SideloadForegroundSession,
) : SideloadRepository, UsbAdbDeviceMonitor.Listener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectMutex = Mutex()

    private val _connectionState = MutableStateFlow<SideloadState>(SideloadState.Idle)
    override val connectionState: StateFlow<SideloadState> = _connectionState.asStateFlow()

    private val _operation = MutableStateFlow(SideloadOperation())
    override val operation: StateFlow<SideloadOperation> = _operation.asStateFlow()

    @Volatile
    private var currentDevice: UsbDevice? = null

    @Volatile
    private var adbConnection: AdbConnection? = null

    private var connectJob: Job? = null
    private var sideloadJob: Job? = null

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
        val redundant = SideloadReadiness.isScanRedundant(
            state = _connectionState.value,
            isConnecting = connectJob?.isActive == true,
            hasLiveConnection = adbConnection?.isConnected() == true,
        )
        if (redundant) return

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

    /**
     * Runs on the repository's own scope so the transfer outlives the screen that started it.
     */
    override fun sideload(uri: Uri) {
        if (_operation.value.status.isActive) return
        val connection = adbConnection?.takeIf { it.isConnected() }
        if (connection == null) {
            _operation.value = SideloadOperation(
                status = SideloadStatus.ERROR,
                error = SideloadError.NO_DEVICE_CONNECTED,
            )
            return
        }
        _operation.value = SideloadOperation(status = SideloadStatus.READING_FILE)
        foregroundSession.start()
        sideloadJob?.cancel()
        sideloadJob = scope.launch {
            streamer.stream(connection, uri).collect { progress -> _operation.value = progress }
        }
    }

    override fun cancelSideload() {
        sideloadJob?.cancel()
        sideloadJob = null
        streamer.cancel()
        _operation.value = _operation.value.copy(status = SideloadStatus.CANCELLED)
        foregroundSession.stop()
    }

    override fun resetOperation() {
        sideloadJob?.cancel()
        sideloadJob = null
        _operation.value = SideloadOperation()
        foregroundSession.stop()
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
            _connectionState.value = SideloadReadiness.stateFor(device.displayName(), live.banner)
            return
        }
        closeConnection()
        _connectionState.value = SideloadState.Connecting
        when (val result = connector.connect(device)) {
            is AdbUsbConnector.ConnectResult.Success -> {
                result.connection.setConnectionListener(::onConnectionClosed)
                adbConnection = result.connection
                _connectionState.value =
                    SideloadReadiness.stateFor(device.displayName(), result.connection.banner)
            }

            is AdbUsbConnector.ConnectResult.Failure ->
                _connectionState.value = SideloadState.Error(result.error, result.detail)
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
