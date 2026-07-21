package `in`.hridayan.ashell.ai.domain.usecase

import `in`.hridayan.ashell.ai.domain.model.DangerLevel

/**
 * Heuristic-based danger level detection for shell/ADB commands.
 *
 * This runs before AI analysis to provide instant danger level assessment
 * using pattern matching. The result is merged with the AI's assessment,
 * always taking the higher danger level.
 */
class DetectDangerLevelUseCase {

    private data class DangerPattern(
        val pattern: Regex,
        val level: DangerLevel
    )

    private val patterns: List<DangerPattern> = listOf(
        // ── CRITICAL ──
        DangerPattern(Regex("rm\\s+-[^\\s]*r[^\\s]*f.*\\s+/\\s*$|rm\\s+-[^\\s]*f[^\\s]*r.*\\s+/\\s*$"), DangerLevel.CRITICAL),
        DangerPattern(Regex("rm\\s+-rf\\s+/(?:\\s|$)"), DangerLevel.CRITICAL),
        DangerPattern(Regex("dd\\s+if="), DangerLevel.CRITICAL),
        DangerPattern(Regex("mkfs\\."), DangerLevel.CRITICAL),
        DangerPattern(Regex(":\\(\\)\\s*\\{.*\\|.*&\\s*\\}"), DangerLevel.CRITICAL), // Fork bomb
        DangerPattern(Regex("fastboot\\s+erase"), DangerLevel.CRITICAL),
        DangerPattern(Regex("fastboot\\s+flash"), DangerLevel.CRITICAL),
        DangerPattern(Regex("recovery\\s+--wipe"), DangerLevel.CRITICAL),

        // ── DANGEROUS ──
        DangerPattern(Regex("\\brm\\s+-r"), DangerLevel.DANGEROUS),
        DangerPattern(Regex("\\brm\\s+"), DangerLevel.DANGEROUS),
        DangerPattern(Regex("\\bwipe\\b"), DangerLevel.DANGEROUS),
        DangerPattern(Regex("factory[_-]?reset"), DangerLevel.DANGEROUS),
        DangerPattern(Regex("\\breboot\\s+bootloader"), DangerLevel.DANGEROUS),
        DangerPattern(Regex("pm\\s+uninstall\\s+-k\\s+--user\\s+0"), DangerLevel.DANGEROUS),
        DangerPattern(Regex("\\bformat\\s"), DangerLevel.DANGEROUS),
        DangerPattern(Regex("\\bflash\\b"), DangerLevel.DANGEROUS),

        // ── MODERATE ──
        DangerPattern(Regex("\\bchmod\\b"), DangerLevel.MODERATE),
        DangerPattern(Regex("\\bchown\\b"), DangerLevel.MODERATE),
        DangerPattern(Regex("\\bmount\\b"), DangerLevel.MODERATE),
        DangerPattern(Regex("pm\\s+uninstall"), DangerLevel.MODERATE),
        DangerPattern(Regex("pm\\s+disable(?!-)"), DangerLevel.MODERATE),
        DangerPattern(Regex("settings\\s+put"), DangerLevel.MODERATE),
        DangerPattern(Regex("\\bsetprop\\b"), DangerLevel.MODERATE),
        DangerPattern(Regex("\\bsu\\s"), DangerLevel.MODERATE),
        DangerPattern(Regex("\\bkill\\b"), DangerLevel.MODERATE),
        DangerPattern(Regex("\\bkillall\\b"), DangerLevel.MODERATE),

        // ── LOW_RISK ──
        DangerPattern(Regex("pm\\s+clear"), DangerLevel.LOW_RISK),
        DangerPattern(Regex("am\\s+force-stop"), DangerLevel.LOW_RISK),
        DangerPattern(Regex("pm\\s+disable-user"), DangerLevel.LOW_RISK),
        DangerPattern(Regex("\\binput\\b"), DangerLevel.LOW_RISK),
        DangerPattern(Regex("\\bsvc\\b"), DangerLevel.LOW_RISK),
        DangerPattern(Regex("\\breboot\\b"), DangerLevel.LOW_RISK),
        DangerPattern(Regex("settings\\s+get"), DangerLevel.LOW_RISK),
    )

    /**
     * Detect the danger level of a command using heuristic pattern matching.
     *
     * @param command The shell/ADB command to analyze
     * @return The detected danger level (defaults to SAFE if no patterns match)
     */
    operator fun invoke(command: String): DangerLevel {
        val normalizedCommand = command.trim().lowercase()

        // Find the highest matching danger level
        var maxDanger = DangerLevel.SAFE
        for (dangerPattern in patterns) {
            if (dangerPattern.pattern.containsMatchIn(normalizedCommand)) {
                if (dangerPattern.level.ordinal > maxDanger.ordinal) {
                    maxDanger = dangerPattern.level
                }
            }
        }
        return maxDanger
    }
}
