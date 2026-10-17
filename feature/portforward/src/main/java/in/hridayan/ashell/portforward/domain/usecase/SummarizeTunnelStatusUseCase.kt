package `in`.hridayan.ashell.portforward.domain.usecase

import `in`.hridayan.ashell.core.common.domain.provider.TunnelStatusSummary
import `in`.hridayan.ashell.portforward.domain.model.TunnelRuntime
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.isRunning
import `in`.hridayan.ashell.portforward.domain.model.needsAttention
import javax.inject.Inject

/**
 * Condenses tunnel state into the summary other features show.
 *
 * A tunnel marked as running that has no runtime was stopped by the system, which also needs the
 * user's attention.
 */
class SummarizeTunnelStatusUseCase @Inject constructor() {

    operator fun invoke(runtimes: Map<String, TunnelRuntime>, wasRunningIds: Set<String>): TunnelStatusSummary {
        val running = runtimes.values.filter { it.state.isRunning }
        val interrupted = wasRunningIds.any { it !in runtimes }
        return TunnelStatusSummary(
            runningCount = running.size,
            needsAttention = interrupted || runtimes.values.any { it.state.needsAttention },
            runningNames = running.map { it.spec.name },
            runningByTransport = running.mapNotNull { (it.spec.target as? TunnelTarget.Device)?.transport }
                .groupingBy { it }
                .eachCount()
        )
    }
}
