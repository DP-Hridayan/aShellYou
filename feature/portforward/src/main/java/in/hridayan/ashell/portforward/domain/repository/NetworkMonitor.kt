package `in`.hridayan.ashell.portforward.domain.repository

import kotlinx.coroutines.flow.StateFlow

/** Whether the phone has any usable network. */
interface NetworkMonitor {
    val isConnected: StateFlow<Boolean>
}
