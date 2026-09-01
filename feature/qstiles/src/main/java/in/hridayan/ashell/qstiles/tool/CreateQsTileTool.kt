package `in`.hridayan.ashell.qstiles.tool

import androidx.compose.ui.text.input.TextFieldValue
import `in`.hridayan.ashell.core.common.domain.model.TileExecutionMode
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import `in`.hridayan.ashell.qstiles.data.provider.TileComponentManager
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.domain.model.TileActiveState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonObject as KJsonObject

@Singleton
class CreateQsTileTool @Inject constructor(
    private val repository: TileRepository,
    private val tileComponentManager: TileComponentManager
) : AiTool {

    override val name: String = "create_qs_tile"

    override val description: String =
        "Create a custom Quick Settings (QS) tile for the user in an available slot (1-10). Before calling this tool: 1) ALWAYS use get_qs_tile_slots to check which slot is empty if you haven't yet, 2) If the user didn't specify an execution mode in their request, ask the user which execution mode they prefer (0 for Shizuku [default], 1 for Root), and 3) Decide whether the tile should be a toggle (on/off switch) or a simple tap action based on the command/purpose."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = mapOf(
            "title" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Short name for the tile (max 15 chars)."
            ),
            "command" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "The exact shell command to run when the tile is ALREADY ON (i.e. to turn the feature OFF). For simple tap tiles, this is the action command."
            ),
            "icon_name" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Icon identifier for the tile. Valid options: ${TileIconProvider.icons.joinToString { it.id }}"
            ),
            "execution_mode" to ToolSchemaProperty(
                type = ToolSchemaType.INTEGER,
                description = "0 for Shizuku (default), 1 for Root."
            ),
            "slot" to ToolSchemaProperty(
                type = ToolSchemaType.INTEGER,
                description = "Optional target slot ID (1 to 10). If omitted or occupied, the first available empty slot is used."
            ),
            "is_toggleable" to ToolSchemaProperty(
                type = ToolSchemaType.BOOLEAN,
                description = "Whether the tile represents an on/off toggle (true) or a one-shot tap action (false). Default false."
            ),
            "inactive_command" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "If is_toggleable is true, the shell command to run when the tile is ALREADY OFF (i.e. to turn the feature ON). Default empty string."
            ),
            "active_subtitle" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Subtitle text when tile is active/on (e.g. 'On' or 'Enabled'). Default 'On'."
            ),
            "inactive_subtitle" to ToolSchemaProperty(
                type = ToolSchemaType.STRING,
                description = "Subtitle text when tile is inactive/off (e.g. 'Off' or 'Disabled'). Default 'Off'."
            )
        ),
        required = listOf("title", "command", "icon_name")
    )

    override suspend fun execute(args: KJsonObject?): String {
        val title = args?.get("title")?.jsonPrimitive?.content ?: return "Error: title is required"
        val command = args["command"]?.jsonPrimitive?.content ?: return "Error: command is required"
        val iconName = args["icon_name"]?.jsonPrimitive?.content ?: "terminal"

        val rawMode = args["execution_mode"]?.jsonPrimitive?.content?.toIntOrNull()
            ?: TileExecutionMode.SHIZUKU
        val executionMode = if (rawMode == TileExecutionMode.ROOT) {
            TileExecutionMode.ROOT
        } else {
            TileExecutionMode.SHIZUKU // Default to Shizuku (0) for any invalid number like 3
        }

        val requestedSlot = args["slot"]?.jsonPrimitive?.content?.toIntOrNull()
        val isToggleable =
            args["is_toggleable"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
        val inactiveCmd = args["inactive_command"]?.jsonPrimitive?.content ?: ""
        val activeSubtitle = args["active_subtitle"]?.jsonPrimitive?.content ?: "On"
        val inactiveSubtitle = args["inactive_subtitle"]?.jsonPrimitive?.content ?: "Off"

        val existing = repository.getTiles().first()
        val tileId =
            if (requestedSlot != null && requestedSlot in 1..10 && existing.none { it.id == requestedSlot }) {
                requestedSlot
            } else {
                (1..10).firstOrNull { id -> existing.none { it.id == id } }
            }

        if (tileId == null) {
            return "Failed to create QS tile: All 10 Quick Settings tile slots (1 to 10) are currently occupied. Please delete an existing tile before creating a new one."
        }

        val slotIndex = tileId - 1 // 0 to 9

        val tileConfig = TileConfig(
            id = tileId,
            name = title,
            executionMode = executionMode,
            iconId = iconName,
            isCustom = true,
            slotIndex = slotIndex,
            activeState = TileActiveState(
                isToggleable = isToggleable,
                isActive = false,
                activeCommand = TextFieldValue(command),
                inactiveCommand = TextFieldValue(inactiveCmd),
                activeTileSubtitle = TextFieldValue(activeSubtitle),
                inactiveTileSubtitle = TextFieldValue(inactiveSubtitle)
            )
        )

        repository.createTile(tileConfig)

        // Ensure component is enabled
        tileComponentManager.setComponentEnabled(slotIndex, true)

        // Prompt user to add to panel
        tileComponentManager.promptAddTile(
            slotIndex = slotIndex,
            label = title,
            iconResId = TileIconProvider.getIconRes(iconName)
        )

        val modeStr = if (executionMode == TileExecutionMode.ROOT) "Root" else "Shizuku"
        val typeStr =
            if (isToggleable) "Toggleable (ON: '$command', OFF: '$inactiveCmd')" else "Tap Action ('$command')"
        return "Successfully created QS tile '$title' in Slot $tileId (Slot Index $slotIndex) using $modeStr mode ($typeStr). System prompt dialog triggered to add Tile $tileId to Quick Settings panel. Tell the user which slot number ($tileId) it was created in."
    }
}
