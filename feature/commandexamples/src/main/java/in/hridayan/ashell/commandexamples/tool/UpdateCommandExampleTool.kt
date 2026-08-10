package `in`.hridayan.ashell.commandexamples.tool

import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.core.common.domain.repository.CommandRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateCommandExampleTool @Inject constructor(
    private val commandRepository: CommandRepository
) : AiTool {

    override val name: String = "update_command_example"

    override val description: String = "Update an existing shell command in the user's Command Examples library. You must provide the exact ID of the command (which you can get by using search_command_examples first) and optionally the fields you want to update."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = "OBJECT",
        properties = mapOf(
            "id" to ToolSchemaProperty(
                type = "INTEGER",
                description = "The exact ID of the command example to update."
            ),
            "title" to ToolSchemaProperty(
                type = "STRING",
                description = "Optional. New short, descriptive title of what the command does."
            ),
            "command_string" to ToolSchemaProperty(
                type = "STRING",
                description = "Optional. New exact shell command string or template."
            ),
            "labels" to ToolSchemaProperty(
                type = "STRING",
                description = "Optional. New comma-separated string labels to categorize the command."
            )
        ),
        required = listOf("id")
    )

    override suspend fun execute(args: JsonObject?): String {
        val id = args?.get("id")?.jsonPrimitive?.content?.toIntOrNull()
            ?: return "Error: valid integer id is required."

        val existingCommand = commandRepository.getCommandById(id)
            ?: return "Error: No command example found with ID $id."

        val title = args["title"]?.jsonPrimitive?.content ?: existingCommand.description
        val commandString = args["command_string"]?.jsonPrimitive?.content ?: existingCommand.command

        val labelsList = if (args.containsKey("labels")) {
            val labelsString = args["labels"]?.jsonPrimitive?.content ?: ""
            if (labelsString.isBlank()) emptyList() else labelsString.split(
                ","
            ).map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            existingCommand.labels
        }

        val updatedCommand = existingCommand.copy(
            command = commandString,
            description = title,
            labels = labelsList
        )

        commandRepository.updateCommand(updatedCommand)

        return "Successfully updated command example '$title' (ID: $id)."
    }
}
