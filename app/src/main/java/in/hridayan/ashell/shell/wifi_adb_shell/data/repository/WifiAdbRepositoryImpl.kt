package `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import `in`.hridayan.ashell.shell.common.domain.model.OutputLine
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.database.WifiAdbDeviceDao
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.mapper.toDomainList
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.mapper.toEntity
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbConnection
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import io.github.muntashirakon.adb.AdbStream
import io.github.muntashirakon.adb.android.AndroidUtils
import io.nayuki.qrcodegen.QrCode
import io.nayuki.qrcodegen.QrCode.Ecc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import kotlin.math.max

class WifiAdbRepositoryImpl(
    private val context: Context,
    private val deviceDao: WifiAdbDeviceDao
) : WifiAdbRepository {

    private val TAG = "WifiAdbShell"
    private var adbShellStream: AdbStream? = null
    private val executor = Executors.newScheduledThreadPool(1)
    private var jmDns: JmDNS? = null
    private val pairingInProgress = mutableSetOf<String>()
    private val connectInProgress = mutableSetOf<String>()

    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    // Reconnect cancellation state
    @Volatile
    private var isReconnectCancelled = false

    @Volatile
    private var currentReconnectingDeviceId: String? = null
    private var activeNsdManager: NsdManager? = null
    private var activeDiscoveryListener: NsdManager.DiscoveryListener? = null
    private var activeReconnectTimeout: java.util.concurrent.ScheduledFuture<*>? = null

    // Heartbeat mechanism
    private var heartbeatJob: kotlinx.coroutines.Job? = null

    @Volatile
    private var isHeartbeatRunning = false

    override fun discoverAdbPairingService(
        pairingCode: String,
        autoPair: Boolean,
        callback: MdnsDiscoveryCallback?
    ) {
        executor.submit {
            try {
                jmDns?.close()

                val wifi =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val lock = wifi.createMulticastLock("adb_mdns_lock")
                lock.setReferenceCounted(true)
                lock.acquire()

                val addr = AndroidUtils.getHostIpAddress(context)
                jmDns = JmDNS.create(InetAddress.getByName(addr))

                val listener = object : ServiceListener {
                    override fun serviceAdded(event: ServiceEvent) {
                        jmDns?.getServiceInfo(event.type, event.name)
                    }

                    override fun serviceRemoved(event: ServiceEvent) {
                        Log.d(TAG, "Service lost: ${event.name}")
                        callback?.onServiceLost(event.name)
                    }

                    override fun serviceResolved(event: ServiceEvent) {
                        val info = event.info
                        val ip = info.inet4Addresses.firstOrNull()?.hostAddress ?: return
                        val port = info.port
                        val key = "$ip:$port"

                        if (pairingInProgress.contains(key)) {
                            Log.d(TAG, "Pairing already in progress for $key, skipping...")
                            return
                        }

                        Log.d(TAG, "Found service: ${event.name} at $ip:$port")
                        callback?.onServiceFound(event.name, ip, port)

                        if (autoPair && event.type.contains("_adb-tls-pairing")) {
                            pairingInProgress.add(key)

                            pair(ip, port, pairingCode, object : PairingListener {
                                override fun onPairingSuccess() {
                                    pairingInProgress.remove(key)
                                    Log.d(TAG, "Pairing succeeded for $key!")

                                    // Don't save device here - only save after successful CONNECTION
                                    // This prevents showing non-functional reconnect tiles for paired-but-not-connected devices

                                    callback?.onPairingSuccess(ip, port)

                                    executor.schedule({
                                        Log.d(TAG, "Discovering ADB connect service after delay...")
                                        mainScope.launch {
                                            WifiAdbConnection.updateState(
                                                WifiAdbState.DiscoveryStarted("connect service discovery started")
                                            )
                                        }
                                        // Pass the paired device IP for filtering and fallback direct connect
                                        discoverConnectService(callback, ip)
                                    }, 2, TimeUnit.SECONDS)
                                }


                                override fun onPairingFailed() {
                                    pairingInProgress.remove(key)
                                    Log.d(TAG, "Pairing failed for $key!")
                                    callback?.onPairingFailed(ip, port)
                                }
                            })
                        }
                    }
                }

                jmDns?.addServiceListener("_adb-tls-pairing._tcp.local.", listener)
                Log.d(TAG, "Started mDNS discovery for ADB pairing...")

            } catch (e: Throwable) {
                Log.e(TAG, "mDNS discovery failed", e)
                callback?.onError(e)
            }
        }
    }

    /**
     * After pairing, discover the normal adb-tls-connect service.
     * @param targetIp IP of device we just paired with - for fallback direct connection
     */
    @Suppress("DEPRECATION")
    @SuppressLint("DefaultLocale")
    private fun discoverConnectService(callback: MdnsDiscoveryCallback?, targetIp: String? = null) {
        var foundService = false

        executor.submit {
            try {
                val wifi =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val lock = wifi.createMulticastLock("adb_mdns_lock_connect")
                lock.setReferenceCounted(true)
                lock.acquire()

                val wifiInfo = wifi.connectionInfo
                val ipAddress = String.format(
                    "%d.%d.%d.%d",
                    wifiInfo.ipAddress and 0xff,
                    wifiInfo.ipAddress shr 8 and 0xff,
                    wifiInfo.ipAddress shr 16 and 0xff,
                    wifiInfo.ipAddress shr 24 and 0xff
                )
                Log.d(
                    TAG,
                    "Creating new JmDNS for connect on $ipAddress (paired device: $targetIp)"
                )

                jmDns?.close()
                jmDns = JmDNS.create(InetAddress.getByName(ipAddress))

                jmDns?.addServiceListener("_adb-tls-connect._tcp.local.", object : ServiceListener {
                    override fun serviceAdded(event: ServiceEvent) {
                        jmDns?.getServiceInfo(event.type, event.name)
                    }

                    override fun serviceRemoved(event: ServiceEvent) {
                        Log.d(TAG, "ADB connect service lost: ${event.name}")
                    }

                    override fun serviceResolved(event: ServiceEvent) {
                        val info = event.info
                        val ip = info.inet4Addresses.firstOrNull()?.hostAddress ?: return
                        val port = info.port
                        val key = "$ip:$port"

                        Log.d(TAG, "Found ADB connect service at $key")

                        if (connectInProgress.contains(key)) {
                            Log.d(TAG, "Connection already in progress for $key, skipping...")
                            return
                        }

                        // If we have a target IP, prefer that device
                        // But also accept others if target device isn't advertising
                        if (targetIp != null && ip != targetIp) {
                            Log.d(
                                TAG,
                                "Found $key but looking for $targetIp, will try this if target not found..."
                            )
                            // Don't skip immediately - schedule a fallback connection
                            return
                        }

                        foundService = true
                        mainScope.launch {
                            WifiAdbConnection.updateState(WifiAdbState.DiscoveryFound(key))
                        }

                        if (event.type.contains("_adb-tls-connect")) {
                            connectInProgress.add(key)

                            mainScope.launch {
                                WifiAdbConnection.updateState(WifiAdbState.ConnectStarted(key))
                            }

                            connect(ip, port, object : ConnectionListener {
                                override fun onConnectionSuccess() {
                                    connectInProgress.remove(key)

                                    // Retrieve device serial and name for proper identification
                                    val serial = getDeviceSerialNumber()
                                    val deviceName = getDeviceName()
                                    Log.d(TAG, "Device info - Serial: $serial, Name: $deviceName")

                                    // Save device with serial number for unique identification
                                    val connectedDevice = WifiAdbDevice(
                                        ip = ip,
                                        port = port,
                                        deviceName = deviceName,
                                        isPaired = true,
                                        lastConnected = System.currentTimeMillis(),
                                        serialNumber = serial
                                    )
                                    ioScope.launch { deviceDao.insertDevice(connectedDevice.toEntity()) }
                                    currentDevice = connectedDevice
                                    WifiAdbConnection.setCurrentDevice(connectedDevice)
                                    Log.d(
                                        TAG,
                                        "Saved device: ${connectedDevice.id} (${connectedDevice.deviceName})"
                                    )

                                    mainScope.launch {
                                        WifiAdbConnection.updateState(
                                            WifiAdbState.ConnectSuccess(key)
                                        )
                                    }
                                    Log.d(TAG, "Connected successfully to $ip:$port")
                                    callback?.onPairingSuccess(ip, port)
                                }

                                override fun onConnectionFailed() {
                                    connectInProgress.remove(key)
                                    mainScope.launch {
                                        WifiAdbConnection.updateState(
                                            WifiAdbState.PairConnectFailed(
                                                key
                                            )
                                        )
                                    }
                                    Log.e(TAG, "Failed to connect to $ip:$port")
                                    callback?.onPairingFailed(ip, port)
                                }
                            })
                        }
                    }
                })

                Log.d(TAG, "Started discovery for _adb-tls-connect._tcp.local.")

                // Fallback: If we have a target IP and mDNS doesn't find it, try direct connection
                if (targetIp != null) {
                    executor.schedule({
                        if (!foundService) {
                            Log.d(
                                TAG,
                                "mDNS didn't find $targetIp, trying direct connection on common ports..."
                            )

                            // Try multiple common ADB ports as fallback
                            val portsToTry = listOf(5555, 37373, 42069, 5037)
                            var connected = false
                            val manager = AdbConnectionManager.getInstance(context)

                            for (port in portsToTry) {
                                if (connected) break

                                Log.d(TAG, "Trying direct connect to $targetIp:$port...")
                                mainScope.launch {
                                    WifiAdbConnection.updateState(WifiAdbState.ConnectStarted("$targetIp:$port (direct)"))
                                }

                                try {
                                    val success = manager.connect(targetIp, port)
                                    if (success) {
                                        connected = true

                                        // Retrieve device serial and name
                                        val serial = getDeviceSerialNumber()
                                        val deviceName = getDeviceName()
                                        Log.d(
                                            TAG,
                                            "Device info (direct) - Serial: $serial, Name: $deviceName"
                                        )

                                        val connectedDevice = WifiAdbDevice(
                                            ip = targetIp,
                                            port = port,
                                            deviceName = deviceName,
                                            isPaired = true,
                                            lastConnected = System.currentTimeMillis(),
                                            serialNumber = serial
                                        )
                                        ioScope.launch { deviceDao.insertDevice(connectedDevice.toEntity()) }
                                        currentDevice = connectedDevice
                                        WifiAdbConnection.setCurrentDevice(connectedDevice)

                                        mainScope.launch {
                                            WifiAdbConnection.updateState(
                                                WifiAdbState.ConnectSuccess("$targetIp:$port")
                                            )
                                        }
                                        Log.d(
                                            TAG,
                                            "Direct connection to $targetIp:$port succeeded!"
                                        )
                                        callback?.onPairingSuccess(targetIp, port)
                                        break
                                    }
                                } catch (e: Exception) {
                                    Log.d(TAG, "Port $port failed: ${e.message}")
                                }
                            }

                            if (!connected) {
                                // All ports failed - do NOT save device, only save successfully connected ones
                                Log.e(TAG, "All direct connection attempts failed for $targetIp")

                                mainScope.launch {
                                    WifiAdbConnection.updateState(
                                        WifiAdbState.PairConnectFailed("Paired but connect failed - try Manual Pair with correct port")
                                    )
                                }
                                callback?.onPairingFailed(targetIp, 0)
                            }
                        }
                    }, 8, TimeUnit.SECONDS)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error discovering connect service", e)
            }
        }
    }


    override fun pair(ip: String, port: Int, pairingCode: String, listener: PairingListener?) {
        executor.submit {
            try {
                val manager = AdbConnectionManager.getInstance(context)
                val status = manager.pair(ip, port, pairingCode)
                if (status) {
                    listener?.onPairingSuccess()
                } else {
                    listener?.onPairingFailed()
                }
            } catch (e: Throwable) {
                Log.e(TAG, "pair() failed", e)
                listener?.onPairingFailed()
            }
        }
    }

    override fun connect(ip: String?, port: Int, callback: ConnectionListener?) {
        executor.submit {
            try {
                val manager = AdbConnectionManager.getInstance(context)
                val actualIp = ip ?: AndroidUtils.getHostIpAddress(context)
                val connected = manager.connect(actualIp, port)
                if (connected) {
                    // Retrieve device serial and name for proper identification
                    val serial = getDeviceSerialNumber()
                    val deviceName = getDeviceName()
                    Log.d(TAG, "Connected! Device info - Serial: $serial, Name: $deviceName")

                    // Save device with serial number
                    val connectedDevice = WifiAdbDevice(
                        ip = actualIp,
                        port = port,
                        deviceName = deviceName,
                        isPaired = true,
                        lastConnected = System.currentTimeMillis(),
                        serialNumber = serial
                    )
                    ioScope.launch { deviceDao.insertDevice(connectedDevice.toEntity()) }
                    currentDevice = connectedDevice
                    WifiAdbConnection.setCurrentDevice(connectedDevice)
                    Log.d(TAG, "Saved device: ${connectedDevice.id}")

                    callback?.onConnectionSuccess()
                } else callback?.onConnectionFailed()
            } catch (e: Throwable) {
                Log.e(TAG, "connect() failed", e)
                callback?.onConnectionFailed()
            }
        }
    }

    /**
     * Retrieves the device serial number by running 'getprop ro.serialno'
     * Returns null if unable to retrieve
     */
    private fun getDeviceSerialNumber(): String? {
        val manager = AdbConnectionManager.getInstance(context)
        if (!manager.isConnected) return null

        return try {
            val stream = manager.openStream("shell:getprop ro.serialno")
            val input = stream.openInputStream()
            val reader = BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8))
            val serial = reader.readLine()?.trim()
            stream.close()
            if (serial.isNullOrBlank()) null else serial
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get device serial", e)
            null
        }
    }

    /**
     * Retrieves or generates a device name by getting the device model
     */
    private fun getDeviceName(): String {
        val manager = AdbConnectionManager.getInstance(context)
        if (!manager.isConnected) return "Unknown Device"

        return try {
            val stream = manager.openStream("shell:getprop ro.product.model")
            val input = stream.openInputStream()
            val reader = BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8))
            val model = reader.readLine()?.trim()
            stream.close()
            if (model.isNullOrBlank()) "Unknown Device" else model
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get device name", e)
            "Unknown Device"
        }
    }

    @Volatile
    private var isAborted = false

    override fun execute(commandText: String): Flow<OutputLine> = flow {
        val manager = AdbConnectionManager.getInstance(context)

        if (!manager.isConnected) {
            emit(OutputLine("ADB not connected", isError = true))
            return@flow
        }

        isAborted = false

        try {
            // Close any existing stream first
            try {
                adbShellStream?.close()
            } catch (e: Exception) {
                // Ignore
            }

            // Use shell:command format for one-shot execution
            adbShellStream = manager.openStream("shell:$commandText")

            val input = adbShellStream!!.openInputStream()
            val reader = BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8))

            var line: String? = null
            while (!isAborted) {
                line = reader.readLine()
                if (line == null) break
                emit(OutputLine(line, isError = false))
            }

            Log.d(TAG, "Command completed. Aborted: $isAborted")

        } catch (e: Exception) {
            // Only emit error if not aborted
            if (!isAborted) {
                emit(OutputLine("Error: ${e.message}", isError = true))
                Log.e("WifiAdbShell", "execute() failed", e)
            } else {
                Log.d(TAG, "Command was aborted, ignoring exception: ${e.message}")
            }
        } finally {
            // Clean up stream after command completes
            try {
                adbShellStream?.close()
            } catch (e: Exception) {
                // Ignore
            }
            adbShellStream = null
            Log.d(TAG, "Shell stream cleaned up")
        }
    }.flowOn(Dispatchers.IO)


    override fun abortShell() {
        Log.d(TAG, "abortShell() called")
        isAborted = true
        try {
            adbShellStream?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing adbShellStream", e)
        }
        adbShellStream = null
    }


    override fun stopMdnsDiscovery() {
        try {
            jmDns?.close()
            jmDns = null
            // Only reset state if not currently connected - preserve existing connection
            mainScope.launch {
                val currentState = WifiAdbConnection.currentState
                if (currentState !is WifiAdbState.ConnectSuccess) {
                    WifiAdbConnection.updateState(WifiAdbState.None)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error closing JmDNS", e)
        }
    }

    override fun getSavedDevicesFlow(): Flow<List<WifiAdbDevice>> =
        deviceDao.getAllDevices().map { entities -> entities.toDomainList() }

    override suspend fun saveDevice(device: WifiAdbDevice) {
        deviceDao.insertDevice(device.toEntity())
    }

    override suspend fun updateDevice(device: WifiAdbDevice) {
        deviceDao.updateDevice(device.toEntity())
    }

    override suspend fun removeDevice(device: WifiAdbDevice) {
        deviceDao.deleteDevice(device.toEntity())
    }

    // ========== NEW RECONNECT FUNCTIONALITY ==========

    private var currentDevice: WifiAdbDevice? = null

    override fun cancelReconnect() {
        Log.d(TAG, "cancelReconnect() called")
        isReconnectCancelled = true
        currentReconnectingDeviceId = null

        // Cancel scheduled timeout
        activeReconnectTimeout?.let { future ->
            future.cancel(false)
            Log.d(TAG, "Cancelled reconnect timeout")
        }
        activeReconnectTimeout = null

        // Stop NSD discovery
        activeDiscoveryListener?.let { listener ->
            try {
                activeNsdManager?.stopServiceDiscovery(listener)
                Log.d(TAG, "Stopped NSD discovery")
            } catch (e: Exception) {
                // May throw if discovery already stopped
                Log.d(TAG, "Error stopping NSD discovery (may already be stopped): ${e.message}")
            }
        }
        activeDiscoveryListener = null
        activeNsdManager = null

        // Only reset state if not currently connected - preserve existing connection
        mainScope.launch {
            val currentState = WifiAdbConnection.currentState
            if (currentState !is WifiAdbState.ConnectSuccess) {
                WifiAdbConnection.updateState(WifiAdbState.None)
            }
        }
    }

    override fun reconnect(device: WifiAdbDevice, listener: ReconnectListener?) {
        // Cancel any ongoing reconnect to a different device
        val previousDeviceId = currentReconnectingDeviceId
        if (previousDeviceId != null && previousDeviceId != device.id) {
            Log.d(
                TAG,
                "Cancelling previous reconnect to $previousDeviceId, starting reconnect to ${device.id}"
            )
            // Cancel previous reconnect resources silently (don't update state to None)
            activeReconnectTimeout?.cancel(false)
            activeReconnectTimeout = null
            activeDiscoveryListener?.let { listener ->
                try {
                    activeNsdManager?.stopServiceDiscovery(listener)
                } catch (e: Exception) {
                    // Ignore - may already be stopped
                }
            }
            activeDiscoveryListener = null
            activeNsdManager = null
        }

        // Set current reconnecting device ID and reset cancellation flag
        currentReconnectingDeviceId = device.id
        isReconnectCancelled = false

        executor.submit {
            try {
                val manager = AdbConnectionManager.getInstance(context)

                // Check if already connected to this device
                if (manager.isConnected && currentDevice?.id == device.id) {
                    Log.d(TAG, "Already connected to ${device.id}")
                    currentReconnectingDeviceId = null
                    listener?.onReconnectSuccess()
                    return@submit
                }

                // Disconnect any existing connection first
                if (manager.isConnected) {
                    Log.d(TAG, "Disconnecting existing connection before reconnect...")
                    try {
                        manager.disconnect()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error disconnecting before reconnect", e)
                    }
                }

                mainScope.launch {
                    WifiAdbConnection.updateState(WifiAdbState.Reconnecting(device.id))
                }

                // For own device, get current local IP since it might have changed
                val targetIp = if (device.isOwnDevice) {
                    val wifi =
                        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val currentIp = getLocalIpAddress(wifi)
                    if (currentIp != null) {
                        Log.d(TAG, "Own device reconnect - using current local IP: $currentIp")
                        currentIp
                    } else {
                        Log.d(TAG, "Could not get current local IP, using saved: ${device.ip}")
                        device.ip
                    }
                } else {
                    device.ip
                }

                Log.d(TAG, "Attempting direct connect to $targetIp:${device.port}")

                // Try direct connect first with current IP and stored port
                try {
                    val connected = manager.connect(targetIp, device.port)
                    if (connected) {
                        Log.d(TAG, "Direct connect succeeded for ${device.id}")
                        // Update device with current IP in case it changed
                        currentDevice = device.copy(
                            ip = targetIp,
                            lastConnected = System.currentTimeMillis()
                        )
                        ioScope.launch { deviceDao.updateDevice(currentDevice!!.toEntity()) }
                        WifiAdbConnection.setCurrentDevice(currentDevice)
                        mainScope.launch {
                            WifiAdbConnection.updateState(
                                WifiAdbState.ConnectSuccess(
                                    device.id,
                                    device.id
                                )
                            )
                        }
                        listener?.onReconnectSuccess()
                        return@submit
                    } else {
                        Log.d(TAG, "Direct connect returned false for ${device.id}")
                    }
                } catch (e: io.github.muntashirakon.adb.AdbPairingRequiredException) {
                    // Device needs re-pairing - public key not saved on target
                    Log.d(TAG, "Device requires re-pairing: ${device.id}")
                    mainScope.launch {
                        WifiAdbConnection.updateState(
                            WifiAdbState.ConnectFailed(
                                "Requires re-pairing",
                                device.id
                            )
                        )
                    }
                    listener?.onReconnectFailed(requiresPairing = true)
                    return@submit
                } catch (e: Exception) {
                    Log.d(TAG, "Direct connect failed for ${device.id}: ${e.message}")
                }

                // Direct connect failed, try mDNS discovery for new port
                Log.d(TAG, "Direct connect failed, trying mDNS discovery for $targetIp...")
                discoverConnectServiceForReconnect(device, listener)

            } catch (e: Throwable) {
                Log.e(TAG, "reconnect() failed", e)
                mainScope.launch {
                    WifiAdbConnection.updateState(
                        WifiAdbState.ConnectFailed(
                            e.message ?: "Unknown error", device.id
                        )
                    )
                }
                listener?.onReconnectFailed(requiresPairing = false)
            }
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("DefaultLocale")
    private fun discoverConnectServiceForReconnect(
        device: WifiAdbDevice,
        listener: ReconnectListener?
    ) {
        try {
            val nsdManager =
                context.getSystemService(Context.NSD_SERVICE) as NsdManager

            // Get current local IP for own device
            val wifi =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val currentLocalIp = getLocalIpAddress(wifi)

            // For own device, use current local IP since it might change between sessions
            val targetIp = if (device.isOwnDevice && currentLocalIp != null) {
                Log.d(TAG, "Own device reconnect - using current local IP: $currentLocalIp")
                currentLocalIp
            } else {
                Log.d(TAG, "Other device reconnect - using saved IP: ${device.ip}")
                device.ip
            }
            // Track state
            var connectionHandled = false

            // Check if already cancelled before starting
            if (isReconnectCancelled) {
                Log.d(TAG, "Reconnect was cancelled before NSD discovery started")
                return
            }
            var discoveryListener: NsdManager.DiscoveryListener? = null

            // Track the device ID we're reconnecting to for this attempt
            val reconnectDeviceId = device.id

            // Set timeout for discovery
            activeReconnectTimeout = executor.schedule({
                // Only update state if we're still reconnecting to the same device
                if (connectionHandled || isReconnectCancelled || currentReconnectingDeviceId != reconnectDeviceId) {
                    Log.d(
                        TAG,
                        "Ignoring timeout - handled: $connectionHandled, cancelled: $isReconnectCancelled, currentDevice: $currentReconnectingDeviceId, thisDevice: $reconnectDeviceId"
                    )
                    return@schedule
                }
                connectionHandled = true
                currentReconnectingDeviceId = null

                Log.d(TAG, "NsdManager discovery timeout for reconnect")

                // Stop discovery
                try {
                    activeDiscoveryListener?.let { nsdManager.stopServiceDiscovery(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping discovery on timeout", e)
                }
                activeDiscoveryListener = null
                activeNsdManager = null
                activeReconnectTimeout = null

                mainScope.launch {
                    // For own device, timeout likely means wireless debugging is off
                    if (device.isOwnDevice) {
                        WifiAdbConnection.updateState(WifiAdbState.WirelessDebuggingOff(device.id))
                    } else {
                        WifiAdbConnection.updateState(
                            WifiAdbState.ConnectFailed(
                                "Discovery timeout",
                                device.id
                            )
                        )
                    }
                }
                listener?.onReconnectFailed(requiresPairing = false)
            }, 10, TimeUnit.SECONDS)

            activeDiscoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String) {
                    Log.d(TAG, "Reconnect mDNS discovery started: $serviceType")
                }

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.e(TAG, "Reconnect discovery start failed: $errorCode")
                    if (!connectionHandled && !isReconnectCancelled) {
                        connectionHandled = true
                        activeReconnectTimeout?.cancel(false)
                        activeReconnectTimeout = null
                        activeDiscoveryListener = null
                        activeNsdManager = null
                        listener?.onReconnectFailed(requiresPairing = false)
                    }
                }

                override fun onDiscoveryStopped(serviceType: String) {
                    Log.d(TAG, "Reconnect discovery stopped: $serviceType")
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.e(TAG, "Reconnect discovery stop failed: $errorCode")
                }

                override fun onServiceFound(info: android.net.nsd.NsdServiceInfo) {
                    if (connectionHandled || isReconnectCancelled) return
                    nsdManager.resolveService(
                        info,
                        object : NsdManager.ResolveListener {
                            override fun onResolveFailed(
                                serviceInfo: android.net.nsd.NsdServiceInfo,
                                errorCode: Int
                            ) {
                                Log.w(TAG, "Resolve failed: errorCode $errorCode")
                            }

                            override fun onServiceResolved(resolvedService: android.net.nsd.NsdServiceInfo) {
                                if (connectionHandled || isReconnectCancelled) return

                                val ip = resolvedService.host?.hostAddress ?: return
                                val port = resolvedService.port

                                Log.d(
                                    TAG,
                                    "Reconnect: Found service at $ip:$port (looking for $targetIp)"
                                )

                                // Match IP for own device (check against all network interfaces)
                                // or exact IP match for other devices
                                val isMatch = if (device.isOwnDevice) {
                                    isMatchingLocalNetwork(ip)
                                } else {
                                    ip == targetIp
                                }

                                if (!isMatch) {
                                    Log.d(TAG, "Reconnect: Ignoring service at $ip (not matching)")
                                    return
                                }

                                // Found matching service - connect!
                                connectionHandled = true
                                activeReconnectTimeout?.cancel(false)
                                activeReconnectTimeout = null

                                try {
                                    nsdManager.stopServiceDiscovery(activeDiscoveryListener)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error stopping discovery after finding service", e)
                                }
                                activeDiscoveryListener = null
                                activeNsdManager = null

                                // Final check for cancellation before connecting
                                if (isReconnectCancelled) {
                                    Log.d(TAG, "Reconnect cancelled just before connect")
                                    return
                                }

                                Log.d(TAG, "Reconnect: Connecting to $ip:$port")

                                val key = "$ip:$port"
                                mainScope.launch {
                                    WifiAdbConnection.updateState(
                                        WifiAdbState.ConnectStarted(
                                            key,
                                            device.id
                                        )
                                    )
                                }

                                connect(ip, port, object : ConnectionListener {
                                    override fun onConnectionSuccess() {
                                        // Clear reconnecting device ID on success
                                        currentReconnectingDeviceId = null
                                        // Update device with new IP/port if they changed
                                        currentDevice = device.copy(
                                            ip = ip,
                                            port = port,
                                            lastConnected = System.currentTimeMillis()
                                        )
                                        ioScope.launch { deviceDao.updateDevice(currentDevice!!.toEntity()) }
                                        WifiAdbConnection.setCurrentDevice(currentDevice)
                                        mainScope.launch {
                                            WifiAdbConnection.updateState(
                                                WifiAdbState.ConnectSuccess(
                                                    key,
                                                    device.id
                                                )
                                            )
                                        }
                                        listener?.onReconnectSuccess()
                                    }

                                    override fun onConnectionFailed() {
                                        // Always emit ConnectFailed for this device
                                        currentReconnectingDeviceId = null
                                        mainScope.launch {
                                            WifiAdbConnection.updateState(
                                                WifiAdbState.ConnectFailed(
                                                    key,
                                                    device.id
                                                )
                                            )
                                        }
                                        // Only call listener if still relevant
                                        if (reconnectDeviceId == device.id) {
                                            listener?.onReconnectFailed(requiresPairing = false)
                                        }
                                    }
                                })
                            }
                        })
                }

                override fun onServiceLost(info: android.net.nsd.NsdServiceInfo) {
                    Log.d(TAG, "Reconnect: Service lost - ${info.serviceName}")
                }
            }

            activeNsdManager = nsdManager
            nsdManager.discoverServices(
                "_adb-tls-connect._tcp",
                NsdManager.PROTOCOL_DNS_SD,
                activeDiscoveryListener
            )
            Log.d(TAG, "Started NsdManager discovery for reconnect to $targetIp")

        } catch (e: Exception) {
            Log.e(TAG, "Error discovering connect service for reconnect", e)
            currentReconnectingDeviceId = null
            mainScope.launch {
                WifiAdbConnection.updateState(
                    WifiAdbState.ConnectFailed(
                        e.message ?: "Discovery error", device.id
                    )
                )
            }
            listener?.onReconnectFailed(requiresPairing = false)
        }
    }

    /**
     * Check if an IP matches any of our local network interfaces.
     */
    private fun isMatchingLocalNetwork(ip: String): Boolean {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            for (networkInterface in java.util.Collections.list(interfaces)) {
                if (networkInterface.isUp) {
                    for (inetAddress in java.util.Collections.list(networkInterface.inetAddresses)) {
                        if (ip == inetAddress.hostAddress) {
                            Log.d(TAG, "IP $ip matches network interface ${networkInterface.name}")
                            return true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network interfaces", e)
        }
        return false
    }

    /**
     * Get current local IP address using NetworkInterface (more reliable).
     */
    @Suppress("DEPRECATION")
    @SuppressLint("DefaultLocale")
    private fun getLocalIpAddress(wifi: WifiManager): String? {
        // Method 1: Use NetworkInterface enumeration (most reliable)
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val name = networkInterface.name.lowercase()
                if (name.contains("wlan") || name.contains("wifi") || name.contains("eth")) {
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                            return address.hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP via NetworkInterface", e)
        }

        // Method 2: Fallback to WifiManager
        try {
            val wifiInfo = wifi.connectionInfo
            if (wifiInfo.ipAddress != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    wifiInfo.ipAddress and 0xff,
                    wifiInfo.ipAddress shr 8 and 0xff,
                    wifiInfo.ipAddress shr 16 and 0xff,
                    wifiInfo.ipAddress shr 24 and 0xff
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP via WifiManager", e)
        }

        return null
    }

    override fun disconnect() {
        executor.submit {
            try {
                val manager = AdbConnectionManager.getInstance(context)
                val disconnectedDevice = currentDevice
                manager.disconnect()
                abortShell()
                currentDevice = null
                WifiAdbConnection.setCurrentDevice(null)
                mainScope.launch {
                    val deviceId = disconnectedDevice?.id
                    WifiAdbConnection.updateState(WifiAdbState.Disconnected(deviceId))
                }
                Log.d(TAG, "Disconnected from ADB")
            } catch (e: Exception) {
                Log.e(TAG, "disconnect() failed", e)
            }
        }
    }

    override fun isConnected(): Boolean {
        return try {
            AdbConnectionManager.getInstance(context).isConnected
        } catch (e: Exception) {
            false
        }
    }

    override fun getCurrentDevice(): WifiAdbDevice? = currentDevice

    override fun forgetDevice(device: WifiAdbDevice) {
        ioScope.launch { deviceDao.deleteDevice(device.toEntity()) }
    }

    override suspend fun generatePairingQR(sessionId: String, pairingCode: Int, size: Int): Bitmap {
        val content = "WIFI:T:ADB;S:$sessionId;P:$pairingCode;;"

        val qr = QrCode.encodeText(content, Ecc.MEDIUM)

        val qrSize = qr.size
        val scale = max(1, size / qrSize)

        val bitmap = createBitmap(qrSize * scale, qrSize * scale)
        for (y in 0 until qrSize) {
            for (x in 0 until qrSize) {
                val color = if (qr.getModule(x, y)) Color.BLACK else Color.WHITE
                for (dy in 0 until scale) {
                    for (dx in 0 until scale) {
                        bitmap[x * scale + dx, y * scale + dy] = color
                    }
                }
            }
        }

        return bitmap
    }

    override fun startHeartbeat() {
        if (isHeartbeatRunning) {
            Log.d(TAG, "Heartbeat already running")
            return
        }

        isHeartbeatRunning = true
        Log.d(TAG, "Starting connection heartbeat")

        heartbeatJob = ioScope.launch {
            while (isHeartbeatRunning) {
                try {
                    delay(2500)

                    if (!isHeartbeatRunning) break

                    val device = currentDevice ?: run {
                        Log.d(TAG, "Heartbeat: No current device, stopping")
                        stopHeartbeat()
                        return@launch
                    }

                    val isStillConnected = try {
                        val manager = AdbConnectionManager.getInstance(context)
                        manager.isConnected
                    } catch (e: Exception) {
                        Log.d(TAG, "Heartbeat check failed: ${e.message}")
                        false
                    }

                    if (!isStillConnected && isHeartbeatRunning) {
                        Log.d(TAG, "Heartbeat detected disconnection for device: ${device.id}")
                        stopHeartbeat()

                        // Update state on main thread
                        mainScope.launch {
                            WifiAdbConnection.updateState(WifiAdbState.Disconnected(device.id))
                            WifiAdbConnection.setCurrentDevice(null)
                        }
                        currentDevice = null
                        return@launch
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Heartbeat cancelled")
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Heartbeat error", e)
                }
            }
        }
    }

    override fun stopHeartbeat() {
        if (!isHeartbeatRunning && heartbeatJob == null) return

        Log.d(TAG, "Stopping connection heartbeat")
        isHeartbeatRunning = false
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    // ========== INTERFACES ==========

    interface PairingListener {
        fun onPairingSuccess()
        fun onPairingFailed()
    }

    interface ConnectionListener {
        fun onConnectionSuccess()
        fun onConnectionFailed()
    }

    interface ReconnectListener {
        fun onReconnectSuccess()
        fun onReconnectFailed(requiresPairing: Boolean)
    }

    interface MdnsDiscoveryCallback {
        fun onServiceFound(name: String, ip: String, port: Int)
        fun onServiceLost(name: String)
        fun onPairingSuccess(ip: String, port: Int)
        fun onPairingFailed(ip: String, port: Int)
        fun onError(e: Throwable)
    }

    interface StateCallback {
        fun onStateChanged(state: WifiAdbState)
    }
}

