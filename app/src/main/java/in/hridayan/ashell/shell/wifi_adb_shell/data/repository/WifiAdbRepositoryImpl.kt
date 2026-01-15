package `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import `in`.hridayan.ashell.shell.common.domain.model.OutputLine
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.database.WifiAdbDeviceDao
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.mapper.toDomainList
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.mapper.toEntity
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.DiscoveredPairingService
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbConnection
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import `in`.hridayan.ashell.shell.wifi_adb_shell.service.AdbConnectionService
import io.github.muntashirakon.adb.AdbPairingRequiredException
import io.github.muntashirakon.adb.AdbStream
import io.github.muntashirakon.adb.android.AndroidUtils.getHostIpAddress
import io.nayuki.qrcodegen.QrCode
import io.nayuki.qrcodegen.QrCode.Ecc
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import kotlin.math.max

class WifiAdbRepositoryImpl(
    private val context: Context, private val deviceDao: WifiAdbDeviceDao
) : WifiAdbRepository {
    companion object {
        private const val TAG = "WifiAdbRepositoryImpl"
    }

    private val tlsConnect = "_adb-tls-connect._tcp"
    private val tlsPairing = "_adb-tls-pairing._tcp"
    private var adbShellStream: AdbStream? = null
    private val executor = Executors.newScheduledThreadPool(1)
    private var jmDns: JmDNS? = null
    private val pairingInProgress = mutableSetOf<String>()
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    @Volatile
    private var isReconnectCancelled = false

    @Volatile
    private var currentReconnectingDeviceId: String? = null
    private var activeNsdManager: NsdManager? = null
    private var activeDiscoveryListener: NsdManager.DiscoveryListener? = null
    private var activeReconnectTimeout: ScheduledFuture<*>? = null

    private var heartbeatJob: Job? = null

    @Volatile
    private var isHeartbeatRunning = false

    private val cachedConnectPorts = mutableMapOf<String, Int>()
    private var pairingNsdManager: NsdManager? = null
    private var pairingNsdDiscoveryListener: NsdManager.DiscoveryListener? = null

    // region QR/Code Pairing Flow

    override fun pairingWithQr(
        pairingCode: String, autoPair: Boolean, callback: MdnsDiscoveryCallback?
    ) {
        executor.submit {
            try {
                jmDns?.close()

                val wifi =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val lock = wifi.createMulticastLock("adb_mdns_lock")
                lock.setReferenceCounted(true)
                lock.acquire()

                val addr = getHostIpAddress(context)
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

                            disconnect()

                            pair(ip, port, pairingCode, object : PairingListener {
                                override fun onPairingSuccess() {
                                    pairingInProgress.remove(key)
                                    Log.d(TAG, "Pairing succeeded for $key!")
                                    callback?.onPairingSuccess(ip, port)

                                    // Check if we have a cached connect port from parallel discovery
                                    val cachedPort =
                                        synchronized(cachedConnectPorts) { cachedConnectPorts[ip] }

                                    if (cachedPort != null) {
                                        // Use cached port for immediate connection
                                        Log.d(
                                            TAG, "Using cached connect port for $ip -> $cachedPort"
                                        )
                                        stopParallelConnectDiscovery()

                                        mainScope.launch {
                                            WifiAdbConnection.updateState(
                                                WifiAdbState.ConnectStarted(
                                                    "$ip:$cachedPort"
                                                )
                                            )
                                        }

                                        connect(ip, cachedPort, object : ConnectionListener {
                                            override fun onConnectionSuccess() {
                                                val serial = getDeviceSerialNumber()
                                                val deviceName = getDeviceName()
                                                Log.d(
                                                    TAG,
                                                    "Device info - Serial: $serial, Name: $deviceName"
                                                )

                                                val connectedDevice = WifiAdbDevice(
                                                    ip = ip,
                                                    port = cachedPort,
                                                    deviceName = deviceName,
                                                    isPaired = true,
                                                    lastConnected = System.currentTimeMillis(),
                                                    serialNumber = serial
                                                )
                                                ioScope.launch {
                                                    deviceDao.insertDevice(
                                                        connectedDevice.toEntity()
                                                    )
                                                }
                                                currentDevice = connectedDevice
                                                WifiAdbConnection.setCurrentDevice(connectedDevice)

                                                mainScope.launch {
                                                    WifiAdbConnection.setDeviceConnected(
                                                        connectedDevice.id, "$ip:$cachedPort"
                                                    )
                                                }
                                                Log.d(
                                                    TAG,
                                                    "Connected successfully via cached port to $ip:$cachedPort"
                                                )
                                                clearCachedConnectPorts()
                                                callback?.onPairingSuccess(ip, cachedPort)
                                            }

                                            override fun onConnectionFailed() {
                                                Log.e(
                                                    TAG,
                                                    "Cached port connection failed for $ip:$cachedPort, trying discovery..."
                                                )
                                                clearCachedConnectPorts()
                                                // Fallback to NSD discovery
                                                discoverConnectService(callback, ip)
                                            }
                                        })
                                    } else {
                                        // No cached port, wait a moment then try discovery
                                        Log.d(
                                            TAG,
                                            "No cached connect port for $ip, falling back to discovery..."
                                        )
                                        stopParallelConnectDiscovery()
                                        executor.schedule({
                                            mainScope.launch {
                                                WifiAdbConnection.updateState(
                                                    WifiAdbState.DiscoveryStarted("connect service discovery started")
                                                )
                                            }
                                            discoverConnectService(callback, ip)
                                        }, 2, TimeUnit.SECONDS)
                                    }
                                }


                                override fun onPairingFailed() {
                                    pairingInProgress.remove(key)
                                    stopParallelConnectDiscovery()
                                    clearCachedConnectPorts()
                                    Log.d(TAG, "Pairing failed for $key!")
                                    callback?.onPairingFailed(ip, port)
                                }
                            })
                        }
                    }
                }

                jmDns?.addServiceListener("_adb-tls-pairing._tcp.local.", listener)
                Log.d(TAG, "Started mDNS discovery for ADB pairing...")

                // Start parallel NSD discovery for connect services
                // This runs alongside pairing to cache connect ports by IP
                startParallelConnectDiscovery()

            } catch (e: Throwable) {
                Log.e(TAG, "mDNS discovery failed", e)
                callback?.onError(e)
            }
        }
    }

    /**
     * Start NSD discovery for connect services in parallel with pairing.
     * Caches discovered connect ports by IP so they can be used immediately when pairing succeeds.
     */
    private fun startParallelConnectDiscovery() {
        try {
            // Stop any existing parallel discovery
            stopParallelConnectDiscovery()

            pairingNsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

            pairingNsdDiscoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String) {
                    Log.d(TAG, "Parallel connect discovery started: $serviceType")
                }

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.e(TAG, "Parallel connect discovery start failed: errorCode=$errorCode")
                }

                override fun onDiscoveryStopped(serviceType: String) {
                    Log.d(TAG, "Parallel connect discovery stopped: $serviceType")
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.e(TAG, "Parallel connect discovery stop failed: $errorCode")
                }

                override fun onServiceFound(info: NsdServiceInfo) {
                    Log.d(TAG, "Parallel: Found connect service: ${info.serviceName}")
                    @Suppress("DEPRECATION")
                    pairingNsdManager?.resolveService(info, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(
                            serviceInfo: NsdServiceInfo, errorCode: Int
                        ) {
                            Log.w(TAG, "Parallel: Resolve failed: errorCode=$errorCode")
                        }

                        override fun onServiceResolved(resolvedService: NsdServiceInfo) {
                            val ip = resolvedService.host?.hostAddress ?: return
                            val port = resolvedService.port

                            Log.d(TAG, "Parallel: Cached connect port for $ip -> $port")
                            synchronized(cachedConnectPorts) {
                                cachedConnectPorts[ip] = port
                            }
                        }
                    })
                }

                override fun onServiceLost(info: NsdServiceInfo) {
                    Log.d(TAG, "Parallel: Connect service lost: ${info.serviceName}")
                }
            }

            pairingNsdManager?.discoverServices(
                tlsConnect, NsdManager.PROTOCOL_DNS_SD, pairingNsdDiscoveryListener
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error starting parallel connect discovery", e)
        }
    }

    /**
     * Stop parallel connect discovery. Does NOT clear cached ports.
     */
    private fun stopParallelConnectDiscovery() {
        try {
            pairingNsdDiscoveryListener?.let { listener ->
                pairingNsdManager?.stopServiceDiscovery(listener)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping parallel connect discovery", e)
        }
        pairingNsdDiscoveryListener = null
        pairingNsdManager = null
    }

    /**
     * Clear cached connect ports.
     */
    private fun clearCachedConnectPorts() {
        synchronized(cachedConnectPorts) {
            cachedConnectPorts.clear()
        }
    }

    /**
     * After pairing, discover the normal adb-tls-connect service using NSD.
     * @param targetIp IP of device we just paired with - to match the correct service
     */
    @SuppressLint("DefaultLocale")
    private fun discoverConnectService(callback: MdnsDiscoveryCallback?, targetIp: String? = null) {
        executor.submit {
            try {
                val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
                var connectionHandled = false
                var pairConnectDiscoveryListener: NsdManager.DiscoveryListener? = null

                Log.d(TAG, "Starting NSD discovery for ADB connect service (target: $targetIp)")

                // Set timeout for discovery
                val discoveryTimeout = executor.schedule({
                    if (connectionHandled) return@schedule
                    connectionHandled = true

                    Log.d(
                        TAG, "NSD connect discovery timeout, trying direct connection fallback..."
                    )

                    try {
                        pairConnectDiscoveryListener?.let { nsdManager.stopServiceDiscovery(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error stopping NSD discovery on timeout", e)
                    }

                    // Fallback to direct connection on common ports
                    if (targetIp != null) {
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
                                        WifiAdbConnection.setDeviceConnected(
                                            connectedDevice.id, "$targetIp:$port"
                                        )
                                    }
                                    Log.d(TAG, "Direct connection to $targetIp:$port succeeded!")
                                    callback?.onPairingSuccess(targetIp, port)
                                    break
                                }
                            } catch (e: Exception) {
                                Log.d(TAG, "Port $port failed: ${e.message}")
                            }
                        }

                        if (!connected) {
                            Log.e(TAG, "All direct connection attempts failed for $targetIp")
                            mainScope.launch {
                                WifiAdbConnection.updateState(
                                    WifiAdbState.PairConnectFailed("Paired but connect failed - try Manual Pair with correct port")
                                )
                            }
                            callback?.onPairingFailed(targetIp, 0)
                        }
                    } else {
                        mainScope.launch {
                            WifiAdbConnection.updateState(
                                WifiAdbState.PairConnectFailed("No target IP for connect discovery")
                            )
                        }
                    }
                }, 10, TimeUnit.SECONDS)

                pairConnectDiscoveryListener = object : NsdManager.DiscoveryListener {
                    override fun onDiscoveryStarted(serviceType: String) {
                        Log.d(TAG, "NSD connect discovery started: $serviceType")
                    }

                    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                        Log.e(TAG, "NSD connect discovery start failed: errorCode=$errorCode")
                        if (!connectionHandled) {
                            connectionHandled = true
                            discoveryTimeout.cancel(false)
                            mainScope.launch {
                                WifiAdbConnection.updateState(
                                    WifiAdbState.PairConnectFailed("Discovery failed (error $errorCode)")
                                )
                            }
                        }
                    }

                    override fun onDiscoveryStopped(serviceType: String) {
                        Log.d(TAG, "NSD connect discovery stopped: $serviceType")
                    }

                    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                        Log.e(TAG, "NSD connect discovery stop failed: $errorCode")
                    }

                    override fun onServiceFound(info: NsdServiceInfo) {
                        if (connectionHandled) return
                        Log.d(TAG, "NSD found service: ${info.serviceName}")
                        @Suppress("DEPRECATION")
                        nsdManager.resolveService(info, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(
                                serviceInfo: NsdServiceInfo, errorCode: Int
                            ) {
                                Log.w(TAG, "NSD resolve failed: errorCode=$errorCode")
                            }

                            override fun onServiceResolved(resolvedService: NsdServiceInfo) {
                                if (connectionHandled) return

                                val ip = resolvedService.host?.hostAddress ?: return
                                val port = resolvedService.port
                                val key = "$ip:$port"

                                Log.d(TAG, "NSD resolved service at $key (looking for: $targetIp)")

                                // If we have a target IP, only connect to that device
                                if (targetIp != null && ip != targetIp) {
                                    Log.d(TAG, "Ignoring $key, not matching target $targetIp")
                                    return
                                }

                                // Found matching service - connect
                                connectionHandled = true
                                discoveryTimeout.cancel(false)

                                try {
                                    nsdManager.stopServiceDiscovery(pairConnectDiscoveryListener)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error stopping NSD discovery", e)
                                }

                                mainScope.launch {
                                    WifiAdbConnection.updateState(WifiAdbState.ConnectStarted(key))
                                }

                                connect(ip, port, object : ConnectionListener {
                                    override fun onConnectionSuccess() {
                                        val serial = getDeviceSerialNumber()
                                        val deviceName = getDeviceName()
                                        Log.d(
                                            TAG, "Device info - Serial: $serial, Name: $deviceName"
                                        )

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

                                        mainScope.launch {
                                            WifiAdbConnection.setDeviceConnected(
                                                connectedDevice.id, key
                                            )
                                        }
                                        Log.d(TAG, "Connected successfully to $ip:$port")
                                        callback?.onPairingSuccess(ip, port)
                                    }

                                    override fun onConnectionFailed() {
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
                        })
                    }

                    override fun onServiceLost(info: NsdServiceInfo) {
                        Log.d(TAG, "NSD service lost: ${info.serviceName}")
                    }
                }

                nsdManager.discoverServices(
                    tlsConnect,
                    NsdManager.PROTOCOL_DNS_SD,
                    pairConnectDiscoveryListener
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error in NSD connect discovery", e)
                mainScope.launch {
                    WifiAdbConnection.updateState(
                        WifiAdbState.PairConnectFailed("Discovery error: ${e.message}")
                    )
                }
            }
        }
    }


    // region Low-Level Connection

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
                val actualIp = ip ?: getHostIpAddress(context)
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

    // region Device Info Utilities

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


    // region Shell Execution

    @Volatile
    private var isAborted = false

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
                cdPart = trimmedCommand.take(separatorIndex).trim()
                remainingCommand = trimmedCommand.substring(
                    separatorIndex + if (trimmedCommand.substring(separatorIndex)
                            .startsWith(" && ")
                    ) 4 else 2
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
    private fun buildWifiAdbCommand(commandText: String): String {
        return if (currentDir != "/") {
            "cd '$currentDir' && $commandText"
        } else {
            commandText
        }
    }

    override fun execute(commandText: String): Flow<OutputLine> = flow {
        val actualCommand = handleCdCommand(commandText)

        if (actualCommand == null) {
            emit(OutputLine("Changed directory to: $currentDir", isError = false))
            return@flow
        }

        val fullCommand = buildWifiAdbCommand(actualCommand)
        val manager = AdbConnectionManager.getInstance(context)

        if (!manager.isConnected) {
            emit(OutputLine("ADB not connected", isError = true))
            return@flow
        }

        isAborted = false

        try {
            try {
                adbShellStream?.close()
            } catch (e: Exception) {
                // Ignore
            }

            adbShellStream = manager.openStream("shell:$fullCommand")

            val input = adbShellStream!!.openInputStream()
            val reader = BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8))

            var line: String?
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
            // Also stop parallel connect discovery and clear cache
            stopParallelConnectDiscovery()
            clearCachedConnectPorts()
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


    // region Device CRUD

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


    // region Reconnection Flow

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

        // Cancel serial match job
        serialMatchJob?.cancel()
        serialMatchJob = null

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
                            ip = targetIp, lastConnected = System.currentTimeMillis()
                        )
                        ioScope.launch { deviceDao.updateDevice(currentDevice!!.toEntity()) }
                        WifiAdbConnection.setCurrentDevice(currentDevice)
                        mainScope.launch {
                            WifiAdbConnection.updateState(
                                WifiAdbState.ConnectSuccess(
                                    device.id, device.id
                                )
                            )
                        }
                        listener?.onReconnectSuccess()
                        return@submit
                    } else {
                        Log.d(TAG, "Direct connect returned false for ${device.id}")
                    }
                } catch (e: AdbPairingRequiredException) {
                    // Device needs re-pairing - public key not saved on target
                    Log.d(TAG, "Device requires re-pairing: ${device.id}")
                    mainScope.launch {
                        WifiAdbConnection.updateState(
                            WifiAdbState.ConnectFailed(
                                "Requires re-pairing", device.id
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
        device: WifiAdbDevice, listener: ReconnectListener?
    ) {
        try {
            val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

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

                Log.d(TAG, "NsdManager discovery timeout for reconnect")

                try {
                    activeDiscoveryListener?.let { nsdManager.stopServiceDiscovery(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping discovery on timeout", e)
                }
                activeDiscoveryListener = null
                activeNsdManager = null
                activeReconnectTimeout = null

                // For own device, timeout likely means wireless debugging is off
                if (device.isOwnDevice) {
                    currentReconnectingDeviceId = null
                    mainScope.launch {
                        WifiAdbConnection.updateState(WifiAdbState.WirelessDebuggingOff(device.id))
                    }
                    listener?.onReconnectFailed(requiresPairing = false)
                } else if (!device.serialNumber.isNullOrBlank()) {
                    // Try serial matching as fallback for devices with saved serial
                    // Keep currentReconnectingDeviceId set - serial matching needs it!
                    Log.d(TAG, "Port discovery timeout, trying serial matching for ${device.id}")
                    discoverAndMatchBySerial(device, listener)
                } else {
                    currentReconnectingDeviceId = null
                    mainScope.launch {
                        WifiAdbConnection.updateState(
                            WifiAdbState.ConnectFailed(
                                "Discovery timeout", device.id
                            )
                        )
                    }
                    listener?.onReconnectFailed(requiresPairing = false)
                }
            }, 6, TimeUnit.SECONDS)

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

                override fun onServiceFound(info: NsdServiceInfo) {
                    if (connectionHandled || isReconnectCancelled) return
                    nsdManager.resolveService(
                        info, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(
                                serviceInfo: NsdServiceInfo, errorCode: Int
                            ) {
                                Log.w(TAG, "Resolve failed: errorCode $errorCode")
                            }

                            override fun onServiceResolved(resolvedService: NsdServiceInfo) {
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
                                            key, device.id
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
                                                    key, device.id
                                                )
                                            )
                                        }
                                        listener?.onReconnectSuccess()
                                    }

                                    override fun onConnectionFailed() {
                                        // Try serial matching for devices with saved serial
                                        if (!device.serialNumber.isNullOrBlank()) {
                                            Log.d(
                                                TAG,
                                                "Connection failed, trying serial matching for ${device.id}"
                                            )
                                            discoverAndMatchBySerial(device, listener)
                                        } else {
                                            // No serial available, emit failure
                                            currentReconnectingDeviceId = null
                                            mainScope.launch {
                                                WifiAdbConnection.updateState(
                                                    WifiAdbState.ConnectFailed(
                                                        key, device.id
                                                    )
                                                )
                                            }
                                            // Only call listener if still relevant
                                            if (reconnectDeviceId == device.id) {
                                                listener?.onReconnectFailed(requiresPairing = false)
                                            }
                                        }
                                    }
                                })
                            }
                        })
                }

                override fun onServiceLost(info: NsdServiceInfo) {
                    Log.d(TAG, "Reconnect: Service lost - ${info.serviceName}")
                }
            }

            activeNsdManager = nsdManager
            nsdManager.discoverServices(
                tlsConnect, NsdManager.PROTOCOL_DNS_SD, activeDiscoveryListener
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

    // Job for serial matching to enable cancellation
    private var serialMatchJob: Job? = null

    /**
     * Step 3 of reconnect: Discover ALL connect services on the network,
     * match by service name (which contains serial), and connect.
     * Service name format: adb-{serial}-{random}
     */
    @Suppress("DEPRECATION")
    @SuppressLint("DefaultLocale")
    private fun discoverAndMatchBySerial(
        device: WifiAdbDevice,
        listener: ReconnectListener?
    ) {
        // Only proceed if device has a saved serial number
        if (device.serialNumber.isNullOrBlank()) {
            Log.d(TAG, "Serial matching skipped - no saved serial for ${device.id}")
            currentReconnectingDeviceId = null
            mainScope.launch {
                WifiAdbConnection.updateState(
                    WifiAdbState.ConnectFailed("No serial for matching", device.id)
                )
            }
            listener?.onReconnectFailed(requiresPairing = false)
            return
        }

        Log.d(
            TAG,
            "Starting serial matching discovery for ${device.id} (serial: ${device.serialNumber})"
        )

        // Cancel any previous serial match job
        serialMatchJob?.cancel()

        val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        var matchFound = false
        val reconnectDeviceId = device.id
        val targetSerial = device.serialNumber

        // Set a timeout for discovery
        val discoveryTimeout = executor.schedule({
            if (!matchFound && !isReconnectCancelled && currentReconnectingDeviceId == reconnectDeviceId) {
                Log.d(TAG, "Serial matching discovery timeout - device not found")

                try {
                    activeDiscoveryListener?.let { nsdManager.stopServiceDiscovery(it) }
                } catch (e: Exception) {
                    // Ignore
                }
                activeDiscoveryListener = null
                activeNsdManager = null

                currentReconnectingDeviceId = null
                mainScope.launch {
                    WifiAdbConnection.updateState(
                        WifiAdbState.ConnectFailed("Device not broadcasting", device.id)
                    )
                }
                listener?.onReconnectFailed(requiresPairing = false)
            }
        }, 10, TimeUnit.SECONDS)

        activeDiscoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                Log.d(TAG, "Serial matching discovery started")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Serial matching discovery start failed: $errorCode")
                discoveryTimeout.cancel(false)
                currentReconnectingDeviceId = null
                mainScope.launch {
                    WifiAdbConnection.updateState(
                        WifiAdbState.ConnectFailed("Discovery failed", device.id)
                    )
                }
                listener?.onReconnectFailed(requiresPairing = false)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "Serial matching discovery stopped")
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Serial matching discovery stop failed: $errorCode")
            }

            override fun onServiceFound(info: NsdServiceInfo) {
                if (matchFound || isReconnectCancelled) return

                val serviceName = info.serviceName
                Log.d(TAG, "Serial matching: Found service $serviceName")

                // Check if service name contains our serial
                // Service name format: adb-{serial}-{random}
                if (serviceName.contains(targetSerial, ignoreCase = true)) {
                    Log.d(TAG, "Serial matching: Service name matches! Resolving $serviceName")

                    nsdManager.resolveService(info, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(
                            serviceInfo: NsdServiceInfo,
                            errorCode: Int
                        ) {
                            Log.e(
                                TAG,
                                "Serial matching: Resolve failed for matching service: $errorCode"
                            )
                        }

                        override fun onServiceResolved(resolvedService: NsdServiceInfo) {
                            if (matchFound || isReconnectCancelled) return

                            val ip = resolvedService.host?.hostAddress ?: return
                            val port = resolvedService.port

                            Log.d(TAG, "Serial matching: Matched device at $ip:$port")
                            matchFound = true
                            discoveryTimeout.cancel(false)

                            // Stop discovery
                            try {
                                nsdManager.stopServiceDiscovery(activeDiscoveryListener)
                            } catch (e: Exception) {
                                // Ignore
                            }
                            activeDiscoveryListener = null
                            activeNsdManager = null

                            // Try to connect to the matched device
                            ioScope.launch {
                                try {
                                    val manager = AdbConnectionManager.getInstance(context)

                                    // Disconnect any existing connection
                                    if (manager.isConnected) {
                                        try {
                                            manager.disconnect()
                                        } catch (e: Exception) {
                                        }
                                    }

                                    Log.d(
                                        TAG,
                                        "Serial matching: Connecting to matched device at $ip:$port"
                                    )
                                    val connected = manager.connect(ip, port)

                                    if (connected) {
                                        Log.d(TAG, "Serial matching: Connection successful!")

                                        // Update device with new IP and port
                                        currentDevice = device.copy(
                                            ip = ip,
                                            port = port,
                                            lastConnected = System.currentTimeMillis()
                                        )
                                        deviceDao.updateDevice(currentDevice!!.toEntity())
                                        WifiAdbConnection.setCurrentDevice(currentDevice)

                                        currentReconnectingDeviceId = null
                                        mainScope.launch {
                                            WifiAdbConnection.updateState(
                                                WifiAdbState.ConnectSuccess("$ip:$port", device.id)
                                            )
                                        }
                                        listener?.onReconnectSuccess()
                                    } else {
                                        Log.e(
                                            TAG,
                                            "Serial matching: Connection to matched device failed"
                                        )
                                        currentReconnectingDeviceId = null
                                        mainScope.launch {
                                            WifiAdbConnection.updateState(
                                                WifiAdbState.ConnectFailed(
                                                    "Connection failed",
                                                    device.id
                                                )
                                            )
                                        }
                                        listener?.onReconnectFailed(requiresPairing = false)
                                    }
                                } catch (e: AdbPairingRequiredException) {
                                    Log.e(TAG, "Serial matching: Device requires re-pairing")
                                    currentReconnectingDeviceId = null
                                    mainScope.launch {
                                        WifiAdbConnection.updateState(
                                            WifiAdbState.ConnectFailed(
                                                "Requires re-pairing",
                                                device.id
                                            )
                                        )
                                    }
                                    listener?.onReconnectFailed(requiresPairing = true)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Serial matching: Connection error", e)
                                    currentReconnectingDeviceId = null
                                    mainScope.launch {
                                        WifiAdbConnection.updateState(
                                            WifiAdbState.ConnectFailed(
                                                "Error: ${e.message}",
                                                device.id
                                            )
                                        )
                                    }
                                    listener?.onReconnectFailed(requiresPairing = false)
                                }
                            }
                        }
                    })
                }
            }

            override fun onServiceLost(info: NsdServiceInfo) {
                Log.d(TAG, "Serial matching: Service lost ${info.serviceName}")
            }
        }

        activeNsdManager = nsdManager
        nsdManager.discoverServices(
            tlsConnect, NsdManager.PROTOCOL_DNS_SD, activeDiscoveryListener
        )
        Log.d(TAG, "Started serial matching discovery for ${device.id}")
    }

    /**
     * Check if an IP matches any of our local network interfaces.
     */
    private fun isMatchingLocalNetwork(ip: String): Boolean {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in Collections.list(interfaces)) {
                if (networkInterface.isUp) {
                    for (inetAddress in Collections.list(networkInterface.inetAddresses)) {
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
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val name = networkInterface.name.lowercase()
                if (name.contains("wlan") || name.contains("wifi") || name.contains("eth")) {
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address is Inet4Address) {
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
        // If currently connected to this device, disconnect first
        val currentDev = currentDevice
        if (currentDev != null && currentDev.id == device.id && isConnected()) {
            Log.d(TAG, "Forget device: Disconnecting from ${device.deviceName} first")
            disconnect()
        }
        ioScope.launch { deviceDao.deleteDevice(device.toEntity()) }
    }

    override suspend fun generatePairingQR(
        sessionId: String, pairingCode: String, size: Int
    ): Bitmap {
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

        // Start foreground service to keep connection alive in background
        AdbConnectionService.start(context)

        heartbeatJob = ioScope.launch {
            while (isHeartbeatRunning) {
                try {
                    delay(5000) // Check every 5 seconds

                    if (!isHeartbeatRunning) break

                    val device = currentDevice ?: run {
                        Log.d(TAG, "Heartbeat: No current device, stopping")
                        stopHeartbeat()
                        return@launch
                    }

                    // Simple isConnected check (foreground service keeps process alive)
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

                        // Update per-device state on main thread
                        mainScope.launch {
                            WifiAdbConnection.setDeviceDisconnected(device.id)
                            WifiAdbConnection.setCurrentDevice(null)
                        }
                        currentDevice = null
                        return@launch
                    }
                } catch (e: CancellationException) {
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

        // Stop foreground service
        AdbConnectionService.stop(context)
    }


    // region Listener Interfaces

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


    // region Code Pairing Discovery

    private var codePairingNsdManager: NsdManager? = null
    private var codePairingDiscoveryListener: NsdManager.DiscoveryListener? = null
    private var onPairingServiceFoundCallback: ((DiscoveredPairingService) -> Unit)? = null
    private var onPairingServiceLostCallback: ((String) -> Unit)? = null

    /**
     * This function is only to discover the ip and port for Pairing with pairing code
     */
    override fun startCodePairingDiscovery(
        onPairingServiceFound: (DiscoveredPairingService) -> Unit,
        onPairingServiceLost: (serviceName: String) -> Unit
    ) {
        stopCodePairingDiscovery()
        Log.d(TAG, "Starting Code Pairing discovery (dual: pairing + connect)")

        onPairingServiceFoundCallback = onPairingServiceFound
        onPairingServiceLostCallback = onPairingServiceLost

        codePairingNsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

        // Start connect service discovery first to cache ports
        startParallelConnectDiscovery()

        // Now start pairing service discovery
        codePairingDiscoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                Log.d(TAG, "Code pairing discovery started: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Code pairing discovery start failed: $errorCode")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "Code pairing discovery stopped: $serviceType")
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Code pairing discovery stop failed: $errorCode")
            }

            override fun onServiceFound(info: NsdServiceInfo) {
                Log.d(TAG, "Code pairing: Found pairing service: ${info.serviceName}")
                @Suppress("DEPRECATION")
                codePairingNsdManager?.resolveService(info, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(
                        serviceInfo: NsdServiceInfo, errorCode: Int
                    ) {
                        Log.w(TAG, "Code pairing: Resolve failed: $errorCode")
                    }

                    override fun onServiceResolved(resolvedService: NsdServiceInfo) {
                        val ip = resolvedService.host?.hostAddress ?: return
                        val port = resolvedService.port
                        val name = resolvedService.serviceName

                        Log.d(TAG, "Code pairing: Resolved service $name at $ip:$port")

                        val service = DiscoveredPairingService(
                            serviceName = name, ip = ip, port = port, deviceName = name
                        )

                        mainScope.launch {
                            onPairingServiceFoundCallback?.invoke(service)
                        }
                    }
                })
            }

            override fun onServiceLost(info: NsdServiceInfo) {
                Log.d(TAG, "Code pairing: Pairing service lost: ${info.serviceName}")
                mainScope.launch {
                    onPairingServiceLostCallback?.invoke(info.serviceName)
                }
            }
        }

        try {
            codePairingNsdManager?.discoverServices(
                tlsPairing, NsdManager.PROTOCOL_DNS_SD, codePairingDiscoveryListener
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error starting code pairing discovery", e)
        }
    }

    override fun stopCodePairingDiscovery() {
        try {
            codePairingDiscoveryListener?.let { listener ->
                codePairingNsdManager?.stopServiceDiscovery(listener)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping code pairing discovery", e)
        }
        codePairingDiscoveryListener = null
        stopParallelConnectDiscovery()
        clearCachedConnectPorts()
        codePairingNsdManager = null
        onPairingServiceFoundCallback = null
        onPairingServiceLostCallback = null
    }

    override fun pairAndConnect(
        ip: String, pairingPort: Int, pairingCode: String, callback: MdnsDiscoveryCallback?
    ) {
        executor.submit {
            Log.d(TAG, "Starting pair and connect for $ip:$pairingPort")

            // Check if already connected to this device
            val currentDev = currentDevice
            if (currentDev != null && currentDev.ip == ip && isConnected()) {
                Log.d(TAG, "Device $ip is already connected")
                mainScope.launch {
                    WifiAdbConnection.updateState(WifiAdbState.AlreadyConnected(ip))
                }
                return@submit
            }

            pair(ip, pairingPort, pairingCode, object : PairingListener {
                override fun onPairingSuccess() {
                    Log.d(TAG, "Pairing succeeded for $ip:$pairingPort, looking up connect port...")

                    // Look up cached connect port for this IP
                    val connectPort = synchronized(cachedConnectPorts) { cachedConnectPorts[ip] }

                    if (connectPort != null) {
                        Log.d(TAG, "Using cached connect port $connectPort for $ip")

                        mainScope.launch {
                            WifiAdbConnection.updateState(WifiAdbState.ConnectStarted("$ip:$connectPort"))
                        }

                        connect(ip, connectPort, object : ConnectionListener {
                            override fun onConnectionSuccess() {
                                val serial = getDeviceSerialNumber()
                                val deviceName = getDeviceName()
                                Log.d(TAG, "Device info - Serial: $serial, Name: $deviceName")

                                val connectedDevice = WifiAdbDevice(
                                    ip = ip,
                                    port = connectPort,
                                    deviceName = deviceName,
                                    isPaired = true,
                                    lastConnected = System.currentTimeMillis(),
                                    serialNumber = serial
                                )
                                ioScope.launch { deviceDao.insertDevice(connectedDevice.toEntity()) }
                                currentDevice = connectedDevice
                                WifiAdbConnection.setCurrentDevice(connectedDevice)

                                mainScope.launch {
                                    WifiAdbConnection.setDeviceConnected(
                                        connectedDevice.id, "$ip:$connectPort"
                                    )
                                }
                                Log.d(TAG, "Connection successful to $ip:$connectPort")
                                clearCachedConnectPorts()
                                callback?.onPairingSuccess(ip, connectPort)
                            }

                            override fun onConnectionFailed() {
                                Log.e(TAG, "Connection failed to $ip:$connectPort")
                                clearCachedConnectPorts()
                                mainScope.launch {
                                    WifiAdbConnection.updateState(WifiAdbState.PairConnectFailed("Connection failed"))
                                }
                                callback?.onPairingFailed(ip, connectPort)
                            }
                        })
                    } else {
                        // No cached port -  fallback to discovery
                        Log.w(TAG, "No cached connect port for $ip, using discovery fallback...")
                        discoverConnectService(callback, ip)
                    }
                }

                override fun onPairingFailed() {
                    Log.e(TAG, "Pairing failed for $ip:$pairingPort")
                    clearCachedConnectPorts()
                    mainScope.launch {
                        WifiAdbConnection.updateState(WifiAdbState.PairingFailed("Pairing failed"))
                    }
                    callback?.onPairingFailed(ip, pairingPort)
                }
            })
        }
    }
}
