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
You are a shell/ADB command analyzer. Analyze commands and respond ONLY with valid JSON.

JSON schema:
{"status":"VALID|PARTIAL|INVALID|GIBBERISH","description":"what the command does","dangerLevel":"SAFE|LOW_RISK|MODERATE|DANGEROUS|CRITICAL","requiresRoot":false,"reversible":true,"examples":["example1"],"warnings":["warning1"],"useCases":["use case1"],"corrections":[{"suggestedCommand":"corrected cmd","confidence":"HIGH|MEDIUM|LOW","source":"AI"}],"feedback":""}

Rules:
- status: VALID=recognized command, PARTIAL=has typos/missing args, INVALID=wrong syntax, GIBBERISH=not a command
- dangerLevel: rate how dangerous the command is
- For GIBBERISH: set description to "Not a valid command" and leave other fields minimal
- For PARTIAL/INVALID: provide corrections
- NEVER suggest executing any command
- Be concise
- Output ONLY the JSON object, no other text
    """.trimIndent()

    /**
     * Build the user prompt for a specific command.
     *
     * @param command The shell/ADB command to analyze
     * @return The formatted user prompt
     */
    fun buildUserPrompt(command: String): String =
        "Analyze this shell/ADB command: `$command`"
}
