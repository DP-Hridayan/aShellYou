package `in`.hridayan.ashell.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "github_repo_stats")
data class GithubRepoStatsEntity(
    @PrimaryKey val repo: String,
    val stars: Int,
    val forks: Int,
    val issues: Int,
    val downloads: Long,
    val license: String,
    val latestVersion: String,
    val lastUpdated: Long
)