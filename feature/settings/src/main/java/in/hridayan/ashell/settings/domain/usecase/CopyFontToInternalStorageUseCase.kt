package `in`.hridayan.ashell.settings.domain.usecase

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

private const val FONTS_DIR_NAME = "fonts"
private const val TTF_EXTENSION = ".ttf"
private const val MAX_FILENAME_LENGTH = 50

class CopyFontToInternalStorageUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(uri: Uri, displayName: String): Result<String> = runCatching {
        val fontsDir = File(context.filesDir, FONTS_DIR_NAME).also { it.mkdirs() }
        val sanitized = displayName
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(MAX_FILENAME_LENGTH)
            .ifBlank { "custom_font_${System.currentTimeMillis()}" }
        val dest = File(fontsDir, "$sanitized$TTF_EXTENSION")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Cannot open input stream for URI")
        dest.absolutePath
    }
}
