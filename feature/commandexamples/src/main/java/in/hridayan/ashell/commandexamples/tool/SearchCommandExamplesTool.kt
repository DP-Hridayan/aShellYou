package `in`.hridayan.ashell.commandexamples.tool

import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import `in`.hridayan.ashell.core.common.domain.repository.CommandRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchCommandExamplesTool @Inject constructor(
    private val commandRepository: CommandRepository
) : AiTool {

    override val name: String = "search_command_examples"

    override val description: String = "Search the user's Command Examples library for shell command templates, descriptions, or labels. Returns matching command examples along with their IDs, titles, command strings, and labels."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = mapOf(
            "query" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Text to search for within titles, command strings, or labels. Leave blank to list all."
            )
        ),
        required = emptyList()
    )

    override suspend fun execute(args: JsonObject?): String {
        val query = args?.get("query")?.jsonPrimitive?.content ?: ""

        val allCommands = commandRepository.getAllCommandsOnce()
        val filtered = if (query.isBlank()) {
            allCommands
        } else {
            allCommands.filter { cmd ->
                cmd.description.contains(query, ignoreCase = true) ||
                        cmd.command.contains(query, ignoreCase = true) ||
                        cmd.labels.any { it.contains(query, ignoreCase = true) }
            }
        }

        if (filtered.isEmpty()) {
            return "No matching command examples found."
        }

        val jsonArray = buildJsonArray {
            for (cmd in filtered) {
                add(
                    buildJsonObject {
                        put("id", cmd.id)
                        put("title", cmd.description)
                        put("command", cmd.command)
                        putJsonArray("labels") {
                            for (label in cmd.labels) {
                                add(kotlinx.serialization.json.JsonPrimitive(label))
                            }
                        }
                    }
                )
            }
        }

        return jsonArray.toString()
    }
}
