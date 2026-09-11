package `in`.hridayan.ashell.core.shizuku.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.shizuku.data.LegacyProcessStarter
import `in`.hridayan.ashell.core.shizuku.data.RemoteProcessFactory
import `in`.hridayan.ashell.core.shizuku.data.ShizukuCommandRunnerImpl
import `in`.hridayan.ashell.core.shizuku.data.ShizukuGateway
import `in`.hridayan.ashell.core.shizuku.data.ShizukuGatewayImpl
import `in`.hridayan.ashell.core.shizuku.data.ShizukuLegacyProcessStarter
import `in`.hridayan.ashell.core.shizuku.data.ShizukuRemoteProcessFactory
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuCommandRunner
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShizukuModule {

    @Binds
    @Singleton
    abstract fun bindShizukuGateway(impl: ShizukuGatewayImpl): ShizukuGateway

    @Binds
    @Singleton
    abstract fun bindShizukuCommandRunner(impl: ShizukuCommandRunnerImpl): ShizukuCommandRunner

    @Binds
    @Singleton
    abstract fun bindLegacyProcessStarter(impl: ShizukuLegacyProcessStarter): LegacyProcessStarter

    companion object {
        @Provides
        @Singleton
        fun provideRemoteProcessFactory(): RemoteProcessFactory = ShizukuRemoteProcessFactory()
    }
}
