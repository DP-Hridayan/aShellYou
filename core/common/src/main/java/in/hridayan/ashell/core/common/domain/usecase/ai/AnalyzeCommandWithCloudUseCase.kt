package `in`.hridayan.ashell.core.common.domain.usecase.ai

import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.repository.CloudAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.repository.CommandRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Analyzes a shell/ADB command using the configured cloud LLM provider.
 *
 * Performs a RAG lookup against the local command database to enrich the prompt
 * before dispatching to the remote API. Returns a [Result] so callers can handle
 * [CloudNetworkException] subtypes without a try/catch at every call site.
 */
class AnalyzeCommandWithCloudUseCase @Inject constructor(
    private val cloudRepository: CloudAnalysisRepository,
    private val commandRepository: CommandRepository,
) {
    suspend operator fun invoke(command: String): Result<AnalysisResult> {
        val trimmed = command.trim()
        if (trimmed.isBlank()) return Result.success(AnalysisResult.error("Command is empty"))

        val safeCommand = trimmed.take(MAX_COMMAND_LENGTH)
        val rootCommand = safeCommand.split(" ").firstOrNull() ?: safeCommand
        val ragContext = try {
            commandRepository.searchCommands(rootCommand).firstOrNull()
                ?.firstOrNull()?.description ?: ""
        } catch (_: Exception) {
            ""
        }

        return try {
            Result.success(cloudRepository.analyzeCommand(safeCommand, ragContext))
        } catch (e: CloudNetworkException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(CloudNetworkException.NetworkError(e))
        }
    }

    companion object {
        const val MAX_COMMAND_LENGTH = 4000
    }
}

