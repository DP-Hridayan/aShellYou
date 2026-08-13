package `in`.hridayan.ashell.settings.domain.repository

import `in`.hridayan.ashell.settings.domain.model.CustomFontEntity
import kotlinx.coroutines.flow.Flow

interface CustomFontRepository {
    fun getAllCustomFonts(): Flow<List<CustomFontEntity>>
    suspend fun insertCustomFont(entity: CustomFontEntity): Long
    suspend fun deleteCustomFont(entity: CustomFontEntity)
    suspend fun getCustomFontById(id: Int): CustomFontEntity?
}
