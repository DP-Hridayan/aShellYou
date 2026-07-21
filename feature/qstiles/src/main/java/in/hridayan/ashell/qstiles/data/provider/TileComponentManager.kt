package `in`.hridayan.ashell.qstiles.data.provider

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.qstiles.service.tiles.Tile01Service
import `in`.hridayan.ashell.qstiles.service.tiles.Tile02Service
import `in`.hridayan.ashell.qstiles.service.tiles.Tile03Service
import `in`.hridayan.ashell.qstiles.service.tiles.Tile04Service
import `in`.hridayan.ashell.qstiles.service.tiles.Tile05Service
import `in`.hridayan.ashell.qstiles.service.tiles.Tile06Service
import `in`.hridayan.ashell.qstiles.service.tiles.Tile07Service
import `in`.hridayan.ashell.qstiles.service.tiles.Tile08Service
import `in`.hridayan.ashell.qstiles.service.tiles.Tile09Service
import `in`.hridayan.ashell.qstiles.service.tiles.Tile10Service
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TileComponentManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val packageManager = context.packageManager
    private val packageName = context.packageName

    private val tileServices = listOf(
        Tile01Service::class,
        Tile02Service::class,
        Tile03Service::class,
        Tile04Service::class,
        Tile05Service::class,
        Tile06Service::class,
        Tile07Service::class,
        Tile08Service::class,
        Tile09Service::class,
        Tile10Service::class
    )

    /**
     * Toggles the enabled state of a TileService component.
     */
    fun setComponentEnabled(slotIndex: Int, enabled: Boolean) {
        if (slotIndex !in tileServices.indices) return

        val componentName = ComponentName(packageName, tileServices[slotIndex].qualifiedName!!)
        val newState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        packageManager.setComponentEnabledSetting(
            componentName,
            newState,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * Prompts the user to add the tile to their active panel (Android 13+).
     * This fulfills the 'automatically reappear' requirement by showing a system dialog.
     */
    fun promptAddTile(slotIndex: Int, label: String, iconResId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val statusBarManager =
                    context.getSystemService(StatusBarManager::class.java)
                val componentName =
                    ComponentName(packageName, tileServices[slotIndex].qualifiedName!!)
                val icon = Icon.createWithResource(context, iconResId)

                statusBarManager?.requestAddTileService(
                    componentName,
                    label,
                    icon,
                    context.mainExecutor,
                ) { result ->
                    // Handle result if needed (e.g. log)
                }
            } catch (e: Exception) {
                // Ignore errors from system manager
            }
        }
    }

    /**
     * Ensures all components are enabled (tray-ready).
     */
    fun ensureAllEnabled() {
        tileServices.indices.forEach { setComponentEnabled(it, true) }
    }
}

