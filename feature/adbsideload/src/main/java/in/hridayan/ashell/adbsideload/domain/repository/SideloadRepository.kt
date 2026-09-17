package `in`.hridayan.ashell.adbsideload.domain.repository

import android.net.Uri
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import kotlinx.coroutines.flow.StateFlow

/**
 * Owns the connection to a device in sideload mode and the transfer running over it.
 *
 * The transfer deliberately lives here rather than on a screen's lifetime: a sideload runs for
 * minutes and must survive the user leaving the screen or backgrounding the app.
 */
interface SideloadRepository {
    val connectionState: StateFlow<SideloadState>

    /** Progress of the transfer in flight, or an idle operation when none is running. */
    val operation: StateFlow<SideloadOperation>

    fun searchDevices()

    /** Asks for USB access again after the user explicitly retries. */
    fun retryPermission()
    fun disconnect()
    fun sideload(uri: Uri)
    fun cancelSideload()
    fun resetOperation()
}
