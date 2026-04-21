package `in`.hridayan.ashell.qstiles.domain.model

import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import kotlinx.serialization.Serializable

/**
 * Immutable configuration for a single Quick Settings tile slot.
 *
 * The tile's runtime behaviour (commands, subtitle, toggle logic) is fully contained
 * inside [activeState]; this class owns only the identity / display metadata.
 */
@Serializable
data class TileConfig(
    /** Stable tile identifier (1–10, matching its fixed slot index + 1). */
    val id: Int,
    /** Human-readable label shown in the QS panel and dashboard. */
    val name: String,
    /** Key into [TileIconProvider] for resolving the drawable. */
    val iconId: String,
    /** Execution mode constant from [TileExecutionMode]. */
    val executionMode: Int,
    /** Full behaviour configuration (commands, state, subtitles). */
    val activeState: TileActiveState,
    /** True for user-created tiles; false for built-in / placeholder tiles. */
    val isCustom: Boolean,
    /** Fixed QS panel slot (0–9). Null means the tile is not yet placed in a slot. */
    val slotIndex: Int? = null,
    /** Optional per-tile command timeout in milliseconds. Null = use global default. */
    val timeoutMs: Long? = null,
)