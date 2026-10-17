package `in`.hridayan.ashell.portforward.domain.usecase

import `in`.hridayan.ashell.portforward.domain.engine.TunnelController
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.repository.SavedTunnelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Saved tunnels that were running when the system stopped the app, and are not running now. */
class ObserveInterruptedTunnelsUseCase @Inject constructor(
    private val repository: SavedTunnelRepository,
    private val controller: TunnelController
) {
    operator fun invoke(): Flow<List<TunnelSpec>> = combine(
        repository.tunnels(),
        repository.wasRunningIds(),
        controller.runtimes
    ) { tunnels, wasRunning, runtimes ->
        tunnels.filter { it.id in wasRunning && it.id !in runtimes }
    }
}
