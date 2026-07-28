package `in`.hridayan.ashell.ai.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.ai.data.local.database.AiCacheDao
import `in`.hridayan.ashell.ai.data.local.database.AiDatabase
import `in`.hridayan.ashell.ai.data.local.database.dao.ChatDao
import `in`.hridayan.ashell.ai.data.local.database.dao.CommandPermissionDao
import `in`.hridayan.ashell.ai.data.repository.AiAnalysisRepositoryImpl
import `in`.hridayan.ashell.core.common.domain.repository.AiAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.repository.CloudAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import javax.inject.Singleton

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE chat_sessions ADD COLUMN isUserRenamed INTEGER NOT NULL DEFAULT 0")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideAiDatabase(@ApplicationContext context: Context): AiDatabase {
        return Room.databaseBuilder(
            context,
            AiDatabase::class.java,
            "ai_database" // changed name since schema changed
        )
            .addMigrations(MIGRATION_4_5)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideAiCacheDao(database: AiDatabase): AiCacheDao {
        return database.aiCacheDao()
    }

    @Provides
    fun provideChatDao(database: AiDatabase): ChatDao {
        return database.chatDao()
    }

    @Provides
    fun provideCommandPermissionDao(database: AiDatabase): CommandPermissionDao {
        return database.commandPermissionDao()
    }

    @Provides
    @Singleton
    fun provideAiAnalysisRepository(
        cacheDao: AiCacheDao,
        cloudAnalysisRepository: CloudAnalysisRepository,
        settingsRepository: SettingsRepository,
    ): AiAnalysisRepository {
        return AiAnalysisRepositoryImpl(cacheDao, cloudAnalysisRepository, settingsRepository)
    }
}
