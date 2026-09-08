package `in`.hridayan.ashell.ai.domain.tool.builtin.diagnostics

import android.os.Build
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetHardwareSpecsTool @Inject constructor() : AiTool {

    override val name: String = "get_hardware_specs"

    override val description: String =
        "Retrieves native hardware specifications including Model, Manufacturer, Android Version, SDK, and CPU Architecture."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = emptyMap(),
        required = emptyList()
    )

    override suspend fun execute(args: JsonObject?): String {
        return buildHardwareSpecsString()
    }

    private fun buildHardwareSpecsString(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val brand = Build.BRAND
        val androidVersion = Build.VERSION.RELEASE
        val sdkInt = Build.VERSION.SDK_INT
        val abis = Build.SUPPORTED_ABIS.joinToString(", ")
        val bootloader = Build.BOOTLOADER
        val hardware = Build.HARDWARE

        return """
            Hardware Specifications:
            Manufacturer: $manufacturer
            Brand: $brand
            Model: $model
            Android Version: $androidVersion (API $sdkInt)
            Supported Architectures (ABIs): $abis
            Hardware/Board: $hardware
            Bootloader: $bootloader
        """.trimIndent()
    }
}
