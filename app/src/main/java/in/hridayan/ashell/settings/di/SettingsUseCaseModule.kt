package `in`.hridayan.ashell.settings.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.settings.domain.usecase.GetAllChangelogsUseCase

@Module
@InstallIn(SingletonComponent::class)
object SettingsUseCaseModule {
    @Provides
    fun provideGetChangelogsUseCase(@ApplicationContext context: Context): GetAllChangelogsUseCase =
        GetAllChangelogsUseCase(context)
}
