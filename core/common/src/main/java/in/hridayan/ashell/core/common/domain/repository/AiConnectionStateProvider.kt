package `in`.hridayan.ashell.core.common.domain.repository

import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import kotlinx.coroutines.flow.Flow

interface AiConnectionStateProvider {
    fun getWifiConnectedDeviceName(): String?
    fun isWifiOwnDevice(): Boolean
    fun getWifiPairedDevices(): List<String>
    fun executeWifiCommand(command: String): Flow<OutputLine>
    fun stopWifiCommand()
}
