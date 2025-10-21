package `in`.hridayan.ashell.shell.data.adb

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import `in`.hridayan.ashell.App.Companion.appContext
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import `in`.hridayan.ashell.shell.domain.usecase.AdbConnectionManager
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import io.github.muntashirakon.adb.AdbStream
import io.github.muntashirakon.adb.LocalServices
import io.github.muntashirakon.adb.android.AdbMdns
import io.github.muntashirakon.adb.android.AndroidUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

class WifiAdbShell {

    private val TAG = "WifiAdbShell"
    private var adbShellStream: AdbStream? = null
    private val executor = Executors.newSingleThreadExecutor()

    private var pairingMdns: AdbMdns? = null
    private var connectMdns: AdbMdns? = null


    /**
     * Called by host when QR is shown.
     * This starts mDNS discovery for incoming pairing request broadcasts.
     */
    fun waitForPairRequest(sessionId: String, pairingCode: Int, callback: StateCallback?) {
        Thread {
            try {
                Log.d(TAG, "🔍 Listening for devices requesting pairing (session=$sessionId)...")

                val pairingListener = object : AdbMdns.OnAdbDaemonDiscoveredListener {
                    override fun onPortChanged(host: InetAddress?, port: Int, serviceName: String?) {
                        if (host == null || port <= 0) return
                        val ip = host.hostAddress ?: return

                        // Filter based on sessionId if serviceName includes it
                        if (serviceName?.contains(sessionId, ignoreCase = true) == false) {
                            Log.d(TAG, "⚠️ Ignored service $serviceName (session mismatch)")
                            return
                        }

                        Log.d(TAG, "📡 Device requesting pairing found: $serviceName ($ip:$port)")
                        callback?.onStateChanged(
                            WifiAdbState.PairingStarted("Pairing with $ip:$port")
                        )

                        // Attempt pairing
                        pair(ip, port, pairingCode, object : PairingListener {
                            override fun onPairingSuccess() {
                                Log.d(TAG, "✅ Pairing success with $ip:$port")
                                callback?.onStateChanged(
                                    WifiAdbState.PairingSuccess("Paired with $ip")
                                )

                                // Stop pairing discovery
                                stopPairDiscovery()
                                // Now discover connect service for this device
                                discoverConnectService(ip, callback)
                            }

                            override fun onPairingFailed() {
                                Log.e(TAG, "❌ Pairing failed with $ip:$port")
                                callback?.onStateChanged(
                                    WifiAdbState.PairingFailed("Pairing failed for $ip")
                                )
                            }
                        })
                    }
                }

                val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val multicastLock = wifiManager?.createMulticastLock("adbMdnsLock")
                multicastLock?.setReferenceCounted(true)
                multicastLock?.acquire()

                pairingMdns = AdbMdns(
                    appContext,
                    AdbMdns.SERVICE_TYPE_TLS_PAIRING,
                    pairingListener
                )

                pairingMdns?.start()
                Log.d(TAG, "✅ Waiting for pairing request...")
                callback?.onStateChanged(WifiAdbState.DiscoveryStarted("Waiting for device..."))

                // Timeout (optional)
                Thread.sleep(30_000)
                if (pairingMdns != null) {
                    Log.w(TAG, "⏰ Timeout waiting for pairing request")
                    stopPairDiscovery()
                    callback?.onStateChanged(WifiAdbState.PairingFailed("Timeout"))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in waitForPairRequest()", e)
                callback?.onStateChanged(
                    WifiAdbState.PairingFailed("Error: ${e.message}")
                )
            }
        }.start()
    }

    private fun discoverConnectService(targetIp: String, callback: StateCallback?) {
        Log.d(TAG, "🌐 Discovering connect service for $targetIp...")

        connectMdns = AdbMdns(
            appContext,
            AdbMdns.SERVICE_TYPE_TLS_CONNECT,
            object : AdbMdns.OnAdbDaemonDiscoveredListener {
                override fun onPortChanged(host: InetAddress?, port: Int, serviceName: String?) {
                    if (host == null || port <= 0) return
                    val ip = host.hostAddress ?: return

                    if (ip != targetIp) {
                        Log.d(TAG, "⚠️ Ignored connect service from different IP: $ip")
                        return
                    }

                    Log.d(TAG, "🔗 Found connect service: $serviceName ($ip:$port)")
                    callback?.onStateChanged(WifiAdbState.ConnectStarted())

                    stopConnectDiscovery()

                    connect(ip, port, object : ConnectionListener {
                        override fun onConnectionSuccess() {
                            val deviceName = serviceName ?: ip
                            callback?.onStateChanged(
                                WifiAdbState.ConnectSuccess("Connected to $deviceName ($ip)")
                            )
                        }

                        override fun onConnectionFailed() {
                            callback?.onStateChanged(
                                WifiAdbState.ConnectFailed("Failed to connect to $ip")
                            )
                        }
                    })
                }
            }
        )
        connectMdns?.start()
    }

    private fun stopPairDiscovery() {
        pairingMdns?.stop()
        pairingMdns = null
    }

    private fun stopConnectDiscovery() {
        connectMdns?.stop()
        connectMdns = null
    }


    fun pair(ip: String, port: Int, pairingCode: Int, listener: PairingListener? = null) {
        executor.submit {
            try {
                val manager = AdbConnectionManager.getInstance(appContext)
                val status = manager.pair(ip, port, pairingCode.toString())
                if (status) listener?.onPairingSuccess() else listener?.onPairingFailed()
            } catch (e: Throwable) {
                Log.e(TAG, "pair() failed", e)
                listener?.onPairingFailed()
            }
        }
    }

    fun connect(ip: String?, port: Int, callback: ConnectionListener? = null) {
        executor.submit {
            try {
                val manager = AdbConnectionManager.getInstance(appContext)
                val connected = manager.connect(ip ?: AndroidUtils.getHostIpAddress(appContext), port)
                if (connected) callback?.onConnectionSuccess() else callback?.onConnectionFailed()
            } catch (e: Throwable) {
                Log.e(TAG, "connect() failed", e)
                callback?.onConnectionFailed()
            }
        }
    }

    fun execute(command: String): Flow<OutputLine> = callbackFlow {
        val manager = AdbConnectionManager.getInstance(appContext)
        if (!manager.isConnected) {
            trySend(OutputLine("ADB not connected", true))
            close()
            return@callbackFlow
        }
        try {
            if (adbShellStream == null || adbShellStream!!.isClosed)
                adbShellStream = manager.openStream(LocalServices.SHELL)

            val out = adbShellStream!!.openOutputStream()
            val input = adbShellStream!!.openInputStream()
            out.write("$command\n".toByteArray(StandardCharsets.UTF_8))
            out.flush()

            val reader = BufferedReader(InputStreamReader(input))
            var line: String?
            while (reader.readLine().also { line = it } != null)
                trySend(OutputLine(line ?: "", false))
        } catch (e: Exception) {
            trySend(OutputLine("Error: ${e.message}", true))
        }
        awaitClose { stop() }
    }.flowOn(Dispatchers.IO)

    fun stop() {
        try {
            adbShellStream?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing adbShellStream", e)
        }
        adbShellStream = null
    }

    // Interfaces same as before
    interface PairingListener {
        fun onPairingSuccess()
        fun onPairingFailed()
    }

    interface ConnectionListener {
        fun onConnectionSuccess()
        fun onConnectionFailed()
    }

    interface StateCallback {
        fun onStateChanged(state: WifiAdbState)
    }
}
