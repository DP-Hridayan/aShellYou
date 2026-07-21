package `in`.hridayan.ashell.shell.wifi_adb_shell.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.database.WifiAdbDeviceDao
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.local.database.WifiAdbDeviceDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WifiAdbDeviceDatabaseModule {

    @Provides
    @Singleton
    fun provideWifiAdbDeviceDatabase(@ApplicationContext context: Context): WifiAdbDeviceDatabase {
        return Room.databaseBuilder(
            context,
            WifiAdbDeviceDatabase::class.java,
            "wifi_adb_device_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideWifiAdbDeviceDao(database: WifiAdbDeviceDatabase): WifiAdbDeviceDao {
        return database.wifiAdbDeviceDao()
    }
}
