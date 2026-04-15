package `in`.hridayan.ashell.qstiles.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.model.TileExecutionMode
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseTileService : TileService() {

    @Inject
    lateinit var repository: TileRepository

    abstract val tileId: Int

    override fun onClick() {
        super.onClick()

        CoroutineScope(Dispatchers.IO).launch {
            repository.getTile(tileId).collect { config ->
                config ?: return@collect

                execute(config)
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()

        CoroutineScope(Dispatchers.IO).launch {
            repository.getTile(tileId).collect { config ->

                val tile = qsTile ?: return@collect

                tile.apply {
                    label = config?.name ?: "Empty"

                    val iconFromProvider = TileIconProvider.iconById[config?.iconId]
                    if (iconFromProvider != null) {
                        icon = android.graphics.drawable.Icon.createWithResource(
                            this@BaseTileService,
                            iconFromProvider.resId
                        )
                    } else {
                        icon = android.graphics.drawable.Icon.createWithResource(
                            this@BaseTileService,
                            R.drawable.ts_add
                        )
                    }



                    state = if (config != null) {
                        Tile.STATE_ACTIVE
                    } else {
                       Tile.STATE_INACTIVE
                    }

                    updateTile()
                }
            }
        }
    }

    private fun execute(config: TileConfig) {
        when (config.executionMode) {
            TileExecutionMode.SHIZUKU -> {
                // run via shizuku
            }

            TileExecutionMode.ROOT -> {
                // run via root
            }
        }
    }
}