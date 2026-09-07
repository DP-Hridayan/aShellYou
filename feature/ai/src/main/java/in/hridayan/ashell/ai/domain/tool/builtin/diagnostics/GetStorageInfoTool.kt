package `in`.hridayan.ashell.ai.domain.tool.builtin.diagnostics

import android.os.Environment
import android.os.StatFs
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetStorageInfoTool @Inject constructor() : AiTool {

    override val name: String = "get_storage_info"

    override val description: String =
        "Retrieves the total and available internal storage space natively."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = emptyMap(),
        required = emptyList()
    )

    override suspend fun execute(args: JsonObject?): String {
        return try {
            val statFs = StatFs(Environment.getDataDirectory().path)
            formatStorageInfo(statFs)
        } catch (e: Exception) {
            "Error retrieving storage info: ${e.message}"
        }
    }

    private fun formatStorageInfo(statFs: StatFs): String {
        val totalSpaceBytes = statFs.blockCountLong * statFs.blockSizeLong
        val availableSpaceBytes = statFs.availableBlocksLong * statFs.blockSizeLong
        val usedSpaceBytes = totalSpaceBytes - availableSpaceBytes

        val totalSpaceGb = bytesToGigabytes(totalSpaceBytes)
        val availableSpaceGb = bytesToGigabytes(availableSpaceBytes)
        val usedSpaceGb = bytesToGigabytes(usedSpaceBytes)

        val percentageUsed = if (totalSpaceBytes > 0) {
            (usedSpaceBytes.toDouble() / totalSpaceBytes.toDouble()) * 100
        } else {
            0.0
        }

        return """
            Storage Info:
            Total Internal Storage: ${"%.2f".format(totalSpaceGb)} GB
            Used Space: ${"%.2f".format(usedSpaceGb)} GB (${"%.1f".format(percentageUsed)}%)
            Available Space: ${"%.2f".format(availableSpaceGb)} GB
        """.trimIndent()
    }

    private fun bytesToGigabytes(bytes: Long): Double {
        return bytes.toDouble() / (1024 * 1024 * 1024)
    }
}
