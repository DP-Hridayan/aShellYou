package `in`.hridayan.ashell.settings.domain.usecase

import android.net.Uri
import `in`.hridayan.ashell.settings.domain.model.CustomFontEntity
import `in`.hridayan.ashell.settings.domain.repository.CustomFontRepository
import javax.inject.Inject

class ImportCustomFontUseCase @Inject constructor(
    private val validateFontFileUseCase: ValidateFontFileUseCase,
    private val copyFontToInternalStorageUseCase: CopyFontToInternalStorageUseCase,
    private val repository: CustomFontRepository
) {
    suspend operator fun invoke(
        uri: Uri,
        displayName: String
    ): Result<CustomFontEntity> {
        val isValid = validateFontFileUseCase(uri).getOrElse { return Result.failure(it) }
        if (!isValid) return Result.failure(IllegalArgumentException("Not a valid TTF file"))

        val filePath = copyFontToInternalStorageUseCase(uri, displayName)
            .getOrElse { return Result.failure(it) }

        val entity = CustomFontEntity(displayName = displayName, filePath = filePath)
        val insertedId = repository.insertCustomFont(entity)
        return Result.success(entity.copy(id = insertedId.toInt()))
    }
}
