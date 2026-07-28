package `in`.hridayan.ashell.core.common.domain.tools

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class FindAppPackageTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AiTool {
    override val name: String = "find_app_package"
    
    override val description: String = "Search for an installed application's package name by providing its common app name (e.g., 'whatsapp', 'facebook'). Returns the package name if found."
    
    override val parametersSchema: ToolSchema = ToolSchema(
        type = "OBJECT",
        properties = mapOf(
            "appName" to ToolSchemaProperty(
                type = "STRING",
                description = "The name of the application to search for."
            )
        ),
        required = listOf("appName")
    )

    override suspend fun execute(args: JsonObject?): String {
        val appName = args?.get("appName")?.jsonPrimitive?.content
            ?: return "Error: appName parameter is required."

        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        // Find best match
        val matches = packages.filter { appInfo ->
            val label = pm.getApplicationLabel(appInfo).toString()
            label.contains(appName, ignoreCase = true) || appInfo.packageName.contains(appName, ignoreCase = true)
        }
        
        if (matches.isEmpty()) {
            return "No application found matching '$appName'."
        }
        
        // Sort by exact match first, then return the first one
        val exactMatch = matches.find { pm.getApplicationLabel(it).toString().equals(appName, ignoreCase = true) }
        val bestMatch = exactMatch ?: matches.first()
        
        return bestMatch.packageName
    }
}
