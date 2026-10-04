package `in`.hridayan.ashell.core.shizuku.data

import ashell.core.shizuku.IShellUserService
import `in`.hridayan.ashell.core.common.data.provider.DispatcherProvider
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuClientToken
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuCommandRunner
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceError
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuServiceState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Starts commands through the UserService helper once it is bound. Until then, commands go through
 * the server's legacy `newProcess` call while the helper binds in the background, so no command
 * waits for the helper to start. Commands wait for the helper only when the legacy call fails.
 */
@Singleton
class ShizukuCommandRunnerImpl @Inject constructor(
    private val connector: ShizukuUserServiceConnector,
    private val processFactory: RemoteProcessFactory,
    private val legacyStarter: LegacyProcessStarter,
    private val clientToken: ShizukuClientToken,
    private val dispatchers: DispatcherProvider
) : ShizukuCommandRunner {

    override val state: StateFlow<ShizukuServiceState> = connector.state

    override suspend fun start(
        command: Array<String>,
        environment: Array<String>?,
        workingDirectory: String?
    ): Result<Process> = withContext(dispatchers.io) {
        val request = ProcessRequest(command, environment, workingDirectory)
        val readyService = connector.readyService()
        when {
            readyService != null -> startWithHelper(readyService, request)
            else -> connector.preflight().fold(
                onSuccess = { startWithoutWaiting(request) },
                onFailure = { Result.failure(it) }
            )
        }
    }

    override suspend fun warmUp(): Result<Unit> = withContext(dispatchers.io) {
        connector.service().map { }
    }

    private suspend fun startWithoutWaiting(request: ProcessRequest): Result<Process> {
        if (!connector.isLegacyStartUsable) return startAfterWarmUp(request)

        connector.warmUpInBackground()
        val bridged = startWithLegacy(request)
        if (bridged.isSuccess) return bridged

        connector.onLegacyStartFailed()
        return startAfterWarmUp(request)
    }

    private suspend fun startAfterWarmUp(request: ProcessRequest): Result<Process> =
        connector.service().fold(
            onSuccess = { service -> startWithHelper(service, request) },
            onFailure = { error -> Result.failure(error.asServiceError()) }
        )

    private fun startWithHelper(service: IShellUserService, request: ProcessRequest): Result<Process> =
        runCatching {
            val remote = service.newProcess(
                request.command,
                request.environment,
                request.workingDirectory,
                clientToken.binder
            ) ?: throw ShizukuServiceError.ProcessStartFailed(null)
            processFactory.create(remote)
        }.recoverCatching { throw it.asServiceError() }

    private fun startWithLegacy(request: ProcessRequest): Result<Process> = runCatching {
        val remote = legacyStarter.start(request.command, request.environment, request.workingDirectory)
        processFactory.create(remote)
    }

    private fun Throwable.asServiceError(): ShizukuServiceError =
        this as? ShizukuServiceError ?: ShizukuServiceError.ProcessStartFailed(this)

    private class ProcessRequest(
        val command: Array<String>,
        val environment: Array<String>?,
        val workingDirectory: String?
    )
}
