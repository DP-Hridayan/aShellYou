package `in`.hridayan.ashell.ai.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.ai.data.repository.ChatRepositoryImpl
import `in`.hridayan.ashell.ai.data.repository.CommandPermissionRepositoryImpl
import `in`.hridayan.ashell.ai.domain.repository.ChatRepository
import `in`.hridayan.ashell.ai.domain.repository.CommandPermissionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindCommandPermissionRepository(
        commandPermissionRepositoryImpl: CommandPermissionRepositoryImpl
    ): CommandPermissionRepository
}
