package `in`.hridayan.ashell.adbsideload.domain.repository

import android.net.Uri
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadPackageInfo
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SideloadRepository {
    val connectionState: StateFlow<SideloadState>
    fun searchDevices()

    /** Asks for USB access again after the user explicitly retries. */
    fun retryPermission()
    fun disconnect()
    suspend fun inspectPackage(uri: Uri): SideloadPackageInfo?
    fun sideload(uri: Uri): Flow<SideloadOperation>
    fun cancelSideload()
}
