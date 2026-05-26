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
     * The prompt is kept ultra-compact (~100 tokens) to maximize KV cache reuse
     * and minimize first-call evaluation time on mobile CPUs.
     */
    fun buildSystemPrompt(): String = """
Analyze Android ADB/shell commands. Output ONLY JSON:
{"status":"VALID|PARTIAL|INVALID|GIBBERISH","description":"<max 10 words>","dangerLevel":"SAFE|LOW_RISK|MODERATE|DANGEROUS|CRITICAL","requiresRoot":false,"reversible":true,"examples":["<usage>"],"warnings":[],"useCases":[],"corrections":[{"suggestedCommand":"","confidence":"HIGH|MEDIUM|LOW","source":"AI"}],"feedback":""}
VALID=working,PARTIAL=incomplete/typo,INVALID=broken,GIBBERISH=not a command. Max 1 item per array. No markdown.
""".trimIndent()

    /**
     * Build the user prompt for a specific command.
     *
     * @param command The shell/ADB command to analyze
     * @return The formatted user prompt
     */
    fun buildUserPrompt(command: String): String = "Command: $command\nJSON:"
}
