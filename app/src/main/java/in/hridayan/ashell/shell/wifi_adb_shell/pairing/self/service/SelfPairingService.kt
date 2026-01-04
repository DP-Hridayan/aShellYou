package `in`.hridayan.ashell.shell.wifi_adb_shell.pairing.self.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.RemoteInput
import `in`.hridayan.ashell.shell.domain.usecase.AdbConnectionManager
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.WifiAdbStorage
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbConnection
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.pairing.self.notification.SelfPairingNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

/**
 * Foreground service for pairing with the device the app is running on (self/own device).
 *
 * Flow:
 * 1. User clicks Developer Options → Service starts and shows "Searching..." notification
 * 2. mDNS discovers pairing service (filtered by local IP)
 * 3. Shows "Enter code" notification with input field
 * 4. User enters pairing code
 * 5. Pairs using existing WifiAdbRepository logic
 * 6. Discovers connect service and connects
 * 7. Saves device with isOwnDevice=true
 */
class SelfPairingService : Service() {

    companion object {
        private const val TAG = "SelfPairingService"

        fun start(context: Context) {
            val intent = Intent(context, SelfPairingService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SelfPairingService::class.java))
        }
    }

    // Dependencies
    private lateinit var notificationHelper: SelfPairingNotificationHelper
    private lateinit var storage: WifiAdbStorage
    private lateinit var repository: WifiAdbRepositoryImpl
    private val mainScope = CoroutineScope(Dispatchers.Main)

    // mDNS and threading
    private var jmDns: JmDNS? = null
    private var executor: ScheduledExecutorService? = null
    private var discoveryTimeout: ScheduledFuture<*>? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    // State
    private var localIpAddress: String? = null
    private var discoveredPairingPort: Int? = null
    private var isProcessing = false

    override fun onCreate() {
        super.onCreate()
        notificationHelper = SelfPairingNotificationHelper(this)
        storage = WifiAdbStorage(this)
        repository = WifiAdbRepositoryImpl(this)
        executor = Executors.newScheduledThreadPool(2)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            SelfPairingNotificationHelper.ACTION_SUBMIT_PAIRING_CODE -> {
                handlePairingCodeSubmission(intent)
            }

            SelfPairingNotificationHelper.ACTION_CANCEL -> {
                cleanup()
                stopSelf()
            }

            else -> {
                // Fresh start - reset state and begin discovery
                resetState()
                val notification = notificationHelper.showSearchingNotification(
                    SelfPairingService::class.java
                )
                startForeground(SelfPairingNotificationHelper.NOTIFICATION_ID, notification)
                startPairingServiceDiscovery()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }

    /**
     * Reset all state for a fresh pairing attempt.
     */
    private fun resetState() {
        Log.d(TAG, "Resetting state for fresh pairing attempt")

        // Cancel any pending operations
        discoveryTimeout?.cancel(true)
        discoveryTimeout = null

        // Close existing mDNS
        try {
            jmDns?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing JmDNS", e)
        }
        jmDns = null

        // Release multicast lock
        try {
            multicastLock?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing multicast lock", e)
        }
        multicastLock = null

        // Disconnect any existing connection to allow fresh pairing
        try {
            val manager = AdbConnectionManager.getInstance(this)
            if (manager.isConnected) {
                Log.d(TAG, "Disconnecting existing connection before pairing")
                manager.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }

        // Reset state variables
        localIpAddress = null
        discoveredPairingPort = null
        isProcessing = false
    }

    /**
     * Full cleanup when service is stopping.
     */
    private fun cleanup() {
        Log.d(TAG, "Cleaning up service")
        discoveryTimeout?.cancel(true)

        try {
            jmDns?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing JmDNS", e)
        }
        jmDns = null

        try {
            multicastLock?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing multicast lock", e)
        }
        multicastLock = null

        executor?.shutdownNow()
        executor = null
    }

    private fun handlePairingCodeSubmission(intent: Intent) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val code = remoteInput?.getCharSequence(SelfPairingNotificationHelper.KEY_PAIRING_CODE)
            ?.toString()

        if (!code.isNullOrBlank()) {
            val codeInt = code.toIntOrNull()
            if (codeInt != null) {
                onPairingCodeReceived(codeInt)
            } else {
                // Invalid format - restart discovery
                restartDiscoveryAfterFailure()
            }
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("DefaultLocale")
    private fun startPairingServiceDiscovery() {
        executor?.submit {
            try {
                // Setup multicast for mDNS
                val wifi = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                multicastLock = wifi.createMulticastLock("adb_self_device_lock").apply {
                    setReferenceCounted(true)
                    acquire()
                }

                // Get local IP address
                val wifiInfo = wifi.connectionInfo
                localIpAddress = String.format(
                    "%d.%d.%d.%d",
                    wifiInfo.ipAddress and 0xff,
                    wifiInfo.ipAddress shr 8 and 0xff,
                    wifiInfo.ipAddress shr 16 and 0xff,
                    wifiInfo.ipAddress shr 24 and 0xff
                )

                Log.d(TAG, "Local IP address: $localIpAddress")
                Log.d(TAG, "Starting mDNS discovery for self device pairing...")

                // Create JmDNS instance
                jmDns = JmDNS.create(InetAddress.getByName(localIpAddress))

                // Add listener for pairing service
                jmDns?.addServiceListener(
                    "_adb-tls-pairing._tcp.local.",
                    createPairingServiceListener()
                )

                // Set timeout
                discoveryTimeout = executor?.schedule({
                    if (discoveredPairingPort == null) {
                        Log.d(TAG, "Discovery timeout - no self device pairing service found")
                        notificationHelper.showFailureNotification(
                            "Timeout - open pairing dialog in Wireless Debugging"
                        )
                        stopSelf()
                    }
                }, 60, TimeUnit.SECONDS)

            } catch (e: Exception) {
                Log.e(TAG, "Error in mDNS discovery", e)
                notificationHelper.showFailureNotification("Discovery error: ${e.message}")
                stopSelf()
            }
        }
    }

    private fun createPairingServiceListener(): ServiceListener {
        return object : ServiceListener {
            override fun serviceAdded(event: ServiceEvent) {
                jmDns?.getServiceInfo(event.type, event.name)
            }

            override fun serviceRemoved(event: ServiceEvent) {
                Log.d(TAG, "Pairing service removed: ${event.name}")
            }

            override fun serviceResolved(event: ServiceEvent) {
                val info = event.info
                val ip = info.inet4Addresses.firstOrNull()?.hostAddress ?: return
                val port = info.port

                Log.d(TAG, "Found pairing service at $ip:$port (local IP: $localIpAddress)")

                // Only accept if it's our own device
                if (ip == localIpAddress && discoveredPairingPort == null) {
                    Log.d(TAG, "Self device pairing service detected!")
                    discoveredPairingPort = port
                    discoveryTimeout?.cancel(false)

                    notificationHelper.showEnterCodeNotification(
                        SelfPairingService::class.java
                    )
                } else if (ip != localIpAddress) {
                    Log.d(TAG, "Ignoring pairing service from other device: $ip")
                }
            }
        }
    }

    private fun onPairingCodeReceived(code: Int) {
        if (isProcessing) {
            Log.d(TAG, "Already processing, ignoring duplicate code submission")
            return
        }
        isProcessing = true

        Log.d(TAG, "Pairing code received: $code")

        val ip = localIpAddress
        val port = discoveredPairingPort

        if (ip == null || port == null) {
            Log.e(TAG, "Cannot pair - IP or port is null")
            notificationHelper.showFailureNotification("Pairing service not found")
            stopSelfDelayed()
            return
        }

        notificationHelper.showPairingInProgressNotification()

        mainScope.launch {
            WifiAdbConnection.updateState(WifiAdbState.PairingStarted())
        }

        // Use repository for pairing
        repository.pair(ip, port, code, object : WifiAdbRepositoryImpl.PairingListener {
            override fun onPairingSuccess() {
                Log.d(TAG, "Pairing succeeded! Starting connect discovery...")
                mainScope.launch {
                    WifiAdbConnection.updateState(WifiAdbState.PairingSuccess(ip))
                }
                discoverConnectServiceAndConnect(ip)
            }

            override fun onPairingFailed() {
                Log.e(TAG, "Pairing failed - wrong code, restarting discovery")
                mainScope.launch {
                    WifiAdbConnection.updateState(WifiAdbState.PairingFailed("Pairing failed"))
                }
                // Restart discovery immediately so user can try again with correct code
                restartDiscoveryAfterFailure()
            }
        })
    }

    /**
     * Restart discovery after a pairing failure (e.g., wrong code).
     * Shows a brief message, then immediately goes back to searching state.
     */
    private fun restartDiscoveryAfterFailure() {
        Log.d(TAG, "Restarting discovery after pairing failure...")
        
        // Show brief error message
        notificationHelper.showFailureNotification("Wrong code - open pairing dialog again")
        
        // Reset state for new attempt
        discoveredPairingPort = null
        isProcessing = false
        
        // Close existing mDNS
        try {
            jmDns?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing JmDNS", e)
        }
        jmDns = null
        
        // Wait a moment for user to see message, then restart discovery
        executor?.schedule({
            notificationHelper.showSearchingNotification(SelfPairingService::class.java)
            startPairingServiceDiscovery()
        }, 2, TimeUnit.SECONDS)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("DefaultLocale")
    private fun discoverConnectServiceAndConnect(targetIp: String) {
        executor?.submit {
            try {
                // Wait for connect service to appear
                Thread.sleep(2000)

                // Close pairing mDNS and create new one for connect
                jmDns?.close()
                jmDns = JmDNS.create(InetAddress.getByName(targetIp))

                var connected = false

                jmDns?.addServiceListener(
                    "_adb-tls-connect._tcp.local.",
                    object : ServiceListener {
                        override fun serviceAdded(event: ServiceEvent) {
                            jmDns?.getServiceInfo(event.type, event.name)
                        }

                        override fun serviceRemoved(event: ServiceEvent) {}

                        override fun serviceResolved(event: ServiceEvent) {
                            if (connected) return

                            val info = event.info
                            val ip = info.inet4Addresses.firstOrNull()?.hostAddress ?: return
                            val port = info.port

                            if (ip == targetIp) {
                                Log.d(TAG, "Found self device connect service at $ip:$port")
                                connected = true
                                connectAndSave(ip, port)
                            }
                        }
                    })

                // Fallback after timeout
                executor?.schedule({
                    if (!connected) {
                        Log.d(TAG, "mDNS connect discovery timeout, trying direct ports...")
                        tryDirectConnect(targetIp)
                    }
                }, 8, TimeUnit.SECONDS)

            } catch (e: Exception) {
                Log.e(TAG, "Error discovering connect service", e)
                notificationHelper.showFailureNotification("Connect discovery error")
                stopSelfDelayed()
            }
        }
    }

    private fun tryDirectConnect(ip: String) {
        val ports = listOf(5555, 37373, 42069, 5037)

        for (port in ports) {
            Log.d(TAG, "Trying direct connect to $ip:$port")
            try {
                val adbManager = AdbConnectionManager.getInstance(this)

                if (adbManager.connect(ip, port)) {
                    Log.d(TAG, "Direct connect succeeded on port $port")
                    saveOwnDeviceAndFinish(ip, port)
                    return
                }
            } catch (e: Exception) {
                Log.d(TAG, "Port $port failed: ${e.message}")
            }
        }

        Log.e(TAG, "All direct connect attempts failed")
        notificationHelper.showFailureNotification("Connection failed - try manual connect")
        stopSelfDelayed()
    }

    private fun connectAndSave(ip: String, port: Int) {
        executor?.submit {
            try {
                val adbManager = AdbConnectionManager.getInstance(this)

                if (adbManager.connect(ip, port)) {
                    Log.d(TAG, "Connected to self device at $ip:$port")
                    saveOwnDeviceAndFinish(ip, port)
                } else {
                    Log.e(TAG, "Connection failed, trying fallback ports")
                    tryDirectConnect(ip)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connection error", e)
                tryDirectConnect(ip)
            }
        }
    }

    private fun saveOwnDeviceAndFinish(ip: String, port: Int) {
        try {
            val adbManager = AdbConnectionManager.getInstance(this)

            // Get device serial number
            val serial = getDeviceProperty(adbManager, "ro.serialno")

            // Get device model name
            val deviceName = getDeviceProperty(adbManager, "ro.product.model") ?: "This Device"

            val ownDevice = WifiAdbDevice(
                ip = ip,
                port = port,
                deviceName = "$deviceName (This Device)",
                isPaired = true,
                lastConnected = System.currentTimeMillis(),
                serialNumber = serial,
                isOwnDevice = true
            )

            storage.saveDevice(ownDevice)
            Log.d(TAG, "Saved self device: ${ownDevice.id}")

            mainScope.launch {
                WifiAdbConnection.updateState(WifiAdbState.ConnectSuccess(ip))
            }

            notificationHelper.showSuccessNotification()
            stopSelfDelayed()

        } catch (e: Exception) {
            Log.e(TAG, "Error saving self device", e)
            notificationHelper.showFailureNotification("Error saving device")
            stopSelfDelayed()
        }
    }

    private fun getDeviceProperty(
        adbManager: io.github.muntashirakon.adb.AbsAdbConnectionManager,
        property: String
    ): String? {
        return try {
            val stream = adbManager.openStream("shell:getprop $property")
            val reader = BufferedReader(InputStreamReader(stream.openInputStream()))
            val value = reader.readLine()?.trim()
            stream.close()
            if (value.isNullOrBlank()) null else value
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get property $property", e)
            null
        }
    }

    private fun stopSelfDelayed() {
        executor?.schedule({
            stopSelf()
        }, 2, TimeUnit.SECONDS)
    }
}
