package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository

import android.graphics.Bitmap
import `in`.hridayan.ashell.shell.common.domain.model.OutputLine
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.ConnectionListener
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.MdnsDiscoveryCallback
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.PairingListener
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.ReconnectListener
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.DiscoveredPairingService
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
import kotlinx.coroutines.flow.Flow

interface WifiAdbRepository {
    fun discoverAdbPairingService(
        pairingCode: String,
        autoPair: Boolean = true,
        callback: MdnsDiscoveryCallback? = null
    )

    fun pair(ip: String, port: Int, pairingCode: String, listener: PairingListener? = null)
    fun connect(ip: String?, port: Int, callback: ConnectionListener? = null)
    fun execute(commandText: String): Flow<OutputLine>
    fun abortShell()
    fun stopMdnsDiscovery()
    
    // Device storage - Flow-based reactive updates
    fun getSavedDevicesFlow(): Flow<List<WifiAdbDevice>>
    suspend fun saveDevice(device: WifiAdbDevice)
    suspend fun updateDevice(device: WifiAdbDevice)
    suspend fun removeDevice(device: WifiAdbDevice)

    // New methods for reconnect functionality
    fun reconnect(device: WifiAdbDevice, listener: ReconnectListener? = null)
    fun cancelReconnect()
    fun disconnect()
    fun isConnected(): Boolean
    fun getCurrentDevice(): WifiAdbDevice?
    fun forgetDevice(device: WifiAdbDevice)

    // Heartbeat mechanism for connection status
    fun startHeartbeat()
    fun stopHeartbeat()

    suspend fun generatePairingQR(sessionId: String, pairingCode: String, size: Int = 512): Bitmap

    // "Pair Using Code" tab - dual discovery for pairing and connect services
    fun startCodePairingDiscovery(
        onPairingServiceFound: (DiscoveredPairingService) -> Unit,
        onPairingServiceLost: (serviceName: String) -> Unit
    )
    fun stopCodePairingDiscovery()
    
    // Pair and connect using cached connect port
    fun pairAndConnect(
        ip: String,
        pairingPort: Int,
        pairingCode: String,
        callback: MdnsDiscoveryCallback?
    )
}
