package `in`.hridayan.ashell.commandexamples.tool

import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.core.common.domain.repository.CommandRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteCommandExampleTool @Inject constructor(
    private val commandRepository: CommandRepository
) : AiTool {

    override val name: String = "delete_command_example"

    override val description: String = "Delete a command example from the user's Command Examples library by its integer database ID."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = "OBJECT",
        properties = mapOf(
            "id" to ToolSchemaProperty(
                type = "INTEGER",
                description = "The database ID of the command example to delete (obtained via search_command_examples)."
            )
        ),
        required = listOf("id")
    )

    override suspend fun execute(args: JsonObject?): String {
        val id = args?.get("id")?.jsonPrimitive?.int ?: return "Error: valid integer id is required"

        val existing = commandRepository.getCommandById(id)
            ?: return "Error: No command example found with ID $id."

        commandRepository.deleteCommand(id)
        return "Successfully deleted command example '${existing.description}' (ID: $id)."
    }
}
