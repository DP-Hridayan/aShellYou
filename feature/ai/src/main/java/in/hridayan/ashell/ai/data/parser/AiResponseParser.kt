package `in`.hridayan.ashell.ai.data.parser

import android.util.Log
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import kotlinx.serialization.json.Json

/**
 * Parses raw LLM plain text output into structured [AnalysisResult].
 */
object AiResponseParser {

    private const val TAG = "AiResponseParser"

    private val json = Json {
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

        if (rawResponse.isBlank()) {
            Log.w(TAG, "Raw response is blank/empty")
            return AnalysisResult.gibberish("AI model returned empty response")
        }

        val cleaned = extractJsonObject(rawResponse)

        return try {
            json.decodeFromString<AnalysisResult>(cleaned)
        } catch (e: Exception) {
            Log.w(TAG, "Strict JSON parse failed, attempting fallback repair: ${e.message}")
            repairAndParse(cleaned)
        }
    }

    private fun extractJsonObject(rawText: String): String {
        var cleaned = rawText.trim()

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substringAfter("```json").substringBeforeLast("```").trim()
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substringAfter("```").substringBeforeLast("```").trim()
        }

        val startIndex = cleaned.indexOf('{')
        val endIndex = cleaned.lastIndexOf('}')

        if (startIndex != -1 && endIndex != -1 && endIndex >= startIndex) {
            return cleaned.substring(startIndex, endIndex + 1)
        }

        return cleaned
    }

    private fun repairAndParse(cleanedJson: String): AnalysisResult {
        val repaired = if (cleanedJson.endsWith("}")) {
            cleanedJson
        } else {
            if (cleanedJson.endsWith("\"")) "$cleanedJson\n}" else "$cleanedJson\"\n}"
        }

        return try {
            json.decodeFromString<AnalysisResult>(repaired)
        } catch (e: Exception) {
            Log.e(TAG, "Fallback JSON repair failed as well.", e)
            AnalysisResult.gibberish("AI response was incomplete or malformed. Try increasing context size.")
        }
    }
}
