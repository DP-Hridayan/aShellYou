package `in`.hridayan.ashell.ai.domain.tool.builtin.diagnostics

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetMemoryInfoTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AiTool {

    override val name: String = "get_memory_info"

    override val description: String =
        "Retrieves total RAM, available RAM, and low memory state of the device natively."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = emptyMap(),
        required = emptyList()
    )

    override suspend fun execute(args: JsonObject?): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return "Failed to access ActivityManager."

        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        return formatMemoryInfo(memoryInfo)
    }

    private fun formatMemoryInfo(memoryInfo: ActivityManager.MemoryInfo): String {
        val totalRamGb = bytesToGigabytes(memoryInfo.totalMem)
        val availableRamGb = bytesToGigabytes(memoryInfo.availMem)
        val thresholdGb = bytesToGigabytes(memoryInfo.threshold)

        return """
            Memory Info:
            Total RAM: ${"%.2f".format(totalRamGb)} GB
            Available RAM: ${"%.2f".format(availableRamGb)} GB
            Low Memory Threshold: ${"%.2f".format(thresholdGb)} GB
            Is Low Memory State: ${memoryInfo.lowMemory}
        """.trimIndent()
    }

    private fun bytesToGigabytes(bytes: Long): Double {
        return bytes.toDouble() / (1024 * 1024 * 1024)
    }
}
