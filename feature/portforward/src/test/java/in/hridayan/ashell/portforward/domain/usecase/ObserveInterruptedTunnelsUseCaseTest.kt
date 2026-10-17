package `in`.hridayan.ashell.portforward.domain.usecase

import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.runtime
import `in`.hridayan.ashell.portforward.domain.spec
import `in`.hridayan.ashell.portforward.presentation.viewmodel.FakeSavedTunnelRepository
import `in`.hridayan.ashell.portforward.presentation.viewmodel.FakeTunnelController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveInterruptedTunnelsUseCaseTest {

    private val repository = FakeSavedTunnelRepository()
    private val controller = FakeTunnelController()
    private val tunnel = spec("a")

    @Test
    fun `a tunnel left running by an earlier process is interrupted`() = runTest {
        repository.saved.value = listOf(tunnel)
        repository.wasRunning.value = setOf(tunnel.id)

        val seen = observe()

        assertEquals(listOf(tunnel), seen.last())
    }

    @Test
    fun `stopping a tunnel never reports it as interrupted`() = runTest {
        repository.saved.value = listOf(tunnel)
        val seen = observe()

        controller.runtimeMap.value = mapOf(tunnel.id to runtime(tunnel))
        repository.wasRunning.value = setOf(tunnel.id)
        // The engine drops the runtime before the database clears the running flag.
        controller.runtimeMap.value = emptyMap()
        repository.wasRunning.value = emptySet()

        assertTrue(seen.all { it.isEmpty() })
    }

    private fun kotlinx.coroutines.test.TestScope.observe(): List<List<TunnelSpec>> {
        val seen = mutableListOf<List<TunnelSpec>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            ObserveInterruptedTunnelsUseCase(repository, controller)().collect { seen += it }
        }
        return seen
    }
}
