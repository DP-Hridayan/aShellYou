package `in`.hridayan.ashell.settings.domain.usecase

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private val TTF_MAGIC = byteArrayOf(0x00, 0x01, 0x00, 0x00)

class ValidateFontFileUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(uri: Uri): Result<Boolean> = runCatching {
        val header = ByteArray(TTF_MAGIC.size)
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.read(header)
        } ?: return Result.failure(IllegalStateException("Cannot open file"))
        header.contentEquals(TTF_MAGIC)
    }
}
