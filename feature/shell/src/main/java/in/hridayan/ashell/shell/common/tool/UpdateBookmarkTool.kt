package `in`.hridayan.ashell.shell.common.tool

import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import `in`.hridayan.ashell.shell.common.domain.repository.BookmarkRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateBookmarkTool @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : AiTool {

    override val name: String = "update_bookmark"

    override val description: String =
        "Update an existing shell command bookmark. You must provide the exact ID of the bookmark (which you can get by using search_bookmarks first) and the new command string."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = mapOf(
            "id" to ToolSchemaProperty(
                type = ToolSchemaType.INTEGER,
                description = "The exact ID of the bookmark to update."
            ),
            "command_string" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "The new shell command string to replace the old one."
            )
        ),
        required = listOf("id", "command_string")
    )

    override suspend fun execute(args: JsonObject?): String {
        val id = args?.get("id")?.jsonPrimitive?.content?.toIntOrNull()
            ?: return "Error: valid integer id is required."

        val commandString = args.get("command_string")?.jsonPrimitive?.content
            ?: return "Error: command_string is required."

        val existingBookmark = bookmarkRepository.getBookmarkById(id)
            ?: return "Error: No bookmark found with ID $id."

        val updatedBookmark = existingBookmark.copy(command = commandString)
        bookmarkRepository.updateBookmark(updatedBookmark)

        return "Successfully updated bookmark (ID: $id) with new command."
    }
}
