package `in`.hridayan.ashell.qstiles.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.domain.executor.TileExecutionManager
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Base class for all 10 QS TileServices (Tile01Service – Tile10Service).
 *
 * Each concrete subclass declares a fixed [slotIndex] (0–9).
 * The service looks up whichever [TileConfig] is currently mapped to that slot.
 *
 * QS tiles CANNOT be programmatically removed from the panel.
 * Instead, an unmapped slot shows STATE_UNAVAILABLE.
 */
@AndroidEntryPoint
abstract class BaseTileService : TileService() {

    @Inject
    lateinit var repository: TileRepository

    @Inject
    lateinit var executionManager: TileExecutionManager

    /** Fixed index (0–9) that each concrete tile service owns. */
    abstract val slotIndex: Int

    /**
     * Coroutine scope tied to this service instance.
     * SupervisorJob ensures a failed child does not cancel the sibling observers.
     */
    private lateinit var serviceScope: CoroutineScope

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    // ─── Listening ────────────────────────────────────────────────────────────

    override fun onStartListening() {
        super.onStartListening()
        serviceScope.launch {
            combine(
                repository.getTileBySlot(slotIndex),
                executionManager.runningTileStates
            ) { config, running -> config to running }
                .collectLatest { (config, running) ->
                    updateQsTile(config, running.containsKey(config?.id))
                }
        }
    }

    // ─── Click ────────────────────────────────────────────────────────────────

    override fun onClick() {
        super.onClick()
        serviceScope.launch(Dispatchers.IO) {
            // Single one-shot read — avoids a long-lived collector on the UI thread
            val config = repository.getTileBySlot(slotIndex)
                .let {
                    // Collect first value non-blocking
                    var result: TileConfig? = null
                    val job = launch { it.collect { v -> result = v; cancel() } }
                    job.join()
                    result
                }

            config ?: return@launch  // No tile mapped to this slot — ignore

            executionManager.execute(config)
        }
    }

    // ─── QS Panel Update ─────────────────────────────────────────────────────

    private fun updateQsTile(config: TileConfig?, isRunning: Boolean) {
        val tile = qsTile ?: return

        tile.apply {
            if (config == null) {
                // No tile assigned to this slot — show as unavailable
                label = "Empty"
                state = Tile.STATE_UNAVAILABLE
                icon = android.graphics.drawable.Icon.createWithResource(
                    this@BaseTileService,
                    R.drawable.ic_adb2
                )
            } else {
                label = if (isRunning) "Running..." else config.name

                val iconRes = TileIconProvider.iconById[config.iconId]
                icon = android.graphics.drawable.Icon.createWithResource(
                    this@BaseTileService,
                    iconRes?.resId ?: R.drawable.ic_adb2
                )

                state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_ACTIVE
            }

            updateTile()
        }
    }
}