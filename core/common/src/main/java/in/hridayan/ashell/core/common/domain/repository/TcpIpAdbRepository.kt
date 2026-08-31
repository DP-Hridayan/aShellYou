package `in`.hridayan.ashell.core.common.domain.repository

import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import kotlinx.coroutines.flow.Flow

/**
 * Minimal ADB-over-TCP interface used by features that need loopback TCP/IP ADB
 * without depending on the wifi_adb_shell feature's repository directly.
 */
interface TcpIpAdbRepository {
    fun connect(ip: String?, port: Int, callback: TcpIpConnectionListener?)
    fun execute(commandText: String): Flow<OutputLine>
    fun isConnected(): Boolean
    fun abortShell()

    interface TcpIpConnectionListener {
        fun onConnectionSuccess()
        fun onConnectionFailed()
    }
}
