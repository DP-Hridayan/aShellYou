package `in`.hridayan.ashell.shell.common.tool

import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import `in`.hridayan.ashell.shell.common.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveBookmarkTool @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : AiTool {

    override val name: String = "save_bookmark"

    override val description: String = "Save a fully formed, ready-to-execute shell command (with all parameters filled) into the user's Bookmarks for quick access. Only use this if the command is general, parameter-less, or the user specifically requests to save it as a bookmark."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = mapOf(
            "command_string" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "The exact shell command string to bookmark."
            )
        ),
        required = listOf("command_string")
    )

    override suspend fun execute(args: JsonObject?): String {
        val commandString = args?.get("command_string")?.jsonPrimitive?.content ?: return "Error: command_string is required"

        val exists = bookmarkRepository.isBookmarked(commandString).first()
        if (exists) {
            return "Command is already bookmarked."
        }

        bookmarkRepository.addBookmark(commandString)

        return "Successfully saved command to Bookmarks."
    }
}
