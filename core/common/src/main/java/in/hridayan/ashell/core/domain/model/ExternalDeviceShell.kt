package `in`.hridayan.ashell.core.domain.model

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for executing shell commands on an external device
 * connected via OTG or Wi-Fi ADB.
 *
 * Implementations live in :feature:shell and are provided via Hilt @Named bindings.
 * :feature:logcat depends only on this interface through :core:common.
 */
interface ExternalDeviceShell {
    /** Streams lines of output from the given command on the external device. */
    fun execute(command: String): Flow<String>

    /** True when this shell's transport has an active connection. */
    val isConnected: Boolean
}
