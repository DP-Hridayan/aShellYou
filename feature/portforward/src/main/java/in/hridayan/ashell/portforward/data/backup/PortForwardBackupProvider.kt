package `in`.hridayan.ashell.portforward.data.backup

import android.util.Log
import `in`.hridayan.ashell.core.common.domain.provider.BackupProvider
import `in`.hridayan.ashell.portforward.data.local.PortForwardDatabase
import `in`.hridayan.ashell.portforward.data.local.RecentHostEntity
import `in`.hridayan.ashell.portforward.data.local.SavedTunnelEntity
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import javax.inject.Inject

@Serializable
data class PortForwardBackupModel(
    val tunnels: List<SavedTunnelEntity>,
    val recentHosts: List<RecentHostEntity>
)

/**
 * Backs up saved tunnels and recent hosts.
 *
 * Whether a tunnel was running is device state, not a preference, so it is never restored: a
 * restored tunnel is always stopped.
 */
class PortForwardBackupProvider @Inject constructor(
    private val database: PortForwardDatabase,
    private val json: Json
) : BackupProvider {

    override val featureId: String = "port_forward"

    override suspend fun getBackupData(): JsonElement? {
        val tunnels = database.savedTunnelDao().getAll().map { it.copy(wasRunning = false) }
        val hosts = database.recentHostDao().getAll()
        if (tunnels.isEmpty() && hosts.isEmpty()) return null
        return json.encodeToJsonElement(PortForwardBackupModel(tunnels, hosts))
    }

    override suspend fun restoreData(data: JsonElement?, legacyData: (String) -> JsonElement?) {
        val element = data ?: return
        try {
            restore(json.decodeFromJsonElement<PortForwardBackupModel>(element))
        } catch (e: CancellationException) {
            throw e
        } catch (e: SerializationException) {
            Log.e(TAG, "Port forwarding backup is unreadable", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Port forwarding backup is unreadable", e)
        }
    }

    private suspend fun restore(model: PortForwardBackupModel) {
        if (model.tunnels.isNotEmpty()) {
            database.savedTunnelDao().deleteAll()
            database.savedTunnelDao().upsertAll(model.tunnels.map { it.copy(wasRunning = false) })
        }
        if (model.recentHosts.isNotEmpty()) {
            database.recentHostDao().deleteAll()
            database.recentHostDao().upsertAll(model.recentHosts)
        }
    }

    private companion object {
        const val TAG = "PortForwardBackup"
    }
}
