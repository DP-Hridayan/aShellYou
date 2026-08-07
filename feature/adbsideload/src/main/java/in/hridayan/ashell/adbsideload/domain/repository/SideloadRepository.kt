package `in`.hridayan.ashell.adbsideload.domain.repository

import android.net.Uri
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import kotlinx.coroutines.flow.Flow

interface SideloadRepository {
    fun searchDevices()
    fun disconnect()
    fun unRegister()
    fun sideload(uri: Uri, onProgress: (SideloadOperation) -> Unit): Flow<SideloadOperation>
    fun cancelSideload()
}
