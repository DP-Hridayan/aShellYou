package `in`.hridayan.ashell.qstiles.data.provider

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.qstiles.service.tiles.*
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
     * Performs a Disable-then-Enable cycle to force the system to remove
     * the tile from the active QS panel and move it to the tray.
     * 
     * If the system remembers the position, re-enabling it might put it 
     * back in some Android versions, but it's the standard way to 'kick'
     * it out of the active panel state.
     */
    fun kickComponent(slotIndex: Int) {
        setComponentEnabled(slotIndex, false)
        // A very short delay isn't possible here synchronously without blocking, 
        // but PackageManager operations are generally processed sequentially by the system.
        setComponentEnabled(slotIndex, true)
    }
    
    /**
     * Prompts the user to add the tile to their active panel (Android 13+).
     * This fulfills the 'automatically reappear' requirement by showing a system dialog.
     */
    fun promptAddTile(slotIndex: Int, label: String, iconResId: Int) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            try {
                val statusBarManager = context.getSystemService(android.app.StatusBarManager::class.java)
                val componentName = ComponentName(packageName, tileServices[slotIndex].qualifiedName!!)
                val icon = android.graphics.drawable.Icon.createWithResource(context, iconResId)

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

