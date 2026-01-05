package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository

import `in`.hridayan.ashell.shell.common.domain.model.OutputLine
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.ConnectionListener
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.MdnsDiscoveryCallback
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.PairingListener
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.ReconnectListener
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
import kotlinx.coroutines.flow.Flow

interface WifiAdbRepository {
    fun discoverAdbPairingService(
        pairingCode: Int,
        autoPair: Boolean = true,
        callback: MdnsDiscoveryCallback? = null
    )
    fun pair(ip: String, port: Int, pairingCode: Int, listener: PairingListener? = null)
    fun connect(ip: String?, port: Int, callback: ConnectionListener? = null)
    fun execute(commandText: String): Flow<OutputLine>
    fun abortShell()
    fun stopMdnsDiscovery()
    fun getSavedDevices(): List<WifiAdbDevice>
    
    // New methods for reconnect functionality
    fun reconnect(device: WifiAdbDevice, listener: ReconnectListener? = null)
    fun disconnect()
    fun isConnected(): Boolean
    fun getCurrentDevice(): WifiAdbDevice?
    fun forgetDevice(device: WifiAdbDevice)
}
