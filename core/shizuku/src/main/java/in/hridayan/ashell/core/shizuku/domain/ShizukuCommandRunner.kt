package `in`.hridayan.ashell.core.shizuku.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * Starts processes with Shizuku privileges through the app's UserService helper.
 * Replaces the removed `Shizuku.newProcess` API.
 */
interface ShizukuCommandRunner {
    val state: StateFlow<ShizukuServiceState>

    /**
     * Starts [command] with Shizuku privileges without waiting for the helper to bind.
     *
     * Uses the helper when it is already bound; otherwise starts the command through the server's
     * legacy `newProcess` call and binds the helper in the background for later commands.
     * Missing binder and denied permission are returned as failures, never bridged.
     *
     * @param environment full environment for the child, or null to inherit the helper's environment
     * @param workingDirectory working directory for the child, or null for the helper's directory
     */
    suspend fun start(
        command: Array<String>,
        environment: Array<String>?,
        workingDirectory: String?
    ): Result<Process>

    /**
     * Binds the helper and suspends until it is ready or has failed.
     */
    suspend fun warmUp(): Result<Unit>
}
