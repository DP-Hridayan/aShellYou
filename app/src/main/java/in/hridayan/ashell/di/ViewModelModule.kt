package `in`.hridayan.ashell.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import `in`.hridayan.ashell.domain.repository.command_examples.CommandRepository
import `in`.hridayan.ashell.presentation.viewmodel.CommandViewModel

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideCommandViewModel(
        commandRepository: CommandRepository
    ): CommandViewModel {
        return CommandViewModel(commandRepository)
    }
}

