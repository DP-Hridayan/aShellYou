package `in`.hridayan.ashell.shell.wifi_adb_shell.service

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ext.SdkExtensions
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.IOException
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.util.Collections

/**
 * mDNS discovery helper using Android's native NsdManager.
 * More reliable than JmDNS for discovering ADB pairing/connect services.
 */
@RequiresApi(Build.VERSION_CODES.R)
class AdbMdnsDiscovery(
    context: Context,
    private val callback: AdbFoundCallback
) {
    companion object {
        private const val TAG = "AdbMdnsDiscovery"
        const val TLS_CONNECT = "_adb-tls-connect._tcp"
        const val TLS_PAIRING = "_adb-tls-pairing._tcp"
    }

    interface AdbFoundCallback {
        fun onPairingServiceFound(ipAddress: String, port: Int)
        fun onConnectServiceFound(ipAddress: String, port: Int)
    }

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val resolvedServices = HashSet<String>()
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var running = false

    @Volatile
    private var stopResolving = false

    private val pairingListener = DiscoveryListener(TLS_PAIRING)
    private val connectListener = DiscoveryListener(TLS_CONNECT)

    fun start() {
        if (running) return
        running = true

        try {
            nsdManager.discoverServices(TLS_PAIRING, NsdManager.PROTOCOL_DNS_SD, pairingListener)
            nsdManager.discoverServices(TLS_CONNECT, NsdManager.PROTOCOL_DNS_SD, connectListener)
            Log.d(TAG, "Started mDNS discovery for pairing and connect services")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service discovery", e)
            running = false
        }
    }

    fun stop() {
        if (!running) return
        running = false

        try {
            nsdManager.stopServiceDiscovery(pairingListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping pairing discovery", e)
        }

        try {
            nsdManager.stopServiceDiscovery(connectListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping connect discovery", e)
        }

        resolvedServices.clear()
        handler.removeCallbacksAndMessages(null)
        stopResolving = false
        Log.d(TAG, "Stopped mDNS discovery")
    }

    /**
     * Check if the resolved service's IP matches any of our network interfaces.
     * This is more reliable than comparing with a single IP address.
     */
    private fun isMatchingNetwork(resolvedService: NsdServiceInfo): Boolean {
        val serviceHost =
            if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.TIRAMISU) >= 7) {
                selectBestAddress(resolvedService.hostAddresses)?.hostAddress ?: return false
            } else {
                resolvedService.host?.hostAddress ?: return false
            }

        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in Collections.list(interfaces)) {
                if (networkInterface.isUp) {
                    for (inetAddress in Collections.list(networkInterface.inetAddresses)) {
                        if (serviceHost == inetAddress.hostAddress) {
                            Log.d(
                                TAG,
                                "Service IP $serviceHost matches network interface ${networkInterface.name}"
                            )
                            return true
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error checking network interfaces", e)
        }

        Log.d(TAG, "Service IP $serviceHost does not match any local network interface")
        return false
    }

    /**
     * Check if a port is available (not in use).
     * Returns false if the port is available (nothing using it).
     * Returns true if the port is in use (service is running).
     */
    private fun isPortInUse(port: Int): Boolean {
        return try {
            ServerSocket().use { socket ->
                socket.bind(InetSocketAddress("127.0.0.1", port), 1)
                false // Port is available, so service is NOT running
            }
        } catch (e: IOException) {
            true // Port is in use, service IS running
        }
    }

    private inner class DiscoveryListener(private val serviceType: String) :
        NsdManager.DiscoveryListener {

        override fun onDiscoveryStarted(serviceType: String) {
            Log.d(TAG, "Discovery started: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Start discovery failed: $serviceType, errorCode: $errorCode")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.d(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Stop discovery failed: $serviceType, errorCode: $errorCode")
        }

        override fun onServiceFound(info: NsdServiceInfo) {
            if (!running) return

            val serviceKey = "${info.serviceName}:${info.serviceType}"

            if (!resolvedServices.contains(serviceKey)) {
                resolvedServices.add(serviceKey)
                nsdManager.resolveService(info, ResolveListener(serviceType))

                // Reset the stopResolving timeout
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({ stopResolving = true }, 3 * 60 * 1000)
            } else if (!stopResolving) {
                // Re-resolve if we're still in the resolving window
                nsdManager.resolveService(info, ResolveListener(serviceType))
            }
        }

        override fun onServiceLost(info: NsdServiceInfo) {
            val host =
                if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.TIRAMISU) >= 7) {
                    selectBestAddress(info.hostAddresses)?.hostAddress ?: "unknown"
                } else {
                    info.host?.hostAddress ?: "unknown"
                }


            val serviceKey = "${info.serviceName}:${info.serviceType}"
            resolvedServices.remove(serviceKey)
            Log.d(TAG, "Service lost: $serviceKey ($host:${info.port})")
        }
    }

    private inner class ResolveListener(private val serviceType: String) :
        NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.w(TAG, "Resolve failed for ${serviceInfo.serviceName}: errorCode $errorCode")
        }

        override fun onServiceResolved(resolvedService: NsdServiceInfo) {
            if (!running) return

            val ipAddress =
                if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.TIRAMISU) >= 7) {
                    selectBestAddress(resolvedService.hostAddresses)?.hostAddress ?: return
                } else {
                    resolvedService.host?.hostAddress ?: return
                }

            val portNumber = resolvedService.port

            Log.d(TAG, "Service resolved: $serviceType at $ipAddress:$portNumber")

            // Check if this service is on our network and the port is in use
            if (isMatchingNetwork(resolvedService) && isPortInUse(portNumber)) {
                Log.d(
                    TAG,
                    "Valid self-device service detected: $serviceType at $ipAddress:$portNumber"
                )

                when (serviceType) {
                    TLS_PAIRING -> callback.onPairingServiceFound(ipAddress, portNumber)
                    TLS_CONNECT -> callback.onConnectServiceFound(ipAddress, portNumber)
                }
            } else {
                Log.d(
                    TAG,
                    "Ignoring service: $serviceType at $ipAddress:$portNumber (not on our network or port not in use)"
                )
            }
        }
    }

    private fun selectBestAddress(addresses: List<InetAddress>): InetAddress? {
        return addresses.firstOrNull { it is Inet4Address }
            ?: addresses.firstOrNull { addr ->
                addr is Inet6Address && !addr.isLinkLocalAddress
            }
    }

}
