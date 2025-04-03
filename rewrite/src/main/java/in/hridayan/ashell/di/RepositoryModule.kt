package `in`.hridayan.ashell.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.data.local.repository.command_examples.CommandRepositoryImpl
import `in`.hridayan.ashell.data.local.repository.command_examples.LabelRepositoryImpl
import `in`.hridayan.ashell.domain.repository.command_examples.CommandRepository
import `in`.hridayan.ashell.domain.repository.command_examples.LabelRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract  fun bindCommandRepository(
        commandRepositoryImpl: CommandRepositoryImpl
    ): CommandRepository
    @Binds
    abstract  fun bindLabelRepository(
        labelRepositoryImpl: LabelRepositoryImpl
    ): LabelRepository
}