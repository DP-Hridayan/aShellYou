package `in`.hridayan.ashell.settings.data.repository

import `in`.hridayan.ashell.settings.data.local.database.CustomFontDao
import `in`.hridayan.ashell.settings.domain.model.CustomFontEntity
import `in`.hridayan.ashell.settings.domain.repository.CustomFontRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CustomFontRepositoryImpl @Inject constructor(
    private val dao: CustomFontDao
) : CustomFontRepository {

    override fun getAllCustomFonts(): Flow<List<CustomFontEntity>> =
        dao.getAllCustomFonts()

    override suspend fun insertCustomFont(entity: CustomFontEntity): Long =
        dao.insertCustomFont(entity)

    override suspend fun deleteCustomFont(entity: CustomFontEntity) =
        dao.deleteCustomFont(entity)

    override suspend fun getCustomFontById(id: Int): CustomFontEntity? =
        dao.getCustomFontById(id)
}
