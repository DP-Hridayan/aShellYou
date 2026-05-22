package `in`.hridayan.ashell.ai.domain.usecase

import `in`.hridayan.ashell.ai.domain.model.CorrectionConfidence
import `in`.hridayan.ashell.ai.domain.model.CorrectionSource
import `in`.hridayan.ashell.ai.domain.model.CorrectionSuggestion
import `in`.hridayan.ashell.commandexamples.domain.repository.CommandRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.math.min

/**
 * Generates correction suggestions using a 4-stage pipeline:
 * 1. Exact database matches → HIGH confidence, DATABASE source
 * 2. Fuzzy database matches → MEDIUM confidence, DATABASE source
 * 3. Heuristic/token matching → MEDIUM confidence, HEURISTIC source
 * 4. AI-generated suggestions → LOW confidence, AI source
 */
class GenerateCorrectionsUseCase @Inject constructor(
    private val commandRepository: CommandRepository
) {
    /** Common command typos and their corrections */
    private val commonTypos = mapOf(
        "lss" to "ls",
        "cta" to "cat",
        "gerp" to "grep",
        "gre" to "grep",
        "ecoh" to "echo",
        "ehco" to "echo",
        "mkdr" to "mkdir",
        "mkdi" to "mkdir",
        "toch" to "touch",
        "touc" to "touch",
        "caht" to "cat",
        "whomai" to "whoami",
        "ifconig" to "ifconfig",
        "ifconfg" to "ifconfig",
        "chmdo" to "chmod",
        "chonw" to "chown",
        "getporp" to "getprop",
        "dumpyss" to "dumpsys",
        "dumspys" to "dumpsys",
        "logcta" to "logcat",
        "logact" to "logcat",
    )

    /**
     * Generate correction suggestions for the given command.
     *
     * @param command The command to generate corrections for
     * @param aiCorrections Corrections from the AI model (added at LOW confidence)
     * @return List of up to 5 suggestions, sorted by confidence (HIGH first)
     */
    suspend operator fun invoke(
        command: String,
        aiCorrections: List<CorrectionSuggestion> = emptyList()
    ): List<CorrectionSuggestion> {
        val results = mutableListOf<CorrectionSuggestion>()
        val baseCommand = command.trim().split("\\s+".toRegex()).firstOrNull() ?: return results
        val commandArgs = command.trim().removePrefix(baseCommand).trim()

        // 1. Exact database matches
        val exactMatches = try {
            commandRepository.searchCommands(baseCommand).firstOrNull() ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        exactMatches
            .filter { it.command.split("\\s+".toRegex()).first().equals(baseCommand, ignoreCase = true) }
            .take(2)
            .forEach { cmd ->
                results.add(
                    CorrectionSuggestion(
                        suggestedCommand = cmd.command,
                        confidence = CorrectionConfidence.HIGH,
                        source = CorrectionSource.DATABASE
                    )
                )
            }

        // 2. Fuzzy database matches (Levenshtein distance ≤ 2)
        if (results.isEmpty()) {
            exactMatches
                .filter {
                    val cmdBase = it.command.split("\\s+".toRegex()).first()
                    val distance = levenshteinDistance(baseCommand.lowercase(), cmdBase.lowercase())
                    distance in 1..2
                }
                .take(3)
                .forEach { cmd ->
                    val fullSuggestion = if (commandArgs.isNotEmpty()) {
                        "${cmd.command.split("\\s+".toRegex()).first()} $commandArgs"
                    } else {
                        cmd.command
                    }
                    results.add(
                        CorrectionSuggestion(
                            suggestedCommand = fullSuggestion,
                            confidence = CorrectionConfidence.MEDIUM,
                            source = CorrectionSource.DATABASE
                        )
                    )
                }
        }

        // 3. Heuristic/typo matching
        val typoFix = commonTypos[baseCommand.lowercase()]
        if (typoFix != null) {
            val fullSuggestion = if (commandArgs.isNotEmpty()) "$typoFix $commandArgs" else typoFix
            // Only add if not already suggested
            if (results.none { it.suggestedCommand.equals(fullSuggestion, ignoreCase = true) }) {
                results.add(
                    CorrectionSuggestion(
                        suggestedCommand = fullSuggestion,
                        confidence = CorrectionConfidence.MEDIUM,
                        source = CorrectionSource.HEURISTIC
                    )
                )
            }
        }

        // 4. AI-generated suggestions (with LOW confidence, AI source override)
        aiCorrections.forEach { correction ->
            if (results.none { it.suggestedCommand.equals(correction.suggestedCommand, ignoreCase = true) }) {
                results.add(
                    correction.copy(
                        confidence = CorrectionConfidence.LOW,
                        source = CorrectionSource.AI
                    )
                )
            }
        }

        // Sort by confidence (HIGH first) and limit to 5
        return results
            .sortedBy { it.confidence.ordinal }
            .take(5)
    }

    /**
     * Compute the Levenshtein edit distance between two strings.
     */
    private fun levenshteinDistance(a: String, b: String): Int {
        val lenA = a.length
        val lenB = b.length
        val dp = Array(lenA + 1) { IntArray(lenB + 1) }

        for (i in 0..lenA) dp[i][0] = i
        for (j in 0..lenB) dp[0][j] = j

        for (i in 1..lenA) {
            for (j in 1..lenB) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[lenA][lenB]
    }
}
