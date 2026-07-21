package `in`.hridayan.ashell.settings.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.settings.data.local.database.GithubRepoStatsDao
import `in`.hridayan.ashell.settings.data.local.database.GithubRepoStatsDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GithubRepoStatsDatabaseModule {

    @Provides
    @Singleton
    fun provideGithubRepoStatsDatabase(@ApplicationContext context: Context): GithubRepoStatsDatabase {
        return Room.databaseBuilder(
            context,
            GithubRepoStatsDatabase::class.java,
            "github_repo_stats_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideGithubRepoStatsDao(database: GithubRepoStatsDatabase): GithubRepoStatsDao {
        return database.githubRepoStatsDao()
    }
}
