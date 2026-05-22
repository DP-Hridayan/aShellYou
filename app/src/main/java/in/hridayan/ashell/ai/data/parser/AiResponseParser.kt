package `in`.hridayan.ashell.ai.data.parser

import `in`.hridayan.ashell.ai.domain.model.AnalysisResult
import `in`.hridayan.ashell.ai.domain.model.AnalysisStatus
import `in`.hridayan.ashell.ai.domain.model.CorrectionConfidence
import `in`.hridayan.ashell.ai.domain.model.CorrectionSource
import `in`.hridayan.ashell.ai.domain.model.CorrectionSuggestion
import `in`.hridayan.ashell.ai.domain.model.DangerLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Parses raw LLM output into structured [AnalysisResult].
 *
 * Handles common LLM output issues:
 * - Markdown code fences around JSON
 * - Extra text before/after JSON
 * - Malformed JSON with missing/extra fields
 * - Invalid enum values
 */
object AiResponseParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Internal raw JSON model matching the LLM output schema.
     */
    @Serializable
    private data class RawAnalysisResponse(
        val status: String = "INVALID",
        val description: String = "",
        val dangerLevel: String = "SAFE",
        val requiresRoot: Boolean = false,
        val reversible: Boolean = true,
        val examples: List<String> = emptyList(),
        val warnings: List<String> = emptyList(),
        val useCases: List<String> = emptyList(),
        val corrections: List<RawCorrection> = emptyList(),
        val feedback: String = ""
    )

    @Serializable
    private data class RawCorrection(
        val suggestedCommand: String = "",
        val confidence: String = "LOW",
        val source: String = "AI"
    )

    /**
     * Parse raw LLM output into a structured [AnalysisResult].
     *
     * @param rawResponse The raw text output from the LLM
     * @return Parsed [AnalysisResult], or a GIBBERISH result if parsing fails
     */
    fun parse(rawResponse: String): AnalysisResult {
        return try {
            val jsonString = extractJson(rawResponse)
            if (jsonString.isNullOrBlank()) {
                return AnalysisResult.gibberish("Could not parse AI response")
            }

            val raw = json.decodeFromString<RawAnalysisResponse>(jsonString)
            mapToAnalysisResult(raw)
        } catch (e: Exception) {
            AnalysisResult.gibberish("Failed to parse AI response: ${e.message}")
        }
    }

    /**
     * Extract a JSON object from raw LLM output.
     * Handles markdown code fences and extra text.
     */
    private fun extractJson(raw: String): String? {
        val trimmed = raw.trim()

        // Try direct parse first
        if (trimmed.startsWith("{")) {
            return extractJsonObject(trimmed)
        }

        // Look for JSON in code fences: ```json ... ``` or ``` ... ```
        val codeFencePattern = Regex("```(?:json)?\\s*\\n?(\\{.*?})\\s*\\n?```", RegexOption.DOT_MATCHES_ALL)
        codeFencePattern.find(trimmed)?.let {
            return it.groupValues[1].trim()
        }

        // Look for any JSON object in the text
        val jsonPattern = Regex("\\{[^{}]*(?:\\{[^{}]*}[^{}]*)*}", RegexOption.DOT_MATCHES_ALL)
        jsonPattern.find(trimmed)?.let {
            return it.value
        }

        return null
    }

    /**
     * Extract a complete JSON object handling nested braces.
     */
    private fun extractJsonObject(text: String): String {
        var depth = 0
        var inString = false
        var escape = false

        for ((index, char) in text.withIndex()) {
            if (escape) {
                escape = false
                continue
            }
            when (char) {
                '\\' -> if (inString) escape = true
                '"' -> inString = !inString
                '{' -> if (!inString) depth++
                '}' -> if (!inString) {
                    depth--
                    if (depth == 0) return text.substring(0, index + 1)
                }
            }
        }
        return text // Return as-is if no matching brace found
    }

    /**
     * Map raw parsed response to domain model with enum validation.
     */
    private fun mapToAnalysisResult(raw: RawAnalysisResponse): AnalysisResult {
        return AnalysisResult(
            status = parseEnum(raw.status, AnalysisStatus.entries, AnalysisStatus.INVALID),
            description = raw.description,
            dangerLevel = parseEnum(raw.dangerLevel, DangerLevel.entries, DangerLevel.SAFE),
            requiresRoot = raw.requiresRoot,
            reversible = raw.reversible,
            examples = raw.examples,
            warnings = raw.warnings,
            useCases = raw.useCases,
            corrections = raw.corrections.mapNotNull { mapCorrection(it) },
            feedback = raw.feedback
        )
    }

    private fun mapCorrection(raw: RawCorrection): CorrectionSuggestion? {
        if (raw.suggestedCommand.isBlank()) return null
        return CorrectionSuggestion(
            suggestedCommand = raw.suggestedCommand,
            confidence = parseEnum(raw.confidence, CorrectionConfidence.entries, CorrectionConfidence.LOW),
            source = CorrectionSource.AI // AI-generated corrections always have AI source
        )
    }

    /**
     * Parse a string to an enum value with fallback.
     */
    private inline fun <reified T : Enum<T>> parseEnum(
        value: String,
        entries: List<T>,
        fallback: T
    ): T {
        return entries.find {
            it.name.equals(value.trim().replace("-", "_"), ignoreCase = true)
        } ?: fallback
    }
}
