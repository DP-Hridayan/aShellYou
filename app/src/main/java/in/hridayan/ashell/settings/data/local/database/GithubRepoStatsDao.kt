package `in`.hridayan.ashell.settings.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import `in`.hridayan.ashell.settings.data.local.entity.GithubRepoStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GithubRepoStatsDao {

    @Query("SELECT * FROM github_repo_stats WHERE repo = :repo")
    fun observe(repo: String): Flow<GithubRepoStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GithubRepoStatsEntity)
}