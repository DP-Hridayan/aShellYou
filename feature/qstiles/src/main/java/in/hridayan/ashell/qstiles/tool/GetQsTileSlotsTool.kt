package `in`.hridayan.ashell.qstiles.tool

import `in`.hridayan.ashell.core.common.domain.model.TileExecutionMode
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchema
import `in`.hridayan.ashell.core.common.domain.model.ai.ToolSchemaType
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonObject as KJsonObject

@Singleton
class GetQsTileSlotsTool @Inject constructor(
    private val repository: TileRepository
) : AiTool {

    override val name: String = "get_qs_tile_slots"

    override val description: String = "Query the current status of all 10 Quick Settings (QS) tile slots (1 to 10), showing which slots are available (empty) and which slots are occupied by existing custom tiles. ALWAYS call this before creating a new tile to see if there is an empty slot and know which slot number to use."

    override val parametersSchema: ToolSchema = ToolSchema(
        type = ToolSchemaType.OBJECT,
        properties = emptyMap(),
        required = emptyList()
    )

    override suspend fun execute(args: KJsonObject?): String {
        val existingTiles = repository.getTiles().first()
        val occupiedIds = existingTiles.map { it.id }.toSet()
        val availableIds = (1..10).filter { it !in occupiedIds }

        val sb = StringBuilder()
        sb.appendLine("Total QS Tile Slots: 10 (Slots 1 to 10)")
        if (availableIds.isNotEmpty()) {
            sb.appendLine("Available Empty Slots: ${availableIds.joinToString(", ")}")
        } else {
            sb.appendLine("Available Empty Slots: None (All 10 slots are occupied)")
        }
        sb.appendLine("Occupied Slots (${existingTiles.size}):")
        if (existingTiles.isEmpty()) {
            sb.appendLine("  None (All 10 slots are empty and ready to use)")
        } else {
            existingTiles.sortedBy { it.id }.forEach { tile ->
                val modeStr = when (tile.executionMode) {
                    TileExecutionMode.ROOT -> "Root"
                    else -> "Shizuku"
                }
                val typeStr = if (tile.activeState.isToggleable) {
                    "Toggle (Cmd to turn OFF: '${tile.activeState.activeCommand.text}', Cmd to turn ON: '${tile.activeState.inactiveCommand.text}')"
                } else {
                    "Tap Action ('${tile.activeState.activeCommand.text}')"
                }
                sb.appendLine("  - Slot ${tile.id}: '${tile.name}' [Mode: $modeStr] ($typeStr)")
            }
        }
        return sb.toString().trimEnd()
    }
}
