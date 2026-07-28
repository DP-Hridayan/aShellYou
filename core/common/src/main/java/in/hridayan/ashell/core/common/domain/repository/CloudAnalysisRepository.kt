package `in`.hridayan.ashell.core.common.domain.repository

import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool

/**
 * Repository contract for command analysis via remote cloud LLM APIs.
 *
 * Throws [in.hridayan.ashell.core.common.domain.model.CloudNetworkException] on any failure.
 * Does not cache — caching is the caller's responsibility.
 */
interface CloudAnalysisRepository {
    suspend fun analyzeCommand(command: String, ragContext: String = "", fallbackModels: List<String>? = null): AnalysisResult

    suspend fun queryCommand(query: String, tools: List<AiTool>, fallbackModels: List<String>? = null): AnalysisResult
}
