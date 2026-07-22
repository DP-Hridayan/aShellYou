package `in`.hridayan.ashell.shell.fastboot.domain.repository

import android.net.Uri
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootCommandResult
import `in`.hridayan.ashell.shell.fastboot.domain.model.FastbootDeviceInfo
import `in`.hridayan.ashell.shell.fastboot.domain.model.FlashOperation
import `in`.hridayan.ashell.shell.fastboot.domain.model.RebootMode
import kotlinx.coroutines.flow.Flow

interface FastbootRepository {
    fun searchDevices()
    fun disconnect()
    fun unRegister()
    fun sendCommand(command: String): Flow<FastbootCommandResult>
    fun getDeviceInfo(): Flow<FastbootDeviceInfo>
    fun reboot(mode: RebootMode)
    fun getAllVariables(): Flow<List<Pair<String, String>>>
    fun flashPartition(
        partition: String,
        imageUri: Uri,
        onProgress: (FlashOperation) -> Unit
    ): Flow<FastbootCommandResult>

    fun erasePartition(
        partition: String,
        onProgress: (FlashOperation) -> Unit
    ): Flow<FastbootCommandResult>

    fun bootImage(imageUri: Uri, onProgress: (FlashOperation) -> Unit): Flow<FastbootCommandResult>
}
