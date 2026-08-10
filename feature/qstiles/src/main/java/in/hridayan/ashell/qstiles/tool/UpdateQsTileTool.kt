package `in`.hridayan.ashell.qstiles.tool

import androidx.compose.ui.text.input.TextFieldValue
import `in`.hridayan.ashell.core.common.domain.model.TileExecutionMode
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaProperty
import `in`.hridayan.ashell.qstiles.data.provider.TileComponentManager
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonObject as KJsonObject

@Singleton
class UpdateQsTileTool @Inject constructor(
    private val repository: TileRepository,
    private val tileComponentManager: TileComponentManager
) : AiTool {

    override val name: String = "update_qs_tile"

    override val description: String = "Update an existing custom Quick Settings (QS) tile. You only need to provide the slot/id of the tile to update, and optionally any properties you want to change. If a property is omitted, its current value is preserved."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = "OBJECT",
        properties = mapOf(
            "slot" to ToolSchemaProperty(
                type = "INTEGER",
                description = "The target slot ID (1 to 10) of the existing tile to update. This is required."
            ),
            "title" to ToolSchemaProperty(
                type = "STRING",
                description = "Optional. New short name for the tile (max 15 chars)."
            ),
            "command" to ToolSchemaProperty(
                type = "STRING",
                description = "Optional. New shell command to run when the tile is ALREADY ON (to turn it OFF) or for tap actions."
            ),
            "icon_name" to ToolSchemaProperty(
                type = "STRING",
                description = "Optional. New icon identifier. Valid options: ${TileIconProvider.icons.joinToString { it.id }}"
            ),
            "execution_mode" to ToolSchemaProperty(
                type = "INTEGER",
                description = "Optional. New execution mode (0 for Shizuku, 1 for Root)."
            ),
            "is_toggleable" to ToolSchemaProperty(
                type = "BOOLEAN",
                description = "Optional. Whether the tile represents an on/off toggle (true) or a tap action (false)."
            ),
            "inactive_command" to ToolSchemaProperty(
                type = "STRING",
                description = "Optional. New shell command to run when the tile is ALREADY OFF (to turn it ON)."
            ),
            "active_subtitle" to ToolSchemaProperty(
                type = "STRING",
                description = "Optional. Subtitle text when tile is active/on."
            ),
            "inactive_subtitle" to ToolSchemaProperty(
                type = "STRING",
                description = "Optional. Subtitle text when tile is inactive/off."
            )
        ),
        required = listOf("slot")
    )

    override suspend fun execute(args: KJsonObject?): String {
        val slot = args?.get("slot")?.jsonPrimitive?.content?.toIntOrNull()
            ?: return "Error: slot is required and must be an integer (1-10)"

        val existingTile = repository.getTileOnce(slot)
            ?: return "Error: No tile exists in slot $slot."

        val title = args["title"]?.jsonPrimitive?.content ?: existingTile.name
        val command = args["command"]?.jsonPrimitive?.content ?: existingTile.activeState.activeCommand.text
        val iconName = args["icon_name"]?.jsonPrimitive?.content ?: existingTile.iconId

        val rawMode = args["execution_mode"]?.jsonPrimitive?.content?.toIntOrNull()
        val executionMode = if (rawMode != null) {
            if (rawMode == TileExecutionMode.ROOT) TileExecutionMode.ROOT else TileExecutionMode.SHIZUKU
        } else {
            existingTile.executionMode
        }

        val isToggleable = args["is_toggleable"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: existingTile.activeState.isToggleable
        val inactiveCmd = args["inactive_command"]?.jsonPrimitive?.content ?: existingTile.activeState.inactiveCommand.text
        val activeSubtitle = args["active_subtitle"]?.jsonPrimitive?.content ?: existingTile.activeState.activeTileSubtitle.text
        val inactiveSubtitle = args["inactive_subtitle"]?.jsonPrimitive?.content ?: existingTile.activeState.inactiveTileSubtitle.text

        val updatedTile = existingTile.copy(
            name = title,
            executionMode = executionMode,
            iconId = iconName,
            activeState = existingTile.activeState.copy(
                isToggleable = isToggleable,
                activeCommand = TextFieldValue(command),
                inactiveCommand = TextFieldValue(inactiveCmd),
                activeTileSubtitle = TextFieldValue(activeSubtitle),
                inactiveTileSubtitle = TextFieldValue(inactiveSubtitle)
            )
        )

        repository.updateTile(updatedTile)

        // Make sure the tile gets visually refreshed in the QS panel
        val slotIndex = slot - 1
        tileComponentManager.refreshTile(slotIndex)

        return "Successfully updated QS tile in Slot $slot."
    }
}
