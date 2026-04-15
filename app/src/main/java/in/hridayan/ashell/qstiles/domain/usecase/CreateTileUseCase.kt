package `in`.hridayan.ashell.qstiles.domain.usecase

import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.processor.TileIconMatcher
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository

class CreateTileUseCase(
    private val repository: TileRepository,
    private val matcher: TileIconMatcher
) {

    suspend operator fun invoke(
        name: String,
        command: String,
        executionMode: Int,
        existing: List<TileConfig>
    ): Result<Unit> {

        val nextId = (1..10).firstOrNull { id ->
            existing.none { it.id == id }
        } ?: return Result.failure(Exception("Max tiles reached"))

        val iconId = matcher.suggestIcons(command).firstOrNull() ?: "terminal"

        val tile = TileConfig(
            id = nextId,
            name = name,
            command = command,
            executionMode = executionMode,
            iconId = iconId,
            isActive = true,
            isCustom = true
        )

        repository.createTile(tile)
        return Result.success(Unit)
    }
}