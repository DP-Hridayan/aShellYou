package `in`.hridayan.ashell.commandexamples.tool

import `in`.hridayan.ashell.core.common.domain.model.CommandEntity
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.core.common.domain.repository.CommandRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveCommandExampleTool @Inject constructor(
    private val commandRepository: CommandRepository
) : AiTool {

    override val name: String = "save_command_example"

    override val description: String = "Save a shell command into the user's Command Examples library for later use. Use this for general templates like 'pm uninstall --user <user> <package>' or complex scripts with placeholders."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = "OBJECT",
        properties = mapOf(
            "title" to ToolSchemaProperty(
                type = "STRING",
                description = "Short, descriptive title of what the command does."
            ),
            "command_string" to ToolSchemaProperty(
                type = "STRING",
                description = "The exact shell command string or template."
            ),
            "labels" to ToolSchemaProperty(
                type = "STRING",
                description = "Comma-separated string labels to categorize the command (e.g., 'Package Manager, Debloat, Battery'). Keep labels concise."
            )
        ),
        required = listOf("title", "command_string")
    )

    override suspend fun execute(args: JsonObject?): String {
        val title = args?.get("title")?.jsonPrimitive?.content ?: return "Error: title is required"
        val commandString = args["command_string"]?.jsonPrimitive?.content ?: return "Error: command_string is required"
        val labelsString = args["labels"]?.jsonPrimitive?.content ?: ""
        val labelsList = if (labelsString.isBlank()) {
            emptyList()
        } else {
            labelsString.split(",").map {
                it.trim()
            }.filter { it.isNotEmpty() }
        }

        // Check if it already exists
        val existingCommands = commandRepository.getAllCommandsOnce()
        if (existingCommands.any { it.command == commandString }) {
            return "Command already exists in Command Examples."
        }

        commandRepository.insertCommand(
            CommandEntity(
                command = commandString,
                description = title,
                labels = labelsList
            )
        )

        return "Successfully saved '$title' to Command Examples."
    }
}
