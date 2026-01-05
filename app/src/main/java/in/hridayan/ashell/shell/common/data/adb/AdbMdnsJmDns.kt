package `in`.hridayan.ashell.shell.common.data.adb

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener

/**
 * ADB mDNS discovery class written in Kotlin.
 * Detects "_adb-tls-pairing._tcp.local." and "_adb-tls-connect._tcp.local." services.
 *
 * Works without root. Requires Wi-Fi connection on same network.
 */
class AdbMdnsJmDns(
    private val context: Context,
    private val serviceType: String,
    private val listener: OnAdbServiceDiscovered
) {
    interface OnAdbServiceDiscovered {
        fun onServiceFound(name: String, ip: String, port: Int)
        fun onServiceLost(name: String)
    }

    companion object {
        private const val TAG = "AdbMdnsJmDNS"
    }

    private var jmDNS: JmDNS? = null
    private var serviceListener: ServiceListener? = null

    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val wifiAddress = getWifiIpAddress(context)
                if (wifiAddress == null) {
                    Log.e(TAG, "No valid Wi-Fi IP found, aborting mDNS start.")
                    return@launch
                }

                jmDNS = JmDNS.create(wifiAddress)
                Log.d(TAG, "mDNS started on ${wifiAddress.hostAddress}")

                serviceListener = object : ServiceListener {
                    override fun serviceAdded(event: ServiceEvent) {
                        Log.d(TAG, "Service added: ${event.name}")
                        jmDNS?.requestServiceInfo(event.type, event.name, true)
                    }

                    override fun serviceRemoved(event: ServiceEvent) {
                        Log.d(TAG, "Service removed: ${event.name}")
                        listener.onServiceLost(event.name)
                    }

                    override fun serviceResolved(event: ServiceEvent) {
                        val info: ServiceInfo = event.info
                        val addresses = info.hostAddresses
                        if (addresses.isNotEmpty()) {
                            val ip = addresses[0]
                            val port = info.port
                            Log.d(TAG, "Resolved ${event.name} at $ip:$port")
                            listener.onServiceFound(event.name, ip, port)
                        }
                    }
                }

                jmDNS?.addServiceListener(serviceType, serviceListener)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to start JmDNS: $e")
            }
        }
    }

    fun stop() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                jmDNS?.let { dns ->
                    serviceListener?.let {
                        dns.removeServiceListener(serviceType, it)
                    }
                    dns.close()
                    Log.d(TAG, "mDNS stopped.")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error stopping JmDNS: $e")
            }
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("DefaultLocale")
    private fun getWifiIpAddress(context: Context): InetAddress? {
        return try {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ip = wifiManager.connectionInfo?.ipAddress ?: return null
            if (ip == 0) return null

            val ipString = String.format(
                "%d.%d.%d.%d",
                (ip and 0xff),
                (ip shr 8 and 0xff),
                (ip shr 16 and 0xff),
                (ip shr 24 and 0xff)
            )
            InetAddress.getByName(ipString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Wi-Fi IP: $e")
            null
        }
    }
}