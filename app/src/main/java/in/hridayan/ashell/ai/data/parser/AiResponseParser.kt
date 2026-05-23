package `in`.hridayan.ashell.ai.data.parser

import android.util.Log
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
 * - Truncated JSON (closes missing braces/brackets/strings)
 * - Invalid enum values
 * - Plain-text key-value responses from small models
 */
object AiResponseParser {

    private const val TAG = "AiParser"

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
        Log.d(TAG, "parse() called, rawResponse length=${rawResponse.length}")

        if (rawResponse.isBlank()) {
            Log.w(TAG, "Raw response is blank/empty")
            return AnalysisResult.gibberish("AI model returned empty response")
        }

        return try {
            val jsonString = extractJson(rawResponse)
            if (jsonString.isNullOrBlank()) {
                Log.e(TAG, "Could not extract JSON from AI output")
                Log.e(TAG, "Raw output was: $rawResponse")

                // Last resort: try to parse as plain-text key-value pairs
                val plainTextResult = parseFromPlainText(rawResponse)
                if (plainTextResult != null) {
                    Log.d(TAG, "Parsed successfully from plain-text fallback")
                    return plainTextResult
                }

                return AnalysisResult.gibberish(
                    "Could not find valid JSON in AI response. Raw output: ${rawResponse.take(200)}"
                )
            }

            Log.d(TAG, "Extracted JSON (${jsonString.length} chars): ${jsonString.take(500)}")

            val raw = json.decodeFromString<RawAnalysisResponse>(jsonString)
            Log.d(TAG, "Deserialized successfully: status=${raw.status}, dangerLevel=${raw.dangerLevel}")

            val result = mapToAnalysisResult(raw)
            Log.d(TAG, "Mapped to AnalysisResult: status=${result.status}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse/decode failed", e)
            Log.e(TAG, "Raw AI output was: $rawResponse")

            // Last resort: try to parse as plain-text key-value pairs
            val plainTextResult = parseFromPlainText(rawResponse)
            if (plainTextResult != null) {
                Log.d(TAG, "Parsed successfully from plain-text fallback after JSON failure")
                return plainTextResult
            }

            AnalysisResult.gibberish(
                "Failed to parse AI response: ${e.message}\n\nRaw AI output: ${rawResponse.take(300)}"
            )
        }
    }

    /**
     * Extract a JSON object from raw LLM output.
     * Handles markdown code fences, extra text, and truncated JSON.
     *
     * Uses pure string operations (indexOf) for code fence detection
     * to avoid PatternSyntaxException on Android's ICU regex engine.
     */
    private fun extractJson(raw: String): String? {
        val trimmed = raw.trim()
        Log.d(TAG, "extractJson(): input starts with '${trimmed.take(30)}...'")

        // Try direct parse first
        if (trimmed.startsWith("{")) {
            Log.d(TAG, "Input starts with '{', extracting JSON object directly")
            val extracted = extractJsonObject(trimmed)
            if (isValidJson(extracted)) {
                return extracted
            }
            Log.d(TAG, "Direct extraction produced invalid JSON, trying repair...")
            val repaired = repairTruncatedJson(extracted)
            if (isValidJson(repaired)) {
                Log.d(TAG, "Repair successful")
                return repaired
            }
        }

        // Look for JSON in code fences using pure string operations:
        // Handles ```json ... ``` or ``` ... ```
        val fenceContent = extractFromCodeFence(trimmed)
        if (fenceContent != null) {
            Log.d(TAG, "Found JSON inside code fence")
            val content = fenceContent.trim()
            if (isValidJson(content)) return content
            val repaired = repairTruncatedJson(content)
            if (isValidJson(repaired)) return repaired
        }

        // Look for code fence that's missing the closing ``` (truncated output)
        val unclosedContent = extractFromUnclosedCodeFence(trimmed)
        if (unclosedContent != null) {
            Log.d(TAG, "Found JSON in unclosed code fence (truncated output)")
            val content = unclosedContent.trim()
            val extracted = extractJsonObject(content)
            val repaired = repairTruncatedJson(extracted)
            if (isValidJson(repaired)) return repaired
        }

        // Look for any JSON object in the text
        val jsonStart = trimmed.indexOf('{')
        if (jsonStart >= 0) {
            Log.d(TAG, "Found '{' at position $jsonStart, extracting JSON object")
            val extracted = extractJsonObject(trimmed.substring(jsonStart))
            if (isValidJson(extracted)) return extracted
            val repaired = repairTruncatedJson(extracted)
            if (isValidJson(repaired)) {
                Log.d(TAG, "Repaired truncated JSON successfully")
                return repaired
            }
        }

        Log.e(TAG, "No JSON object found in output")
        return null
    }

    /**
     * Extract content from a complete code fence (``` ... ```) using indexOf.
     * Returns the content between the opening and closing fence markers, or null.
     */
    private fun extractFromCodeFence(text: String): String? {
        val openIndex = text.indexOf("```")
        if (openIndex < 0) return null

        // Skip past the opening ``` and any optional language tag (e.g. "json")
        val afterOpen = openIndex + 3
        if (afterOpen >= text.length) return null

        // Find the end of the opening fence line (skip "json" tag and whitespace)
        var contentStart = afterOpen
        // Skip optional language identifier like "json"
        while (contentStart < text.length && text[contentStart].isLetter()) {
            contentStart++
        }
        // Skip whitespace/newlines after language tag
        while (contentStart < text.length && (text[contentStart] == ' ' || text[contentStart] == '\n' || text[contentStart] == '\r' || text[contentStart] == '\t')) {
            contentStart++
        }

        // Find the closing ```
        val closeIndex = text.indexOf("```", contentStart)
        if (closeIndex < 0) return null

        val content = text.substring(contentStart, closeIndex).trim()
        // Only return if it looks like it contains JSON
        if (content.contains("{")) {
            return content
        }
        return null
    }

    /**
     * Extract content from an unclosed code fence (``` without closing ```) using indexOf.
     * Returns the content after the opening fence marker, or null.
     */
    private fun extractFromUnclosedCodeFence(text: String): String? {
        val openIndex = text.indexOf("```")
        if (openIndex < 0) return null

        val afterOpen = openIndex + 3
        if (afterOpen >= text.length) return null

        // Skip optional language identifier like "json"
        var contentStart = afterOpen
        while (contentStart < text.length && text[contentStart].isLetter()) {
            contentStart++
        }
        // Skip whitespace/newlines after language tag
        while (contentStart < text.length && (text[contentStart] == ' ' || text[contentStart] == '\n' || text[contentStart] == '\r' || text[contentStart] == '\t')) {
            contentStart++
        }

        // Check if there's a closing ``` — if so, this isn't an unclosed fence
        val closeIndex = text.indexOf("```", contentStart)
        if (closeIndex >= 0) return null

        val content = text.substring(contentStart).trim()
        // Only return if it looks like it contains JSON
        if (content.contains("{")) {
            return content
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
        // If we got here, braces are unbalanced (truncated JSON)
        Log.w(TAG, "extractJsonObject: unbalanced braces (depth=$depth), returning raw text for repair")
        return text
    }

    /**
     * Attempt to repair truncated JSON by closing open strings, arrays, and objects.
     *
     * Small models often produce output that gets cut off mid-JSON when they hit the
     * max token limit. This method tries to make the JSON parseable by closing any
     * open constructs.
     */
    private fun repairTruncatedJson(json: String): String {
        Log.d(TAG, "repairTruncatedJson(): attempting repair on ${json.length} chars")

        var repaired = json.trimEnd()

        // Remove trailing comma if present
        repaired = repaired.trimEnd(',', ' ', '\n', '\r', '\t')

        // Track state
        var inString = false
        var escape = false
        val stack = mutableListOf<Char>() // tracks open { and [

        for (char in repaired) {
            if (escape) {
                escape = false
                continue
            }
            when {
                char == '\\' && inString -> escape = true
                char == '"' -> inString = !inString
                !inString && char == '{' -> stack.add('{')
                !inString && char == '[' -> stack.add('[')
                !inString && char == '}' -> { if (stack.isNotEmpty() && stack.last() == '{') stack.removeAt(stack.lastIndex) }
                !inString && char == ']' -> { if (stack.isNotEmpty() && stack.last() == '[') stack.removeAt(stack.lastIndex) }
            }
        }

        // Close open string
        if (inString) {
            repaired += "\""
        }

        // Remove trailing comma after closing the string
        repaired = repaired.trimEnd().trimEnd(',')

        // Close open arrays and objects in reverse order
        for (opener in stack.reversed()) {
            repaired += if (opener == '[') "]" else "}"
        }

        Log.d(TAG, "repairTruncatedJson(): repaired JSON (last 100 chars): ...${repaired.takeLast(100)}")
        return repaired
    }

    /**
     * Quick check if a string is valid JSON that can be deserialized.
     */
    private fun isValidJson(text: String): Boolean {
        return try {
            json.decodeFromString<RawAnalysisResponse>(text)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Parse plain-text key-value responses from small models that don't output JSON.
     *
     * Handles output like:
     * ```
     * Status: PARTIAL
     * Description: This command disables...
     * Required arguments: None
     * Corrections: None
     * Feedback: The command is missing...
     * ```
     *
     * @param raw The raw text to parse
     * @return An [AnalysisResult] if key-value pairs were found, or null
     */
    private fun parseFromPlainText(raw: String): AnalysisResult? {
        Log.d(TAG, "parseFromPlainText(): attempting plain-text parse")

        val lines = raw.trim().lines()
        val kvMap = mutableMapOf<String, String>()

        for (line in lines) {
            val colonIndex = line.indexOf(':')
            if (colonIndex > 0) {
                val key = line.substring(0, colonIndex).trim().lowercase()
                val value = line.substring(colonIndex + 1).trim()
                if (value.isNotBlank()) {
                    kvMap[key] = value
                }
            }
        }

        // Need at least a status or description to consider it valid
        if (kvMap.isEmpty() || (!kvMap.containsKey("status") && !kvMap.containsKey("description"))) {
            Log.d(TAG, "parseFromPlainText(): no usable key-value pairs found")
            return null
        }

        Log.d(TAG, "parseFromPlainText(): found ${kvMap.size} key-value pairs: ${kvMap.keys}")

        val status = kvMap["status"]?.let { statusStr ->
            parseEnum(statusStr, AnalysisStatus.entries, AnalysisStatus.INVALID)
        } ?: AnalysisStatus.INVALID

        val dangerLevel = (kvMap["danger level"] ?: kvMap["dangerlevel"] ?: kvMap["danger"])?.let { dangerStr ->
            parseEnum(dangerStr, DangerLevel.entries, DangerLevel.SAFE)
        } ?: DangerLevel.SAFE

        val description = kvMap["description"] ?: ""

        val feedback = kvMap["feedback"] ?: ""

        val requiresRoot = kvMap["requires root"]?.let {
            it.equals("true", ignoreCase = true) || it.equals("yes", ignoreCase = true)
        } ?: kvMap["requiresroot"]?.let {
            it.equals("true", ignoreCase = true) || it.equals("yes", ignoreCase = true)
        } ?: false

        val reversible = kvMap["reversible"]?.let {
            it.equals("true", ignoreCase = true) || it.equals("yes", ignoreCase = true)
        } ?: true

        val corrections = mutableListOf<CorrectionSuggestion>()
        val correctionsValue = kvMap["corrections"] ?: kvMap["correction"] ?: kvMap["suggested command"]
        if (correctionsValue != null &&
            !correctionsValue.equals("none", ignoreCase = true) &&
            !correctionsValue.equals("n/a", ignoreCase = true) &&
            correctionsValue.isNotBlank()
        ) {
            corrections.add(
                CorrectionSuggestion(
                    suggestedCommand = correctionsValue,
                    confidence = CorrectionConfidence.LOW,
                    source = CorrectionSource.AI
                )
            )
        }

        Log.d(TAG, "parseFromPlainText(): built AnalysisResult with status=$status, dangerLevel=$dangerLevel")

        return AnalysisResult(
            status = status,
            description = description,
            dangerLevel = dangerLevel,
            requiresRoot = requiresRoot,
            reversible = reversible,
            corrections = corrections,
            feedback = feedback
        )
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
