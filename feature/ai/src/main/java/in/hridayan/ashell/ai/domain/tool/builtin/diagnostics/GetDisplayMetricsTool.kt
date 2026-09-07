package `in`.hridayan.ashell.ai.domain.tool.builtin.diagnostics

import android.content.Context
import android.content.res.Resources
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDisplayMetricsTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AiTool {

    override val name: String = "get_display_metrics"

    override val description: String =
        "Retrieves the screen resolution, density (DPI), and current refresh rate natively."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = emptyMap(),
        required = emptyList()
    )

    override suspend fun execute(args: JsonObject?): String {
        return buildDisplayMetricsString()
    }

    private fun buildDisplayMetricsString(): String {
        val displayMetrics = Resources.getSystem().displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val densityDpi = displayMetrics.densityDpi
        val density = displayMetrics.density

        val refreshRate = getRefreshRate()

        return """
            Display Metrics:
            Resolution: ${width}x${height} pixels
            Density: $densityDpi DPI (Scale: ${density}x)
            Current Refresh Rate: $refreshRate Hz
        """.trimIndent()
    }

    private fun getRefreshRate(): Float {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            @Suppress("DEPRECATION")
            windowManager?.defaultDisplay?.refreshRate ?: 0f
        } catch (e: Exception) {
            0f
        }
    }
}
