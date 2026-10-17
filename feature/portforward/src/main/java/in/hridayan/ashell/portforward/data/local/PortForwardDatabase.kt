package `in`.hridayan.ashell.portforward.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SavedTunnelEntity::class, RecentHostEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PortForwardDatabase : RoomDatabase() {
    abstract fun savedTunnelDao(): SavedTunnelDao
    abstract fun recentHostDao(): RecentHostDao

    companion object {
        const val NAME = "port_forward.db"

        /** Adds the columns of device tunnels. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                DEVICE_COLUMNS.forEach { column ->
                    db.execSQL("ALTER TABLE saved_tunnels ADD COLUMN $column TEXT")
                }
            }
        }

        private val DEVICE_COLUMNS = listOf("transport", "deviceId", "deviceName", "endpointType", "endpointValue")
    }
}
