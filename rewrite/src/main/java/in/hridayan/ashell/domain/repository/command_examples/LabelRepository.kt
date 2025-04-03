package `in`.hridayan.ashell.domain.repository.command_examples

import `in`.hridayan.ashell.data.local.model.command_examples.LabelEntity
import kotlinx.coroutines.flow.Flow

interface LabelRepository {
    fun getAllLabels(): Flow<List<LabelEntity>>
    suspend fun insertLabel(name: String)
}