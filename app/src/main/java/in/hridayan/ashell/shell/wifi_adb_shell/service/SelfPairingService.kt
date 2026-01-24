package `in`.hridayan.ashell.shell.wifi_adb_shell.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.RemoteInput
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.shell.wifi_adb_shell.utils.WirelessDebuggingUtils
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbConnection
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbEvent
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import `in`.hridayan.ashell.shell.wifi_adb_shell.notification.SelfPairingNotificationHelper
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Foreground service for pairing with the device the app is running on (self/own device).
 *
 * Uses Android's native NsdManager (via AdbMdnsDiscovery) for reliable mDNS discovery.
 *
 * Flow:
 * 1. User clicks Developer Options → Service starts and shows "Searching..." notification
 * 2. NsdManager discovers pairing service (filtered by matching network interfaces)
 * 3. Shows "Enter code" notification with input field
 * 4. User enters pairing code
 * 5. Pairs using existing WifiAdbRepository logic
 * 6. NsdManager discovers connect service
 * 7. Connects and saves device with isOwnDevice=true
 */
@RequiresApi(Build.VERSION_CODES.R)
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

    // Hilt EntryPoint for service injection
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SelfPairingServiceEntryPoint {
        fun wifiAdbRepository(): WifiAdbRepository
    }

    // Dependencies
    private lateinit var notificationHelper: SelfPairingNotificationHelper
    private lateinit var repository: WifiAdbRepository
    private val mainScope = CoroutineScope(Dispatchers.Main)

    // NsdManager-based mDNS discovery (replaces JmDNS)
    private var mdnsDiscovery: AdbMdnsDiscovery? = null
    private var executor: ScheduledExecutorService? = null
    private var discoveryTimeout: ScheduledFuture<*>? = null

    // State
    private var discoveredPairingIp: String? = null
    private var discoveredPairingPort: Int? = null
    private var discoveredConnectPort: Int? = null
    @Volatile private var isProcessing = false
    @Volatile private var isPairingDone = false
    @Volatile private var isConnected = false

    override fun onCreate() {
        super.onCreate()
        notificationHelper = SelfPairingNotificationHelper(this)
        
        // Get repository via Hilt EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SelfPairingServiceEntryPoint::class.java
        )
        repository = entryPoint.wifiAdbRepository()
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
                startMdnsDiscovery()
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

        // Stop mDNS discovery
        mdnsDiscovery?.stop()
        mdnsDiscovery = null

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
        discoveredPairingIp = null
        discoveredPairingPort = null
        discoveredConnectPort = null
        isProcessing = false
        isPairingDone = false
        isConnected = false
    }

    /**
     * Full cleanup when service is stopping.
     */
    private fun cleanup() {
        Log.d(TAG, "Cleaning up service")
        discoveryTimeout?.cancel(true)

        mdnsDiscovery?.stop()
        mdnsDiscovery = null

        executor?.shutdownNow()
        executor = null
    }

    private fun handlePairingCodeSubmission(intent: Intent) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val code = remoteInput?.getCharSequence(SelfPairingNotificationHelper.KEY_PAIRING_CODE)
            ?.toString()?.trim()

        if (!code.isNullOrBlank()) {
            // Validate that code contains only digits (6 digit pairing code)
            if (code.all { it.isDigit() } && code.length == 6) {
                onPairingCodeReceived(code)
            } else {
                // Invalid format - show error and keep listening
                Log.d(TAG, "Invalid pairing code format: $code")
                notificationHelper.showFailureNotification(getString(R.string.self_pair_wrong_code))
                executor?.schedule({
                    notificationHelper.showEnterCodeNotification(SelfPairingService::class.java)
                }, 2, TimeUnit.SECONDS)
            }
        }
    }

    /**
     * Start mDNS discovery using NsdManager.
     * This discovers BOTH pairing and connect services simultaneously.
     */
    private fun startMdnsDiscovery() {
        Log.d(TAG, "Starting NsdManager-based mDNS discovery...")

        mdnsDiscovery = AdbMdnsDiscovery(this, object : AdbMdnsDiscovery.AdbFoundCallback {
            override fun onPairingServiceFound(ipAddress: String, port: Int) {
                if (isPairingDone) {
                    Log.d(TAG, "Pairing service found but pairing already done, ignoring")
                    return
                }
                
                if (isProcessing) {
                    Log.d(TAG, "Pairing service found but pairing in progress, ignoring")
                    return
                }

                // Always update to the latest service info (handles dialog close/reopen)
                Log.d(TAG, "Pairing service detected at $ipAddress:$port")
                discoveredPairingIp = ipAddress
                discoveredPairingPort = port
                discoveryTimeout?.cancel(false)

                // Show notification to enter pairing code
                notificationHelper.showEnterCodeNotification(SelfPairingService::class.java)
            }
            
            override fun onPairingServiceLost() {
                if (isPairingDone || isProcessing) {
                    Log.d(TAG, "Pairing service lost but pairing done/in progress, ignoring")
                    return
                }
                
                // Debounce: only clear state if service doesn't reappear in 3 seconds
                // This handles cases where user briefly closes/reopens the pairing dialog
                Log.d(TAG, "Pairing service lost - scheduling state clear after delay")
                executor?.schedule({
                    if (!isPairingDone && !isProcessing && discoveredPairingIp != null) {
                        Log.d(TAG, "Clearing stale pairing state after debounce")
                        discoveredPairingIp = null
                        discoveredPairingPort = null
                        notificationHelper.showSearchingNotification(SelfPairingService::class.java)
                    }
                }, 3, TimeUnit.SECONDS)
            }

            override fun onConnectServiceFound(ipAddress: String, port: Int) {
                if (isConnected) {
                    Log.d(TAG, "Connect service found but already connected, ignoring")
                    return
                }

                // ALWAYS store the connect port when found, even before pairing completes
                // The connect service may be discovered before pairing and then disappear
                if (discoveredPairingIp == null || discoveredPairingIp == ipAddress) {
                    Log.d(TAG, "Connect service detected at $ipAddress:$port - storing for later use")
                    discoveredConnectPort = port

                    // If pairing is already done, connect immediately
                    if (isPairingDone) {
                        Log.d(TAG, "Pairing already done, connecting now...")
                        connectAndSave(ipAddress, port)
                    }
                } else {
                    Log.d(TAG, "Ignoring connect service at $ipAddress:$port (different from pairing IP $discoveredPairingIp)")
                }
            }
        })

        mdnsDiscovery?.start()

        // Set timeout for pairing service discovery
        discoveryTimeout = executor?.schedule({
            if (discoveredPairingPort == null) {
                Log.d(TAG, "Discovery timeout - no self device pairing service found")
                notificationHelper.showFailureNotification(
                    getString(R.string.self_pair_timeout)
                )
                stopSelf()
            }
        }, 2, TimeUnit.MINUTES)
    }

    private fun onPairingCodeReceived(code: String) {
        if (isProcessing) {
            Log.d(TAG, "Already processing, ignoring duplicate code submission")
            return
        }
        isProcessing = true

        Log.d(TAG, "Pairing code received: $code")

        val ip = discoveredPairingIp
        val port = discoveredPairingPort

        if (ip == null || port == null) {
            Log.e(TAG, "Cannot pair - IP or port is null")
            notificationHelper.showFailureNotification(getString(R.string.self_pair_pairing_not_found))
            stopSelfDelayed()
            return
        }

        notificationHelper.showPairingInProgressNotification()

        mainScope.launch {
            WifiAdbConnection.updateState(WifiAdbState.Pairing())
        }

        // Use repository for pairing
        repository.pair(ip, port, code, object : WifiAdbRepositoryImpl.PairingListener {
            override fun onPairingSuccess() {
                Log.d(TAG, "Pairing succeeded!")
                isPairingDone = true
                mainScope.launch {
                    WifiAdbConnection.tryEmitEvent(WifiAdbEvent.PairingSuccess(ip))
                }

                // Check if we already have a connect port from earlier discovery
                val connectPort = discoveredConnectPort
                if (connectPort != null) {
                    Log.d(TAG, "Using previously discovered connect port: $connectPort")
                    connectAndSave(ip, connectPort)
                } else {
                    // Wait a bit for the connect service to be discovered via mDNS
                    Log.d(TAG, "Waiting for connect service discovery...")
                    executor?.schedule({
                        if (!isConnected) {
                            // Check again if connect port was discovered while waiting
                            val port = discoveredConnectPort
                            if (port != null) {
                                Log.d(TAG, "Connect port discovered during wait: $port")
                                connectAndSave(ip, port)
                            } else {
                                Log.d(TAG, "Connect service not found via mDNS, trying direct connect...")
                                tryDirectConnect(ip)
                            }
                        }
                    }, 3, TimeUnit.SECONDS)
                }
            }

            override fun onPairingFailed() {
                Log.e(TAG, "Pairing failed - wrong code")
                isProcessing = false
                mainScope.launch {
                    WifiAdbConnection.updateState(WifiAdbState.Idle)
                    WifiAdbConnection.tryEmitEvent(WifiAdbEvent.PairingFailed("Pairing failed"))
                }
                // Show error and let user try again
                notificationHelper.showFailureNotification(getString(R.string.self_pair_wrong_code))
                executor?.schedule({
                    notificationHelper.showEnterCodeNotification(SelfPairingService::class.java)
                }, 2, TimeUnit.SECONDS)
            }
        })
    }

    private fun connectAndSave(ip: String, port: Int) {
        if (isConnected) return

        executor?.submit {
            try {
                val adbManager = AdbConnectionManager.getInstance(this)

                if (adbManager.connect(ip, port)) {
                    Log.d(TAG, "Connected to self device at $ip:$port")
                    isConnected = true
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

    private fun tryDirectConnect(ip: String) {
        if (isConnected) return

        val ports = listOf(5555, 37373, 42069, 5037)

        for (port in ports) {
            Log.d(TAG, "Trying direct connect to $ip:$port")
            try {
                val adbManager = AdbConnectionManager.getInstance(this)

                if (adbManager.connect(ip, port)) {
                    Log.d(TAG, "Direct connect succeeded on port $port")
                    isConnected = true
                    saveOwnDeviceAndFinish(ip, port)
                    return
                }
            } catch (e: Exception) {
                Log.d(TAG, "Port $port failed: ${e.message}")
            }
        }

        Log.e(TAG, "All direct connect attempts failed")
        notificationHelper.showFailureNotification(getString(R.string.self_pair_connection_failed))
        stopSelfDelayed()
    }

    @SuppressLint("DefaultLocale")
    private fun saveOwnDeviceAndFinish(ip: String, port: Int) {
        try {
            val adbManager = AdbConnectionManager.getInstance(this)

            // Get device serial number
            val serial = getDeviceProperty(adbManager, "ro.serialno")

            // Get device model name
            val deviceName = getDeviceProperty(adbManager, "ro.product.model") ?: getString(R.string.this_device)

            val ownDevice = WifiAdbDevice(
                ip = ip,
                port = port,
                deviceName = deviceName,
                isPaired = true,
                lastConnected = System.currentTimeMillis(),
                serialNumber = serial,
                isOwnDevice = true
            )

            CoroutineScope(Dispatchers.IO).launch { 
                repository.saveDevice(ownDevice)
            }
            Log.d(TAG, "Saved self device: ${ownDevice.id}")

            mainScope.launch {
                // Set current device in WifiAdbConnection for cross-component state sharing
                WifiAdbConnection.setCurrentDevice(ownDevice)
                // Set Connected state and emit ConnectSuccess event
                WifiAdbConnection.setDeviceConnected(ownDevice.id, "$ip:$port")
                WifiAdbConnection.tryEmitEvent(WifiAdbEvent.ConnectSuccess(ownDevice.id, "$ip:$port"))
            }

            // Grant WRITE_SECURE_SETTINGS permission for future wireless debugging control
            // This allows the app to enable wireless debugging programmatically on reconnects
            try {
                val granted = WirelessDebuggingUtils.grantWriteSecureSettingsViaAdb(this, adbManager)
                Log.d(TAG, "WRITE_SECURE_SETTINGS permission grant: $granted")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to grant WRITE_SECURE_SETTINGS, will use fallback for reconnects", e)
            }

            notificationHelper.showSuccessNotification()
            stopSelfDelayed()

        } catch (e: Exception) {
            Log.e(TAG, "Error saving self device", e)
            notificationHelper.showFailureNotification(getString(R.string.self_pair_error_saving))
            stopSelfDelayed()
        }
    }

    private fun getDeviceProperty(
        adbManager: AbsAdbConnectionManager,
        property: String
    ): String? {
        return try {
            val stream = adbManager.openStream("shell:getprop $property")
            val reader = BufferedReader(InputStreamReader(stream.openInputStream()))
            val result = reader.readLine()?.trim()
            reader.close()
            stream.close()
            if (result.isNullOrBlank()) null else result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting property $property", e)
            null
        }
    }

    private fun stopSelfDelayed() {
        executor?.schedule({
            stopSelf()
        }, 2, TimeUnit.SECONDS)
    }
}
