package `in`.hridayan.ashell.qstiles.domain.usecase

import `in`.hridayan.ashell.qstiles.domain.model.TileActiveState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.processor.TileIconMatcher
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository

/**
 * Creates a new [TileConfig] with the next available slot and persists it.
 *
 * Returns [Result.failure] if all 10 slots are already occupied.
 */
class CreateTileUseCase(
    private val repository: TileRepository,
    private val matcher: TileIconMatcher,
) {

    suspend operator fun invoke(
        name: String,
        activeState: TileActiveState,
        executionMode: Int,
        existing: List<TileConfig>,
    ): Result<Unit> {

        val nextId = (1..10).firstOrNull { id -> existing.none { it.id == id } }
            ?: return Result.failure(Exception("Max tiles reached"))

        val iconId = matcher.suggestIcons(activeState.activeCommand).firstOrNull() ?: "terminal"

        val tile = TileConfig(
            id = nextId,
            name = name,
            executionMode = executionMode,
            iconId = iconId,
            isCustom = true,
            activeState = activeState,
        )

        repository.createTile(tile)
        return Result.success(Unit)
    }
}