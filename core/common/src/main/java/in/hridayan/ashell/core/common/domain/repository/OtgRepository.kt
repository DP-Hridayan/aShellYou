package `in`.hridayan.ashell.core.common.domain.repository

import com.cgutman.adblib.AdbConnection
import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import kotlinx.coroutines.flow.Flow

interface OtgRepository {
    fun searchDevices()
    fun disconnect()
    fun unRegister()
    fun runOtgCommand(command: String): Flow<OutputLine>
    fun stopCommand()

    // Added for file browser support
    fun isConnected(): Boolean
    fun getAdbConnection(): AdbConnection?
}
