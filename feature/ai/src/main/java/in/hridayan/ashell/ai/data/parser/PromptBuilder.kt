package `in`.hridayan.ashell.ai.data.parser

/**
 * Builds system and user prompts for AI command analysis.
 *
 * Designed to guide small instruction-tuned models (0.5B) to output
 * a single accurate sentence using a few-shot structure.
 */
object PromptBuilder {

    /**
     * Build the system prompt that instructs the model to describe commands.
     * Includes few-shot examples to enforce conciseness and prevent rambling.
     */
    fun buildSystemPrompt(): String = """
You describe Android ADB/shell commands in one sentence.
If the command is unrecognized, invalid, or gibberish, reply ONLY with: Not a recognized command.

Examples:
Command: adb shell pm list packages
Description: Lists all installed packages on the device.

Command: adb shell ls -la
Description: Lists all files and directories in the current folder with detailed information.

Command: adb shell sksksksk
Description: Not a recognized command.

Command: adb shell reboot
Description: Reboots the Android device.
""".trimIndent()

    /**
     * Build the user prompt for a specific command.
     *
     * @param command The shell/ADB command to analyze
     * @return The formatted user prompt
     */
    fun buildUserPrompt(command: String): String = """
Command: ${if (command.trim().startsWith("adb")) command.trim() else "adb shell ${command.trim()}"}
Description:
""".trimIndent()
}
