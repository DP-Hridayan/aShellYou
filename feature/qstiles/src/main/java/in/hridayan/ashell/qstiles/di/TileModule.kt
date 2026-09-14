package `in`.hridayan.ashell.qstiles.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.qstiles.data.executor.RootExecutor
import `in`.hridayan.ashell.qstiles.data.executor.ShizukuExecutor
import `in`.hridayan.ashell.qstiles.data.provider.TileNotificationHelper
import `in`.hridayan.ashell.qstiles.data.repository.TileLogRepositoryImpl
import `in`.hridayan.ashell.qstiles.data.repository.TileRepositoryImpl
import `in`.hridayan.ashell.qstiles.domain.executor.CommandExecutor
import `in`.hridayan.ashell.qstiles.domain.executor.TileExecutionManager
import `in`.hridayan.ashell.qstiles.domain.repository.TileLogRepository
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TileModule {

    @Provides
    @Singleton
    fun provideTileRepository(impl: TileRepositoryImpl): TileRepository = impl

    @Provides
    @Singleton
    fun provideTileLogRepository(impl: TileLogRepositoryImpl): TileLogRepository = impl

    @Provides
    @Singleton
    @Named("shizuku")
    fun provideShizukuExecutor(impl: ShizukuExecutor): CommandExecutor = impl

    @Provides
    @Singleton
    @Named("root")
    fun provideRootExecutor(impl: RootExecutor): CommandExecutor = impl

    @Provides
    @Singleton
    fun provideTileNotificationHelper(
        @ApplicationContext context: Context
    ): TileNotificationHelper = TileNotificationHelper(context)

    @Provides
    @Singleton
    fun provideTileExecutionManager(
        @Named("shizuku") shizukuExecutor: CommandExecutor,
        @Named("root") rootExecutor: CommandExecutor,
        logRepository: TileLogRepository,
        notificationHelper: TileNotificationHelper
    ): TileExecutionManager = TileExecutionManager(
        shizukuExecutor = shizukuExecutor,
        rootExecutor = rootExecutor,
        logRepository = logRepository,
        notificationHelper = notificationHelper
    )
}
