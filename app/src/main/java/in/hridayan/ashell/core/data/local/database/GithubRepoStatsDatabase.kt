package `in`.hridayan.ashell.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import `in`.hridayan.ashell.core.data.local.model.GithubRepoStatsEntity

@Database(entities = [GithubRepoStatsEntity::class], version = 1, exportSchema = false)
abstract class GithubRepoStatsDatabase : RoomDatabase() {
    abstract fun githubRepoStatsDao(): GithubRepoStatsDao
}