package `in`.hridayan.ashell.ai.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.ai.data.local.database.AiCacheDao
import `in`.hridayan.ashell.ai.data.local.database.AiCacheDatabase
import `in`.hridayan.ashell.ai.data.repository.AiAnalysisRepositoryImpl
import `in`.hridayan.ashell.ai.data.repository.AiModelRepositoryImpl
import `in`.hridayan.ashell.ai.data.repository.LlamaInferenceEngine
import `in`.hridayan.ashell.ai.domain.repository.AiAnalysisRepository
import `in`.hridayan.ashell.ai.domain.repository.AiModelRepository
import `in`.hridayan.ashell.ai.domain.usecase.AnalyzeCommandUseCase
import `in`.hridayan.ashell.ai.domain.usecase.DetectDangerLevelUseCase
import `in`.hridayan.ashell.ai.domain.usecase.GenerateCorrectionsUseCase
import `in`.hridayan.ashell.ai.domain.usecase.GetCachedAnalysisUseCase
import `in`.hridayan.ashell.commandexamples.domain.repository.CommandRepository
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import javax.inject.Singleton

/**
 * Hilt module providing all AI feature dependencies.
 * Self-contained — does not modify existing DI modules.
 */
@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideAiCacheDatabase(@ApplicationContext context: Context): AiCacheDatabase {
        return Room.databaseBuilder(
            context,
            AiCacheDatabase::class.java,
            "ai_cache_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideAiCacheDao(database: AiCacheDatabase): AiCacheDao {
        return database.aiCacheDao()
    }

    @Provides
    @Singleton
    fun provideAiAnalysisRepository(
        cacheDao: AiCacheDao,
        inferenceEngine: LlamaInferenceEngine,
        settingsRepository: SettingsRepository,
        @ApplicationContext context: Context
    ): AiAnalysisRepository {
        return AiAnalysisRepositoryImpl(cacheDao, inferenceEngine, settingsRepository, context)
    }

    @Provides
    @Singleton
    fun provideAiModelRepository(
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository
    ): AiModelRepository {
        return AiModelRepositoryImpl(context, settingsRepository)
    }

    @Provides
    fun provideDetectDangerLevelUseCase(): DetectDangerLevelUseCase {
        return DetectDangerLevelUseCase()
    }

    @Provides
    fun provideAnalyzeCommandUseCase(
        analysisRepository: AiAnalysisRepository,
        modelRepository: AiModelRepository
    ): AnalyzeCommandUseCase {
        return AnalyzeCommandUseCase(
            analysisRepository, modelRepository
        )
    }

    @Provides
    fun provideGenerateCorrectionsUseCase(
        commandRepository: CommandRepository
    ): GenerateCorrectionsUseCase {
        return GenerateCorrectionsUseCase(commandRepository)
    }

    @Provides
    fun provideGetCachedAnalysisUseCase(
        analysisRepository: AiAnalysisRepository
    ): GetCachedAnalysisUseCase {
        return GetCachedAnalysisUseCase(analysisRepository)
    }
}
