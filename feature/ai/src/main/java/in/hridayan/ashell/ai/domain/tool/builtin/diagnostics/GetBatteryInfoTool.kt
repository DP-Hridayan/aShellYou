package `in`.hridayan.ashell.ai.domain.tool.builtin.diagnostics

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetBatteryInfoTool @Inject constructor(
    @ApplicationContext private val context: Context
) : AiTool {

    override val name: String = "get_battery_info"

    override val description: String =
        "Retrieves current battery status including percentage, health, temperature, and charging state natively."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = emptyMap(),
        required = emptyList()
    )

    override suspend fun execute(args: JsonObject?): String {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus =
            context.registerReceiver(null, intentFilter) ?: return "Battery status unavailable."
        return buildBatteryStatusString(batteryStatus)
    }

    private fun buildBatteryStatusString(intent: Intent): String {
        val percentage = getBatteryPercentage(intent)
        val chargingState = getChargingState(intent)
        val health = getBatteryHealth(intent)
        val temperature = getBatteryTemperature(intent)
        val voltage = getBatteryVoltage(intent)
        val technology = getBatteryTechnology(intent)

        return """
            Battery Info:
            Level: $percentage%
            Status: $chargingState
            Health: $health
            Temperature: $temperature°C
            Voltage: $voltage mV
            Technology: $technology
        """.trimIndent()
    }

    private fun getBatteryPercentage(intent: Intent): Float {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        return if (level != -1 && scale != -1) {
            (level.toFloat() / scale.toFloat()) * 100f
        } else {
            0f
        }
    }

    private fun getChargingState(intent: Intent): String {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> getPlugTypeString(intent)
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
    }

    private fun getPlugTypeString(intent: Intent): String {
        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        return when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "Charging (USB)"
            BatteryManager.BATTERY_PLUGGED_AC -> "Charging (AC)"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Charging (Wireless)"
            else -> "Charging"
        }
    }

    private fun getBatteryHealth(intent: Intent): String {
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
    }

    private fun getBatteryTemperature(intent: Intent): Float {
        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        return tempTenths / 10f
    }

    private fun getBatteryVoltage(intent: Intent): Int {
        return intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
    }

    private fun getBatteryTechnology(intent: Intent): String {
        return intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
    }
}
