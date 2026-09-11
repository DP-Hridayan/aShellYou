package `in`.hridayan.ashell.core.shizuku.data

import ashell.core.shizuku.IShellUserService
import `in`.hridayan.ashell.core.common.data.provider.DispatcherProvider
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuCommandRunner
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceError
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Starts commands through the UserService helper and falls back to the server's legacy
 * `newProcess` call when the helper itself cannot be started on the current server.
 */
@Singleton
class ShizukuCommandRunnerImpl @Inject constructor(
    private val connector: ShizukuUserServiceConnector,
    private val processFactory: RemoteProcessFactory,
    private val legacyStarter: LegacyProcessStarter,
    private val dispatchers: DispatcherProvider
) : ShizukuCommandRunner {

    override val state: StateFlow<ShizukuServiceState> = connector.state

    override suspend fun start(
        command: Array<String>,
        environment: Array<String>?,
        workingDirectory: String?
    ): Result<Process> = withContext(dispatchers.io) {
        connector.service().fold(
            onSuccess = { service -> startWithHelper(service, command, environment, workingDirectory) },
            onFailure = { error -> startWithoutHelper(error.asServiceError(), command, environment, workingDirectory) }
        )
    }

    override suspend fun warmUp(): Result<Unit> = withContext(dispatchers.io) {
        connector.service().map { }
    }

    private fun startWithHelper(
        service: IShellUserService,
        command: Array<String>,
        environment: Array<String>?,
        workingDirectory: String?
    ): Result<Process> = runCatching {
        val remote = service.newProcess(command, environment, workingDirectory)
            ?: throw ShizukuServiceError.ProcessStartFailed(null)
        processFactory.create(remote)
    }.recoverCatching { throw it.asServiceError() }

    private fun startWithoutHelper(
        error: ShizukuServiceError,
        command: Array<String>,
        environment: Array<String>?,
        workingDirectory: String?
    ): Result<Process> {
        if (!error.allowsLegacyFallback()) return Result.failure(error)
        return runCatching { processFactory.create(legacyStarter.start(command, environment, workingDirectory)) }
            .recoverCatching { throw it.asServiceError() }
    }

    private fun Throwable.asServiceError(): ShizukuServiceError =
        this as? ShizukuServiceError ?: ShizukuServiceError.ProcessStartFailed(this)

    private fun ShizukuServiceError.allowsLegacyFallback(): Boolean =
        this is ShizukuServiceError.BindTimeout ||
            this is ShizukuServiceError.BindFailed ||
            this is ShizukuServiceError.BinderDied
}
