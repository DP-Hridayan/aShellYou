package `in`.hridayan.ashell.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.settings.data.datastore.SettingsDataStore
import `in`.hridayan.ashell.settings.data.repository.SettingsRepositoryImpl
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import `in`.hridayan.ashell.settings.domain.usecase.ToggleSettingUseCase
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Provides
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore =
        SettingsDataStore(context)

    @Provides
    fun provideSettingsRepository(dataStore: SettingsDataStore): SettingsRepository =
        SettingsRepositoryImpl(dataStore)

    @Provides
    fun provideToggleSettingUseCase(repo: SettingsRepository): ToggleSettingUseCase =
        ToggleSettingUseCase(repo)

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }
}