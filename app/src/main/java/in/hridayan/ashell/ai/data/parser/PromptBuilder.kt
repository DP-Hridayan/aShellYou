package `in`.hridayan.ashell.ai.data.parser

/**
 * Builds system and user prompts for AI command analysis.
 *
 * The prompts are designed to work with small instruction-tuned models
 * (0.5B–1.5B parameters) and produce structured JSON responses.
 */
object PromptBuilder {

    /**
     * Build the system prompt that instructs the model to analyze commands.
     *
     * The prompt is kept compact (~300 tokens) to fit within the context window
     * of small models while providing clear instructions for structured output.
     */
    fun buildSystemPrompt(): String = """
You analyze Android shell/ADB commands. Reply ONLY with JSON using this schema:
{"status":"VALID|PARTIAL|INVALID|GIBBERISH","description":"","dangerLevel":"SAFE|LOW_RISK|MODERATE|DANGEROUS|CRITICAL","requiresRoot":false,"reversible":true,"examples":[],"warnings":[],"useCases":[],"corrections":[{"suggestedCommand":"","confidence":"HIGH|MEDIUM|LOW","source":"AI"}],"feedback":""}

Rules:
- VALID: working command.
- PARTIAL: recognized command but incomplete, missing arguments, or has a typo.
- INVALID: broken syntax.
- GIBBERISH: not a command.
- dangerLevel: SAFE (read-only), LOW_RISK (minor), MODERATE (system), DANGEROUS (data loss), CRITICAL (brick).
- Output ONLY JSON (no markdown, max 1 item per array, description <= 10 words).
- examples: MUST be a complete, valid, executable usage example. If GIBBERISH or INVALID, examples MUST be empty [].
- corrections: If command is PARTIAL, MUST suggest correct syntax in 'suggestedCommand' using '<...>' placeholders.

Examples:
Command: pm list packages
JSON: {"status":"VALID","description":"Lists installed packages","dangerLevel":"SAFE","requiresRoot":false,"reversible":true,"examples":["pm list packages"],"warnings":[],"useCases":["View installed apps"],"corrections":[],"feedback":""}

Command: pm enable
JSON: {"status":"PARTIAL","description":"Enables an app package","dangerLevel":"MODERATE","requiresRoot":false,"reversible":true,"examples":["pm enable com.example.app"],"warnings":["Disabling critical system apps can cause boot loops"],"useCases":["Re-enable a disabled application"],"corrections":[{"suggestedCommand":"pm enable <package_name>","confidence":"HIGH","source":"AI"}],"feedback":"Specify the package name to enable it"}

Command: shsidhasdg
JSON: {"status":"GIBBERISH","description":"Unrecognized command","dangerLevel":"SAFE","requiresRoot":false,"reversible":true,"examples":[],"warnings":[],"useCases":[],"corrections":[],"feedback":"This does not appear to be a valid command"}
""".trimIndent()

    /**
     * Build the user prompt for a specific command.
     *
     * @param command The shell/ADB command to analyze
     * @return The formatted user prompt
     */
    fun buildUserPrompt(command: String): String = "Command: $command\nJSON:"
}
