package `in`.hridayan.ashell.portforward.data.status

import `in`.hridayan.ashell.core.common.domain.provider.TunnelStatusProvider
import `in`.hridayan.ashell.core.common.domain.provider.TunnelStatusSummary
import `in`.hridayan.ashell.portforward.domain.engine.TunnelController
import `in`.hridayan.ashell.portforward.domain.repository.SavedTunnelRepository
import `in`.hridayan.ashell.portforward.domain.usecase.SummarizeTunnelStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TunnelStatusProviderImpl @Inject constructor(
    controller: TunnelController,
    repository: SavedTunnelRepository,
    summarize: SummarizeTunnelStatusUseCase
) : TunnelStatusProvider {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val summary: StateFlow<TunnelStatusSummary> =
        combine(controller.runtimes, repository.wasRunningIds(), summarize::invoke)
            .stateIn(scope, SharingStarted.Eagerly, TunnelStatusSummary())
}
