package `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import `in`.hridayan.ashell.shell.domain.usecase.AdbConnectionManager
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.WifiAdbStorage
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbConnection
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import io.github.muntashirakon.adb.AdbStream
import io.github.muntashirakon.adb.LocalServices
import io.github.muntashirakon.adb.android.AndroidUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

class WifiAdbRepositoryImpl(private val context: Context) : WifiAdbRepository {

    private val TAG = "WifiAdbShell"
    private var adbShellStream: AdbStream? = null
    private val executor = Executors.newScheduledThreadPool(1)
    private var jmDns: JmDNS? = null
    private val pairingInProgress = mutableSetOf<String>()
    private val connectInProgress = mutableSetOf<String>()
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private val storage = WifiAdbStorage(context)

    override fun discoverAdbPairingService(
        pairingCode: Int,
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
                                    callback?.onPairingSuccess(ip, port)

                                    executor.schedule({
                                        Log.d(TAG, "Discovering ADB connect service after delay...")
                                        mainScope.launch {
                                            WifiAdbConnection.updateState(
                                                WifiAdbState.DiscoveryStarted("connect service discovery started")
                                            )
                                        }
                                        discoverConnectService(callback)
                                    }, 1, TimeUnit.SECONDS)
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
     */
    @Suppress("DEPRECATION")
    @SuppressLint("DefaultLocale")
    private fun discoverConnectService(callback: MdnsDiscoveryCallback?) {
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
                Log.d(TAG, "Creating new JmDNS for connect on $ipAddress")

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

                        if (connectInProgress.contains(key)) {
                            Log.d(TAG, "Connection already in progress for $key, skipping...")
                            return
                        }

                        mainScope.launch {
                            WifiAdbConnection.updateState(WifiAdbState.DiscoveryFound(key))
                        }

                        Log.d(TAG, "Found ADB connect service at $key")

                        if (event.type.contains("_adb-tls-connect")) {
                            connectInProgress.add(key)

                            mainScope.launch {
                                WifiAdbConnection.updateState(WifiAdbState.ConnectStarted(key))
                            }

                            connect(ip, port, object : ConnectionListener {
                                override fun onConnectionSuccess() {
                                    connectInProgress.remove(key)
                                    mainScope.launch {
                                        WifiAdbConnection.updateState(
                                            WifiAdbState.ConnectSuccess(
                                                key
                                            )
                                        )
                                    }
                                    Log.d(TAG, "Connected successfully to $ip:$port")
                                    callback?.onPairingSuccess(ip, port)
                                }

                                override fun onConnectionFailed() {
                                    connectInProgress.remove(key)
                                    mainScope.launch {
                                        WifiAdbConnection.updateState(WifiAdbState.ConnectFailed(key))
                                    }
                                    Log.e(TAG, "Failed to connect to $ip:$port")
                                    callback?.onPairingFailed(ip, port)
                                }
                            })
                        }
                    }
                })

                Log.d(TAG, "Started discovery for _adb-tls-connect._tcp.local.")

            } catch (e: Exception) {
                Log.e(TAG, "Error discovering connect service", e)
            }
        }
    }

    override fun pair(ip: String, port: Int, pairingCode: Int, listener: PairingListener?) {
        executor.submit {
            try {
                val manager = AdbConnectionManager.getInstance(context)
                val status = manager.pair(ip, port, pairingCode.toString())
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
                val connected = manager.connect(ip ?: AndroidUtils.getHostIpAddress(context), port)
                if (connected) {
                    callback?.onConnectionSuccess()
                    storage.saveDevice(WifiAdbDevice(ip ?: "", port, isPaired = true))
                } else callback?.onConnectionFailed()
            } catch (e: Throwable) {
                Log.e(TAG, "connect() failed", e)
                callback?.onConnectionFailed()
            }
        }
    }

    override fun execute(commandText: String): Flow<OutputLine> = flow {
        val manager = AdbConnectionManager.getInstance(context)

        if (!manager.isConnected) {
            emit(OutputLine("ADB not connected", isError = true))
            return@flow
        }

        try {
            if (adbShellStream == null || adbShellStream!!.isClosed) {
                adbShellStream = manager.openStream(LocalServices.SHELL)
            }

            val output = adbShellStream!!.openOutputStream()
            val input = adbShellStream!!.openInputStream()
            val reader = BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8))

            output.write("$commandText\n".toByteArray(StandardCharsets.UTF_8))
            output.flush()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                emit(OutputLine(line.orEmpty(), isError = false))
            }

        } catch (e: Exception) {
            emit(OutputLine("Error: ${e.message}", isError = true))
            Log.e("WifiAdbShell", "execute() failed", e)
        } finally {
            abortShell()
        }
    }.flowOn(Dispatchers.IO)

    override fun abortShell() {
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
            mainScope.launch { WifiAdbConnection.updateState(WifiAdbState.None) }
        } catch (e: Exception) {
            Log.e(TAG, "Error closing JmDNS", e)
        }
    }

    override fun getSavedDevices(): List<WifiAdbDevice> = storage.getDevices()

    interface PairingListener {
        fun onPairingSuccess()
        fun onPairingFailed()
    }

    interface ConnectionListener {
        fun onConnectionSuccess()
        fun onConnectionFailed()
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
