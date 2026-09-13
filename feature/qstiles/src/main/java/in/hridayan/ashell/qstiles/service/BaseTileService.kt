package `in`.hridayan.ashell.qstiles.service

import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.core.common.domain.model.TileExecutionMode
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuCommandRunner
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.domain.executor.TileExecutionManager
import `in`.hridayan.ashell.qstiles.domain.model.TileActiveState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.repository.MaterialIconRepository
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Base class for all 10 QS TileServices (Tile01Service – Tile10Service).
 *
 * Each concrete subclass owns a fixed [slotIndex] (0–9).  The service observes
 * whichever [TileConfig] is currently mapped to that slot.
 *
 * **Toggleable tiles**: clicking alternates the tile's [TileActiveState.isActive] flag
 * stored in DataStore and executes the appropriate command.
 *
 * **Static tiles**: clicking always executes [TileActiveState.activeCommand] without
 * changing the stored state.
 *
 * QS tiles **cannot** be programmatically removed from the panel.
 * An unmapped slot shows [Tile.STATE_UNAVAILABLE].
 */
@AndroidEntryPoint
abstract class BaseTileService : TileService() {

    @Inject
    lateinit var repository: TileRepository

    @Inject
    lateinit var executionManager: TileExecutionManager

    @Inject
    lateinit var materialIconRepository: MaterialIconRepository

    @Inject
    lateinit var shizukuCommandRunner: ShizukuCommandRunner

    /** Fixed index (0–9) that each concrete tile service owns. */
    abstract val slotIndex: Int

    private lateinit var serviceScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onStartListening() {
        super.onStartListening()
        serviceScope.launch {
            combine(
                repository.getTileBySlot(slotIndex),
                executionManager.runningTileStates,
            ) { config, running -> config to running }
                .collectLatest { (config, running) ->
                    if (config?.executionMode == TileExecutionMode.SHIZUKU) {
                        launch(Dispatchers.IO) {
                            shizukuCommandRunner.warmUp()
                        }
                    }

                    updateQsTile(config, running.containsKey(config?.id))
                }
        }
    }

    override fun onClick() {
        super.onClick()
        serviceScope.launch(Dispatchers.IO) {
            val config = repository.getTileBySlot(slotIndex).firstValue() ?: return@launch

            val success = executionManager.execute(config)

            if (success && config.activeState.isToggleable) {
                repository.toggleTile(config.id)
            }
        }
    }

    private suspend fun updateQsTile(config: TileConfig?, isRunning: Boolean) {
        val tile = qsTile ?: return

        if (config == null) {
            tile.apply {
                label = getString(R.string.tile_n, slotIndex + 1)
                icon = Icon.createWithResource(this@BaseTileService, R.drawable.ic_adb)
                state = Tile.STATE_UNAVAILABLE
                updateTile()
            }
            return
        }

        tile.apply {
            label = if (isRunning) "Running…" else config.name

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                subtitle = config.activeState.currentSubtitle
            }

            icon = TileIconProvider.iconById[config.iconId]
                ?.let { Icon.createWithResource(this@BaseTileService, it.resId) }
                ?: resolveCloudIcon(config.iconId)

            state = if (config.activeState.isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

            updateTile()
        }
    }

    private suspend fun resolveCloudIcon(iconId: String): Icon {
        val iconName = TileIconProvider.extractCloudIconName(iconId)

        materialIconRepository.getCachedIconBitmap(iconName)?.toIconOrNull()?.let { return it }

        val readyState = materialIconRepository.loadOrDownloadFont().getOrNull()

        val codepoint = readyState?.icons?.find { it.name == iconName }?.codepoint

        if (codepoint != null) {
            materialIconRepository.renderAndCacheIcon(iconName, codepoint).getOrNull()
                ?.toIconOrNull()?.let { return it }
        }

        return Icon.createWithResource(this@BaseTileService, R.drawable.ic_adb)
    }

    private fun File.toIconOrNull(): Icon? {
        if (!exists()) return null
        val bitmap = BitmapFactory.decodeFile(absolutePath) ?: return null
        return Icon.createWithBitmap(bitmap)
    }

    /** Collects a single emission from the flow and returns it. */
    private suspend fun <T> Flow<T>.firstValue(): T {
        var result: T? = null

        val job = serviceScope.launch {
            collect { v ->
                result = v
                cancel()
            }
        }

        job.join()

        @Suppress("UNCHECKED_CAST")
        return result as T
    }
}
