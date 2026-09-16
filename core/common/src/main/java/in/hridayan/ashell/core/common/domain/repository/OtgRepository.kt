package `in`.hridayan.ashell.core.common.domain.repository

import com.cgutman.adblib.AdbConnection
import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import kotlinx.coroutines.flow.Flow

interface OtgRepository {
    fun searchDevices()
    fun disconnect()
    fun runOtgCommand(command: String): Flow<OutputLine>
    fun stopCommand()

    /** True only while the ADB handshake has completed and the connection is still alive. */
    fun isConnected(): Boolean

    /** The live connection, or null when there is none or it has died. */
    fun getAdbConnection(): AdbConnection?
}
