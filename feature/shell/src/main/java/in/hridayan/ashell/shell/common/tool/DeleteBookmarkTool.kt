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
class DeleteBookmarkTool @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : AiTool {

    override val name: String = "delete_bookmark"

    override val description: String = "Delete a bookmarked shell command from the user's bookmarks library by exact command string."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = mapOf(
            "command_string" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "The exact shell command string to remove from bookmarks."
            )
        ),
        required = listOf("command_string")
    )

    override suspend fun execute(args: JsonObject?): String {
        val commandString = args?.get("command_string")?.jsonPrimitive?.content ?: return "Error: command_string is required"

        bookmarkRepository.deleteBookmarkByCommand(commandString)
        return "Successfully removed '$commandString' from bookmarks."
    }
}
