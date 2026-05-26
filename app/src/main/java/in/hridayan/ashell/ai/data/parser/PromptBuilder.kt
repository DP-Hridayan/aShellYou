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
     * Uses full key names (the 0.5B model handles these reliably) but minimal
     * field count. Two examples teach correct danger level differentiation.
     * ~120 tokens system prompt for fast first-call evaluation.
     */
    fun buildSystemPrompt(): String = """
Analyze Android ADB/shell commands. Reply ONLY with JSON. description MUST accurately state what the command does.
Command: pm list packages
{"status":"VALID","description":"Lists all installed packages","dangerLevel":"SAFE","requiresRoot":false,"corrections":[],"feedback":""}
Command: pm uninstall
{"status":"PARTIAL","description":"Missing package name argument","dangerLevel":"MODERATE","requiresRoot":false,"corrections":["pm uninstall <package>"],"feedback":"Specify package"}
Command: xyzabc
{"status":"GIBBERISH","description":"Not a recognized command","dangerLevel":"SAFE","requiresRoot":false,"corrections":[],"feedback":""}
status: VALID/PARTIAL/INVALID/GIBBERISH. dangerLevel: SAFE/LOW_RISK/MODERATE/DANGEROUS/CRITICAL.
""".trimIndent()

    /**
     * Build the user prompt for a specific command.
     *
     * @param command The shell/ADB command to analyze
     * @return The formatted user prompt
     */
    fun buildUserPrompt(command: String): String = "Command: $command\nJSON:"
}
