package `in`.hridayan.ashell.portforward.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * A saved tunnel. Target columns are flat so each tunnel kind uses its own without a separate
 * table; [kind] says which columns apply. Columns added after version 1 default to null, so older
 * backups still decode.
 *
 * @property wasRunning set while the tunnel runs and cleared when it is stopped, so a tunnel still
 * marked after a restart of the app was stopped by the system.
 */
@Serializable
@Entity(tableName = "saved_tunnels")
data class SavedTunnelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val kind: String,
    val localPort: Int,
    val shareOnNetwork: Boolean,
    val targetHost: String?,
    val targetPort: Int?,
    val targetPath: String?,
    val autoStart: Boolean,
    val looksLikeHttp: Boolean,
    val wasRunning: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val transport: String? = null,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val endpointType: String? = null,
    val endpointValue: String? = null
)

@Serializable
@Entity(tableName = "recent_hosts")
data class RecentHostEntity(
    @PrimaryKey val host: String,
    val lastPort: Int,
    val lastUsedAt: Long
)
