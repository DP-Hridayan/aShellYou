package `in`.hridayan.ashell.ai.domain.tool.builtin

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueryInstalledAppsTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AiTool {

    override val name: String = "query_installed_apps"
    
    override val description: String = "Query the installed applications on the device. Returns a JSON array of apps. Use this instead of running 'pm list packages' via shell to save tokens and get structured data."
    
    override val parametersSchema: ToolSchema = ToolSchema(
        type = "OBJECT",
        properties = mapOf(
            "search_query" to ToolSchemaProperty(
                type = "STRING",
                description = "Optional search query to filter apps by name or package name."
            ),
            "include_system_apps" to ToolSchemaProperty(
                type = "BOOLEAN",
                description = "Whether to include system apps in the results. Default is false."
            )
        ),
        required = listOf()
    )

    override suspend fun execute(args: JsonObject?): String {
        val searchQuery = args?.get("search_query")?.jsonPrimitive?.content ?: ""
        val includeSystemApps = args?.get("include_system_apps")?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false

        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val filteredPackages = packages.filter { appInfo ->
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (!includeSystemApps && isSystem) return@filter false
            
            if (searchQuery.isNotBlank()) {
                val label = pm.getApplicationLabel(appInfo).toString()
                label.contains(searchQuery, ignoreCase = true) || appInfo.packageName.contains(searchQuery, ignoreCase = true)
            } else {
                true
            }
        }

        val jsonArray = buildJsonArray {
            for (appInfo in filteredPackages) {
                add(buildJsonObject {
                    put("name", pm.getApplicationLabel(appInfo).toString())
                    put("package", appInfo.packageName)
                    put("isSystem", (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0)
                })
            }
        }

        return jsonArray.toString()
    }
}
