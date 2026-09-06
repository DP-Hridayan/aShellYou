package `in`.hridayan.ashell.commandexamples.tool

import `in`.hridayan.ashell.core.common.domain.model.CommandEntity
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import `in`.hridayan.ashell.core.common.domain.repository.CommandRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatchManageCommandExamplesTool @Inject constructor(
    private val commandRepository: CommandRepository
) : AiTool {

    override val name: String = "batch_manage_command_examples"

    override val description: String =
        "Perform bulk database operations (insert, update, delete) on command examples in a single tool call to save time. Provide JSON arrays formatted as strings for each operation."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = mapOf(
            "inserts_json" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Optional. A JSON array string of objects with 'title', 'command_string', and optionally 'labels'. Example: '[{\"title\": \"A\", \"command_string\": \"cmd\", \"labels\": \"L1,L2\"}]'"
            ),
            "updates_json" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Optional. A JSON array string of objects containing 'id' (required) and optionally 'title', 'command_string', 'labels'. Example: '[{\"id\": 1, \"title\": \"New Title\"}]'"
            ),
            "deletes_json" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Optional. A JSON array string of integer IDs to delete. Example: '[1, 2, 3]'"
            )
        ),
        required = emptyList()
    )

    private fun parseLabels(labelsString: String?): List<String> {
        if (labelsString.isNullOrBlank()) return emptyList()
        return labelsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    override suspend fun execute(args: JsonObject?): String {
        if (args == null) return "Error: No arguments provided."

        var insertedCount = 0
        var updatedCount = 0
        var deletedCount = 0

        val errors = mutableListOf<String>()

        // Process inserts
        args["inserts_json"]?.jsonPrimitive?.content?.let { jsonStr ->
            if (jsonStr.isNotBlank()) {
                try {
                    val array = Json.decodeFromString<JsonArray>(jsonStr)
                    for (element in array) {
                        val obj = element.jsonObject
                        val title = obj["title"]?.jsonPrimitive?.content
                        val cmd = obj["command_string"]?.jsonPrimitive?.content
                        val labelsStr = obj["labels"]?.jsonPrimitive?.content

                        if (title != null && cmd != null) {
                            commandRepository.insertCommand(
                                CommandEntity(
                                    command = cmd,
                                    description = title,
                                    labels = parseLabels(labelsStr)
                                )
                            )
                            insertedCount++
                        } else {
                            errors.add("Insert failed: 'title' and 'command_string' are required.")
                        }
                    }
                } catch (e: Exception) {
                    errors.add("Failed to parse or execute inserts_json: ${e.message}")
                }
            }
        }

        // Process updates
        args["updates_json"]?.jsonPrimitive?.content?.let { jsonStr ->
            if (jsonStr.isNotBlank()) {
                try {
                    val array = Json.decodeFromString<JsonArray>(jsonStr)
                    for (element in array) {
                        val obj = element.jsonObject
                        val id = obj["id"]?.jsonPrimitive?.intOrNull

                        if (id != null) {
                            val existing = commandRepository.getCommandById(id)
                            if (existing != null) {
                                val title =
                                    obj["title"]?.jsonPrimitive?.content ?: existing.description
                                val cmd = obj["command_string"]?.jsonPrimitive?.content
                                    ?: existing.command
                                val labelsList = if (obj.containsKey("labels")) {
                                    parseLabels(obj["labels"]?.jsonPrimitive?.content)
                                } else {
                                    existing.labels
                                }

                                val updated = existing.copy(
                                    command = cmd,
                                    description = title,
                                    labels = labelsList
                                )
                                commandRepository.updateCommand(updated)
                                updatedCount++
                            } else {
                                errors.add("Command Example ID $id not found for update.")
                            }
                        } else {
                            errors.add("Update failed: 'id' is required.")
                        }
                    }
                } catch (e: Exception) {
                    errors.add("Failed to parse or execute updates_json: ${e.message}")
                }
            }
        }

        // Process deletes
        args["deletes_json"]?.jsonPrimitive?.content?.let { jsonStr ->
            if (jsonStr.isNotBlank()) {
                try {
                    val array = Json.decodeFromString<JsonArray>(jsonStr)
                    for (element in array) {
                        val id = element.jsonPrimitive.intOrNull
                        if (id != null) {
                            commandRepository.deleteCommand(id)
                            deletedCount++
                        }
                    }
                } catch (e: Exception) {
                    errors.add("Failed to parse or execute deletes_json: ${e.message}")
                }
            }
        }

        return buildString {
            append("Batch execution complete. ")
            append("Inserted: $insertedCount, Updated: $updatedCount, Deleted: $deletedCount. ")
            if (errors.isNotEmpty()) {
                append("\nErrors encountered:\n- " + errors.joinToString("\n- "))
            }
        }
    }
}
