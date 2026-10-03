package `in`.hridayan.ashell.mirror.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.common.data.provider.DispatcherProvider
import `in`.hridayan.ashell.mirror.data.repository.MirrorRepositoryImpl
import `in`.hridayan.ashell.mirror.data.server.ScrcpyServerArtifact
import `in`.hridayan.ashell.mirror.data.server.ScrcpyServerDeployer
import `in`.hridayan.ashell.mirror.domain.protocol.ServerCommandBuilder
import `in`.hridayan.ashell.mirror.domain.repository.MirrorRepository

@Module
@InstallIn(SingletonComponent::class)
object MirrorModule {

    @Provides
    fun provideServerCommandBuilder(): ServerCommandBuilder = ServerCommandBuilder(
        serverPath = ScrcpyServerArtifact.REMOTE_PATH,
        serverVersion = ScrcpyServerArtifact.VERSION
    )

    @Provides
    fun provideScrcpyServerDeployer(
        @ApplicationContext context: Context,
        dispatchers: DispatcherProvider
    ): ScrcpyServerDeployer = ScrcpyServerDeployer(
        jarSource = { context.assets.open(ScrcpyServerArtifact.ASSET_NAME).use { it.readBytes() } },
        remotePath = ScrcpyServerArtifact.REMOTE_PATH,
        remotePrefix = ScrcpyServerArtifact.REMOTE_PREFIX,
        ioDispatcher = dispatchers.io
    )
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class MirrorRepositoryModule {

    @Binds
    @ViewModelScoped
    abstract fun bindMirrorRepository(impl: MirrorRepositoryImpl): MirrorRepository
}
