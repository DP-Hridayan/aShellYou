package `in`.hridayan.ashell.portforward.domain.usecase

import `in`.hridayan.ashell.portforward.domain.engine.TunnelController
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.repository.SavedTunnelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.runningFold
import javax.inject.Inject

/**
 * Saved tunnels that were running when the system stopped the app, and are not running now.
 *
 * Only an earlier process can have been stopped by the system, so a tunnel the engine has run in
 * this process never counts. Stopping one removes its runtime before the database clears its
 * running flag, and checking only the current runtimes would report it for that moment.
 */
class ObserveInterruptedTunnelsUseCase @Inject constructor(
    private val repository: SavedTunnelRepository,
    private val controller: TunnelController
) {
    operator fun invoke(): Flow<List<TunnelSpec>> = combine(
        repository.tunnels(),
        repository.wasRunningIds(),
        controller.runtimes.runningFold(emptySet<String>()) { seen, runtimes -> seen + runtimes.keys }
    ) { tunnels, wasRunning, runThisProcess ->
        tunnels.filter { it.id in wasRunning && it.id !in runThisProcess }
    }
}
