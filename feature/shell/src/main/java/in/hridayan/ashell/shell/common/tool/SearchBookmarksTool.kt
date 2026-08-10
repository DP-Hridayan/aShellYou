package `in`.hridayan.ashell.shell.common.tool

import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.shell.common.domain.repository.BookmarkRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchBookmarksTool @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : AiTool {

    override val name: String = "search_bookmarks"

    override val description: String = "Search the user's bookmarked shell commands. Returns matching bookmarks along with their database ID and exact command string."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = "OBJECT",
        properties = mapOf(
            "query" to ToolSchemaProperty(
                type = "STRING",
                description = "Text to search for within bookmarked commands. Leave blank to list all bookmarks."
            )
        ),
        required = emptyList()
    )

    override suspend fun execute(args: JsonObject?): String {
        val query = args?.get("query")?.jsonPrimitive?.content ?: ""

        val allBookmarks = bookmarkRepository.getBookmarksSorted(0) // Default sorting
        val filtered = if (query.isBlank()) {
            allBookmarks
        } else {
            allBookmarks.filter { it.command.contains(query, ignoreCase = true) }
        }

        if (filtered.isEmpty()) {
            return "No matching bookmarked commands found."
        }

        val jsonArray = buildJsonArray {
            for (bm in filtered) {
                add(
                    buildJsonObject {
                        put("id", bm.id)
                        put("command", bm.command)
                    }
                )
            }
        }

        return jsonArray.toString()
    }
}
