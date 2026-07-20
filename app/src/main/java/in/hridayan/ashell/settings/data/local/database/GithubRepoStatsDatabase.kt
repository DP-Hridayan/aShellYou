package `in`.hridayan.ashell.settings.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import `in`.hridayan.ashell.settings.data.local.entity.GithubRepoStatsEntity

@Database(entities = [GithubRepoStatsEntity::class], version = 1)
abstract class GithubRepoStatsDatabase : RoomDatabase() {
    abstract fun githubRepoStatsDao(): GithubRepoStatsDao
}