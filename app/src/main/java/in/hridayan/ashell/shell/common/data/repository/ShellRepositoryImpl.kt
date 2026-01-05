package `in`.hridayan.ashell.shell.common.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.shell.common.domain.model.OutputLine
import `in`.hridayan.ashell.shell.common.domain.repository.ShellRepository
import `in`.hridayan.ashell.shell.common.domain.usecase.ShellCommandExecutor
import `in`.hridayan.ashell.shell.common.domain.usecase.ShizukuPermissionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ShellRepositoryImpl @Inject constructor(
    private val shellCommandExecutor: ShellCommandExecutor,
    private val shizukuPermissionHandler: ShizukuPermissionHandler,
    @param:ApplicationContext private val context: Context
) : ShellRepository {

    override fun hasShizukuPermission(): Boolean {
        return shizukuPermissionHandler.hasPermission()
    }

    override fun shizukuPermissionState(): StateFlow<Boolean> {
        return shizukuPermissionHandler.permissionGranted
    }

    override fun refreshShizukuPermission() {
        return shizukuPermissionHandler.refreshPermissionState()
    }

    override fun requestShizukuPermission() {
        return shizukuPermissionHandler.requestPermission()
    }

    override fun hasRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = process.outputStream

            outputStream.write("id\n".toByteArray())
            outputStream.flush()

            outputStream.write("exit\n".toByteArray())
            outputStream.flush()

            val result = process.waitFor()
            result == 0
        } catch (e: Exception) {
            false
        }
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