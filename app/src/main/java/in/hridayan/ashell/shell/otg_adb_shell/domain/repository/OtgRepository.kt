package `in`.hridayan.ashell.shell.otg_adb_shell.domain.repository

import `in`.hridayan.ashell.shell.domain.model.OutputLine
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.model.OtgState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface OtgRepository {
    val state: StateFlow<OtgState>
    fun searchDevices()
    fun disconnect()
    fun unRegister()
    fun runOtgCommand(command: String): Flow<OutputLine>
    fun stopCommand()
}