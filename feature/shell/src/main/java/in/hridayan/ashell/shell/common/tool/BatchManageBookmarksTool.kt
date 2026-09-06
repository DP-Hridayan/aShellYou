package `in`.hridayan.ashell.shell.common.tool

import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import `in`.hridayan.ashell.shell.common.domain.repository.BookmarkRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatchManageBookmarksTool @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : AiTool {

    override val name: String = "batch_manage_bookmarks"

    override val description: String =
        "Perform bulk database operations (insert, update, delete) on bookmarks in a single tool call to save time. Provide JSON arrays formatted as strings for each operation."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = mapOf(
            "inserts_json" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Optional. A JSON array string of commands to insert. Example: '[\"ls -l\", \"pwd\"]'"
            ),
            "updates_json" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Optional. A JSON array string of objects containing 'id' and 'command_string'. Example: '[{\"id\": 1, \"command_string\": \"ping google.com\"}]'"
            ),
            "deletes_json" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Optional. A JSON array string of integer IDs to delete. Example: '[1, 2, 3]'"
            )
        ),
        required = emptyList()
    )

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
                        val cmd = element.jsonPrimitive.content
                        bookmarkRepository.addBookmark(cmd)
                        insertedCount++
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
                        val cmd = obj["command_string"]?.jsonPrimitive?.content
                        if (id != null && cmd != null) {
                            val existing = bookmarkRepository.getBookmarkById(id)
                            if (existing != null) {
                                bookmarkRepository.updateBookmark(existing.copy(command = cmd))
                                updatedCount++
                            } else {
                                errors.add("Bookmark ID $id not found for update.")
                            }
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
                            bookmarkRepository.deleteBookmarkById(id)
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
