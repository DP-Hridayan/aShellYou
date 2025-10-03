package `in`.hridayan.ashell.shell.domain.repository

import `in`.hridayan.ashell.shell.domain.model.OutputLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ShellRepository {
    fun hasShizukuPermission(): Boolean
    fun shizukuPermissionState(): StateFlow<Boolean>
    fun requestShizukuPermission()
    fun refreshShizukuPermission()
    suspend fun executeBasicCommand(command: String): Flow<OutputLine>
    suspend fun executeRootCommand(command: String): Flow<OutputLine>
    suspend fun executeShizukuCommand(command: String): Flow<OutputLine>
    fun stopCommand()
}