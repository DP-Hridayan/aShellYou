package `in`.hridayan.ashell.settings.domain.usecase

import `in`.hridayan.ashell.settings.domain.model.CustomFontEntity
import `in`.hridayan.ashell.settings.domain.repository.CustomFontRepository
import java.io.File
import javax.inject.Inject

class DeleteCustomFontUseCase @Inject constructor(
    private val repository: CustomFontRepository
) {
    suspend operator fun invoke(entity: CustomFontEntity): Result<Unit> = runCatching {
        File(entity.filePath).delete()
        repository.deleteCustomFont(entity)
    }
}
