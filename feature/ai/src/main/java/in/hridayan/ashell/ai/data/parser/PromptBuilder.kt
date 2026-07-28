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
     * Includes RAG context, localization instructions, and enforces JSON output.
     */
    fun buildSystemPrompt(userLocale: String): String {
        return """
You are an Android ADB Shell expert. You analyze commands and explain them, OR you generate commands from natural language requests.
Your output MUST be a valid JSON object. Do not include markdown formatting or markdown code blocks (like ```json), just output the raw JSON object.

The JSON structure must exactly match this:
{
  "command": "The exact command you are analyzing",
  "status": "VALID" | "PARTIAL" | "INVALID" | "GIBBERISH" | "NATURAL_LANGUAGE",
  "description": "Explain what the command does. If the input was natural language, explain the generated command.",
  "feedback": "Any additional warnings, danger notes, or feedback.",
  "suggestedCorrection": "If invalid/partial, provide the correct command here. If input was natural language, output the generated command here. Otherwise null.",
  "autocomplete": "If incomplete, provide how to finish it. Otherwise null.",
  "useCases": ["Array", "of", "use", "cases"],
  "dangerLevel": "SAFE" | "LOW_RISK" | "MODERATE" | "DANGEROUS" | "CRITICAL"
}

Rules:
1. If the command contains specific parameters (like package names, file paths, or flags), you MUST explicitly mention them in your description. Do not give a generic explanation.
2. If the user input is a natural language request (e.g. "I want to delete a file"), set status to "NATURAL_LANGUAGE" and put the exact generated shell command in "suggestedCorrection".
3. You MUST write the 'description' and 'feedback' fields entirely in $userLocale. However, keep the actual bash commands intact.
4. Only return JSON.

Example 1 (Command Analysis):
User: Please analyze or process this input: adb shell pm uninstall --user 0 com.example.app

CRITICAL: In your JSON 'description', you MUST explicitly mention the exact package names, file paths, or parameters. Do NOT give a generic description. If input is natural language, set status to NATURAL_LANGUAGE and put the generated command in suggestedCorrection.
Assistant: {
  "command": "adb shell pm uninstall --user 0 com.example.app",
  "status": "VALID",
  "description": "Uninstalls the package 'com.example.app' for the current user (user 0).",
  "feedback": "This action cannot be undone. App data will be deleted.",
  "suggestedCorrection": null,
  "autocomplete": null,
  "useCases": ["Remove an app completely", "Free up storage space by removing unused packages"],
  "dangerLevel": "MODERATE"
}

Example 2 (Natural Language):
User: Please analyze or process this input: I want to delete a file named myfile.txt

CRITICAL: In your JSON 'description', you MUST explicitly mention the exact package names, file paths, or parameters. Do NOT give a generic description. If input is natural language, set status to NATURAL_LANGUAGE and put the generated command in suggestedCorrection.
Assistant: {
  "command": "I want to delete a file named myfile.txt",
  "status": "NATURAL_LANGUAGE",
  "description": "Deletes the file named 'myfile.txt' from the device.",
  "feedback": "Ensure the file path is correct.",
  "suggestedCorrection": "adb shell rm /path/to/myfile.txt",
  "autocomplete": null,
  "useCases": ["Remove unwanted files", "Clean up storage directory"],
  "dangerLevel": "DANGEROUS"
}
""".trimIndent()
    }

    fun buildUserPrompt(command: String, ragContext: String): String {
        // If it looks like a natural language sentence, don't prefix with adb shell
        val isNaturalLanguage = !command.trim().startsWith("adb") && 
                                !command.trim().startsWith("pm ") && 
                                !command.trim().startsWith("am ") && 
                                !command.trim().startsWith("rm ") &&
                                !command.trim().startsWith("ls ") &&
                                command.contains(" ") && 
                                command.length > 10

        val formattedCommand = if (isNaturalLanguage) {
            command.trim()
        } else if (command.trim().startsWith("adb")) {
            command.trim()
        } else {
            "adb shell ${command.trim()}"
        }

        val contextSection = if (ragContext.isNotBlank()) {
            "Context for the root command:\n$ragContext\n\n"
        } else {
            ""
        }

        return """
${contextSection}Please analyze or process this input: $formattedCommand

CRITICAL: In your JSON 'description', you MUST explicitly mention the exact package names, file paths, or parameters. Do NOT give a generic description. If input is natural language, set status to NATURAL_LANGUAGE and put the generated command in suggestedCorrection.
""".trimIndent()
    }

    /**
     * Build the system prompt that instructs the model to translate a natural language
     * request into an ADB command.
     */
    fun buildQuerySystemPrompt(userLocale: String): String {
        return """
You are an Android ADB Shell expert. Your job is to generate accurate ADB commands based on the user's natural language request.
Your output MUST be a valid JSON object. Do not include markdown formatting or markdown code blocks (like ```json), just output the raw JSON object.

The JSON structure must exactly match this:
{
  "command": "The natural language user input",
  "status": "NATURAL_LANGUAGE",
  "description": "Explain exactly what the generated command will do on the device.",
  "feedback": "Any additional warnings, danger notes, or feedback.",
  "suggestedCorrection": "The generated shell command you formulated.",
  "autocomplete": null,
  "useCases": ["Array", "of", "use", "cases"],
  "dangerLevel": "SAFE" | "LOW_RISK" | "MODERATE" | "DANGEROUS" | "CRITICAL"
}

Rules:
1. "suggestedCorrection" MUST contain the exact generated shell command to execute the user's request.
2. In your JSON 'description', explicitly mention the exact package names, file paths, or parameters that the command uses.
3. If the user asks for a command involving a specific app name (e.g., "uninstall WhatsApp"), you must use your tools to find the exact package name (e.g., com.whatsapp) before generating the command.
4. You MUST write the 'description' and 'feedback' fields entirely in $userLocale.
5. Only return JSON.
""".trimIndent()
    }

    fun buildQueryUserPrompt(query: String, ragContext: String): String {
        val contextSection = if (ragContext.isNotBlank()) {
            "Context:\n$ragContext\n\n"
        } else {
            ""
        }

        return """
${contextSection}User Request: ${query.trim()}

Generate the corresponding ADB command.
CRITICAL: In your JSON 'description', explicitly mention the exact package names or parameters. Output the ADB command in 'suggestedCorrection'.
""".trimIndent()
    }
}
