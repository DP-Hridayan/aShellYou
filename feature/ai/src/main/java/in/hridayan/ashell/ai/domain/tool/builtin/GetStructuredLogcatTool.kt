package `in`.hridayan.ashell.ai.domain.tool.builtin

import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetStructuredLogcatTool @Inject constructor() : AiTool {

    override val name: String = "get_structured_logcat"

    override val description: String = "Fetch recent logcat output. Uses 'logcat -d' to safely fetch logs without hanging. Returns raw text logs."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = mapOf(
            "log_level" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Optional log level to filter by (e.g., '*:E' for errors, '*:W' for warnings). Default is '*:D'."
            ),
            "max_lines" to ToolSchemaProperty(
                type = ToolSchemaType.INTEGER,
                description = "Maximum number of lines to fetch. Default is 100. Max is 500."
            ),
            "grep_filter" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Optional string to filter log lines containing this string."
            )
        ),
        required = emptyList()
    )

    override suspend fun execute(args: JsonObject?): String {
        val logLevel = args?.get("log_level")?.jsonPrimitive?.content ?: "*:D"
        val maxLines = args?.get("max_lines")?.jsonPrimitive?.content?.toIntOrNull()?.coerceAtMost(500) ?: 100
        val grepFilter = args?.get("grep_filter")?.jsonPrimitive?.content

        return try {
            val process = withContext(Dispatchers.IO) {
                Runtime.getRuntime().exec(
                    arrayOf("logcat", "-d", "-v", "threadtime", "-t", maxLines.toString(), logLevel)
                )
            }
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            val builder = StringBuilder()
            var line: String?
            var count = 0

            while (withContext(Dispatchers.IO) {
                    reader.readLine()
                }.also { line = it } != null) {
                if (grepFilter.isNullOrBlank() || line!!.contains(grepFilter, ignoreCase = true)) {
                    builder.appendLine(line)
                    count++
                    if (count >= maxLines) break
                }
            }

            withContext(Dispatchers.IO) {
                process.waitFor()
            }

            if (builder.isEmpty()) {
                "No logs found."
            } else {
                builder.toString()
            }
        } catch (e: Exception) {
            "Error fetching logcat: ${e.message}"
        }
    }
}
