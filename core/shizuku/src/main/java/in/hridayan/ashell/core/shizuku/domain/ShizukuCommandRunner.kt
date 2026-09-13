package `in`.hridayan.ashell.core.shizuku.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * Starts processes with Shizuku privileges through the app's UserService helper.
 * Replaces the removed `Shizuku.newProcess` API.
 */
interface ShizukuCommandRunner {
    val state: StateFlow<ShizukuServiceState>

    /**
     * Starts [command] in the privileged helper.
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
     * Binds the helper ahead of time so the first command does not pay the start-up cost.
     */
    suspend fun warmUp(): Result<Unit>

    /**
     * Tries to start the command using the helper if already bound, otherwise immediately falls
     * back to legacy process creation to avoid background warmup timeouts.
     */
    suspend fun startFast(
        command: Array<String>,
        environment: Array<String>?,
        workingDirectory: String?
    ): Result<Process>
}
