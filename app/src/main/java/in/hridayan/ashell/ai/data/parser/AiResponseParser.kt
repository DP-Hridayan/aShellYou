package `in`.hridayan.ashell.ai.data.parser

import android.util.Log
import `in`.hridayan.ashell.ai.domain.model.AnalysisResult
import `in`.hridayan.ashell.ai.domain.model.AnalysisStatus

/**
 * Parses raw LLM plain text output into structured [AnalysisResult].
 */
object AiResponseParser {

    private const val TAG = "AiParser"

    /**
     * Parse raw LLM output into a structured [AnalysisResult].
     *
     * @param rawResponse The raw text output from the LLM
     * @return Parsed [AnalysisResult], or a GIBBERISH result if parsing fails
     */
    fun parse(rawResponse: String): AnalysisResult {
        Log.d(TAG, "parse() called, rawResponse length=${rawResponse.length}")

        val cleaned = rawResponse.trim()
        if (cleaned.isBlank()) {
            Log.w(TAG, "Raw response is blank/empty")
            return AnalysisResult.gibberish("AI model returned empty response")
        }

        // Check if the model indicates this is not a recognized command
        val isGibberish = cleaned.contains("Not a recognized command", ignoreCase = true) ||
                cleaned.contains("unrecognized command", ignoreCase = true) ||
                cleaned.contains("invalid command", ignoreCase = true)

        return if (isGibberish) {
            Log.d(TAG, "Command recognized as GIBBERISH/invalid")
            AnalysisResult.gibberish(cleaned)
        } else {
            Log.d(TAG, "Command recognized as VALID")
            AnalysisResult(
                status = AnalysisStatus.VALID,
                description = cleaned
            )
        }
    }
}
