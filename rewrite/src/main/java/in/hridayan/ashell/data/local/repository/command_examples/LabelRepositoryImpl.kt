package `in`.hridayan.ashell.data.local.repository.command_examples

import `in`.hridayan.ashell.data.local.database.command_examples.LabelDao
import `in`.hridayan.ashell.data.local.model.command_examples.LabelEntity
import `in`.hridayan.ashell.domain.repository.command_examples.LabelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LabelRepositoryImpl @Inject constructor(
    private val labelDao: LabelDao
) : LabelRepository {
    override suspend fun insertLabel(name: String) {
        labelDao.insertLabel(LabelEntity(label = name))
    }

    override fun getAllLabels(): Flow<List<LabelEntity>> {
        return labelDao.getAllLabels()
    }
}
