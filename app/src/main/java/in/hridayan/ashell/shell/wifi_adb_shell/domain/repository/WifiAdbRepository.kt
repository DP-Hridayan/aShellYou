package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository

import `in`.hridayan.ashell.shell.domain.model.OutputLine
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.ConnectionListener
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.MdnsDiscoveryCallback
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.PairingListener
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

    fun stop()

    fun stopMdnsDiscovery()
}