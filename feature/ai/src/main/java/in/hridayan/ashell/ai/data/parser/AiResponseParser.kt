package `in`.hridayan.ashell.ai.data.parser

import android.util.Log
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisStatus

/**
 * Parses raw LLM plain text output into structured [AnalysisResult].
 */
object AiResponseParser {

    private const val TAG = "AiParser"

    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parse raw LLM output into a structured [AnalysisResult].
     *
     * @param rawResponse The raw text output from the LLM
     * @return Parsed [AnalysisResult], or a GIBBERISH result if parsing fails
     */
    fun parse(rawResponse: String): AnalysisResult {
        Log.d(TAG, "parse() called, rawResponse length=${rawResponse.length}")

        var cleaned = rawResponse.trim()
        if (cleaned.isBlank()) {
            Log.w(TAG, "Raw response is blank/empty")
            return AnalysisResult.gibberish("AI model returned empty response")
        }

        // Clean up markdown block if model ignored the prompt instruction
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substringAfter("```json").substringBeforeLast("```").trim()
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substringAfter("```").substringBeforeLast("```").trim()
        }

        // Deepseek and other models might output preamble text. Extract just the JSON object.
        val startIndex = cleaned.indexOf('{')
        val endIndex = cleaned.lastIndexOf('}')
        if (startIndex != -1 && endIndex != -1 && endIndex >= startIndex) {
            cleaned = cleaned.substring(startIndex, endIndex + 1)
        }

        try {
            return json.decodeFromString<AnalysisResult>(cleaned)
        } catch (e: Exception) {
            Log.w(TAG, "Strict JSON parse failed, attempting fallback repair: ${e.message}")
            
            // Fallback: Model probably hit max_tokens and truncated the JSON
            // We append missing braces/quotes to try and salvage it.
            var repaired = cleaned
            if (!repaired.endsWith("}")) {
                if (repaired.endsWith("\"")) {
                    repaired += "\n}"
                } else {
                    repaired += "\"\n}"
                }
            }
            
            try {
                return json.decodeFromString<AnalysisResult>(repaired)
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback JSON repair failed as well.", e2)
                return AnalysisResult.gibberish("AI response was incomplete or malformed. Try increasing context size.")
            }
        }
    }
}
