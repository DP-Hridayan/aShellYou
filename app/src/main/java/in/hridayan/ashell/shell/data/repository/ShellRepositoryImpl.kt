package `in`.hridayan.ashell.shell.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import `in`.hridayan.ashell.shell.domain.repository.ShellRepository
import `in`.hridayan.ashell.shell.domain.usecase.ShellCommandExecutor
import `in`.hridayan.ashell.shell.domain.usecase.ShizukuPermissionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ShellRepositoryImpl @Inject constructor(
    private val shellCommandExecutor: ShellCommandExecutor,
    private val shizukuPermissionHandler: ShizukuPermissionHandler,
    @ApplicationContext private val context: Context
): ShellRepository{
    override fun isShizukuInstalled() : Boolean {
        return shizukuPermissionHandler.isShizukuInstalled()
    }

    override fun hasShizukuPermission(): Boolean {
        return shizukuPermissionHandler.hasPermission()
    }

    override fun shizukuPermissionState(): StateFlow<Boolean>{
        return shizukuPermissionHandler.permissionGranted
    }

    override fun refreshShizukuPermission() {
        return shizukuPermissionHandler.refreshPermissionState()
    }

    override fun requestShizukuPermission() {
        return shizukuPermissionHandler.requestPermission()
    }

    override suspend fun executeBasicCommand(command: String): Flow<OutputLine> {
        return shellCommandExecutor.runBasic(command, context)
    }

    override suspend fun executeRootCommand(command: String): Flow<OutputLine> {
        return shellCommandExecutor.runRoot(command)
    }

    override suspend fun executeShizukuCommand(command: String): Flow<OutputLine> {
        return shellCommandExecutor.runShizuku(command)
    }

    override fun stopCommand() {
        return shellCommandExecutor.stop()
    }
}