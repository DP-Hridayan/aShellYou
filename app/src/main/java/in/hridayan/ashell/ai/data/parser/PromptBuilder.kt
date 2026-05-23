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
You analyze Android shell and ADB commands.

Reply ONLY with valid JSON using this exact schema:
{"status":"VALID|PARTIAL|INVALID|GIBBERISH","description":"","dangerLevel":"SAFE|LOW_RISK|MODERATE|DANGEROUS|CRITICAL","requiresRoot":false,"reversible":true,"examples":[],"warnings":[],"useCases":[],"corrections":[{"suggestedCommand":"","confidence":"HIGH|MEDIUM|LOW","source":"AI"}],"feedback":""}

Rules:
- VALID = complete working command
- PARTIAL = recognized but incomplete/typo
- INVALID = broken syntax
- GIBBERISH = not a shell/ADB command
- If base command is recognized, prefer PARTIAL over GIBBERISH

Output rules:
- Output ONLY JSON
- Start with {
- End with }
- No markdown
- No ** formatting
- No explanations outside JSON
- description <= 10 words
- max 1 example
- max 1 warning
- max 1 use case
- max 1 correction
- feedback short

dangerLevel:
- SAFE = read-only
- LOW_RISK = minor changes
- MODERATE = system changes
- DANGEROUS = data loss possible
- CRITICAL = can brick device

Example:
{"status":"VALID","description":"Lists installed packages","dangerLevel":"SAFE","requiresRoot":false,"reversible":true,"examples":["pm list packages"],"warnings":[],"useCases":["View installed apps"],"corrections":[],"feedback":""}
""".trimIndent()

    /**
     * Build the user prompt for a specific command.
     *
     * @param command The shell/ADB command to analyze
     * @return The formatted user prompt
     */
    fun buildUserPrompt(command: String): String = "Command: $command\nJSON:"
}
