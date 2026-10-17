package `in`.hridayan.ashell.portforward.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.core.common.domain.provider.BackupProvider
import `in`.hridayan.ashell.core.common.domain.provider.DeviceTunnelControl
import `in`.hridayan.ashell.core.common.domain.provider.TunnelStatusProvider
import `in`.hridayan.ashell.portforward.data.backup.PortForwardBackupProvider
import `in`.hridayan.ashell.portforward.data.device.DeviceChannels
import `in`.hridayan.ashell.portforward.data.device.DeviceTunnelControlImpl
import `in`.hridayan.ashell.portforward.data.engine.RoutingTargetConnector
import `in`.hridayan.ashell.portforward.data.engine.TargetConnector
import `in`.hridayan.ashell.portforward.data.engine.TunnelEngine
import `in`.hridayan.ashell.portforward.data.local.PortForwardDatabase
import `in`.hridayan.ashell.portforward.data.network.ConnectivityNetworkMonitor
import `in`.hridayan.ashell.portforward.data.notification.TunnelAlertNotifierImpl
import `in`.hridayan.ashell.portforward.data.permission.LocalNetworkGateImpl
import `in`.hridayan.ashell.portforward.data.probe.PortAvailabilityImpl
import `in`.hridayan.ashell.portforward.data.probe.TunnelTesterImpl
import `in`.hridayan.ashell.portforward.data.repository.SavedTunnelRepositoryImpl
import `in`.hridayan.ashell.portforward.data.session.PortForwardForegroundSessionImpl
import `in`.hridayan.ashell.portforward.data.settings.PortForwardSettingsSourceImpl
import `in`.hridayan.ashell.portforward.data.status.TunnelStatusProviderImpl
import `in`.hridayan.ashell.portforward.domain.engine.TunnelController
import `in`.hridayan.ashell.portforward.domain.notification.TunnelAlertNotifier
import `in`.hridayan.ashell.portforward.domain.repository.ConnectedDevices
import `in`.hridayan.ashell.portforward.domain.repository.LocalNetworkGate
import `in`.hridayan.ashell.portforward.domain.repository.NetworkMonitor
import `in`.hridayan.ashell.portforward.domain.repository.PortAvailability
import `in`.hridayan.ashell.portforward.domain.repository.PortForwardSettingsSource
import `in`.hridayan.ashell.portforward.domain.repository.SavedTunnelRepository
import `in`.hridayan.ashell.portforward.domain.repository.TunnelTester
import `in`.hridayan.ashell.portforward.domain.session.PortForwardForegroundSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PortForwardBindingsModule {

    @Binds
    @Singleton
    abstract fun bindTunnelController(impl: TunnelEngine): TunnelController

    @Binds
    @Singleton
    abstract fun bindTunnelStatusProvider(impl: TunnelStatusProviderImpl): TunnelStatusProvider

    @Binds
    abstract fun bindTargetConnector(impl: RoutingTargetConnector): TargetConnector

    @Binds
    abstract fun bindSavedTunnelRepository(impl: SavedTunnelRepositoryImpl): SavedTunnelRepository

    @Binds
    abstract fun bindTunnelTester(impl: TunnelTesterImpl): TunnelTester

    @Binds
    abstract fun bindPortAvailability(impl: PortAvailabilityImpl): PortAvailability

    @Binds
    abstract fun bindLocalNetworkGate(impl: LocalNetworkGateImpl): LocalNetworkGate

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor

    @Binds
    abstract fun bindConnectedDevices(impl: DeviceChannels): ConnectedDevices

    @Binds
    abstract fun bindDeviceTunnelControl(impl: DeviceTunnelControlImpl): DeviceTunnelControl

    @Binds
    abstract fun bindSettingsSource(impl: PortForwardSettingsSourceImpl): PortForwardSettingsSource

    @Binds
    abstract fun bindForegroundSession(impl: PortForwardForegroundSessionImpl): PortForwardForegroundSession

    @Binds
    abstract fun bindAlertNotifier(impl: TunnelAlertNotifierImpl): TunnelAlertNotifier

    @Binds
    @IntoSet
    abstract fun bindBackupProvider(impl: PortForwardBackupProvider): BackupProvider
}

@Module
@InstallIn(SingletonComponent::class)
object PortForwardProvidersModule {

    private const val MAX_PARALLEL_SOCKET_OPERATIONS = 256

    @Provides
    @Singleton
    @PortForwardDispatcher
    fun providePortForwardDispatcher(): CoroutineDispatcher =
        Dispatchers.IO.limitedParallelism(MAX_PARALLEL_SOCKET_OPERATIONS)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PortForwardDatabase =
        Room.databaseBuilder(context, PortForwardDatabase::class.java, PortForwardDatabase.NAME)
            .addMigrations(PortForwardDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(true)
            .build()
}
