package `in`.hridayan.ashell.qstiles.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.qstiles.data.parser.CodepointsParser
import `in`.hridayan.ashell.qstiles.data.renderer.MaterialIconBitmapRenderer
import `in`.hridayan.ashell.qstiles.domain.model.FontLoadState
import `in`.hridayan.ashell.qstiles.domain.repository.MaterialIconRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaterialIconRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val codepointsParser: CodepointsParser,
    private val bitmapRenderer: MaterialIconBitmapRenderer,
) : MaterialIconRepository {

    private val _fontState = MutableStateFlow<FontLoadState>(FontLoadState.Loading)
    override val fontState: StateFlow<FontLoadState> = _fontState.asStateFlow()

    private val loadMutex = Mutex()

    override suspend fun loadFont(): FontLoadState.Ready =
        withContext(Dispatchers.IO) {
            loadMutex.withLock {
                val currentState = _fontState.value
                if (currentState is FontLoadState.Ready) {
                    return@withContext currentState
                }

                _fontState.value = FontLoadState.Loading

                val fontFile = ensureFontFile()
                val codepointsText = context.assets.open(CODEPOINTS_ASSET_NAME).bufferedReader()
                    .use { it.readText() }

                val typeface = Typeface.createFromFile(fontFile)
                val icons = codepointsParser.parse(codepointsText)

                val readyState = FontLoadState.Ready(typeface = typeface, icons = icons)
                _fontState.value = readyState
                readyState
            }
        }

    override suspend fun renderAndCacheIcon(
        iconName: String,
        codepoint: Int,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val ready = _fontState.value as? FontLoadState.Ready
                ?: error(FONT_NOT_LOADED_MESSAGE)

            val bitmapFile = getBitmapFile(iconName)

            if (bitmapFile.exists()) return@runCatching bitmapFile

            val bitmap = bitmapRenderer.render(
                typeface = ready.typeface,
                codepoint = codepoint,
                sizePx = TILE_ICON_SIZE_PX,
            )

            bitmapFile.parentFile?.mkdirs()
            FileOutputStream(bitmapFile).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
            }

            bitmapFile
        }
    }

    override fun getCachedIconBitmap(iconName: String): File? {
        val file = getBitmapFile(iconName)
        return if (file.exists()) file else null
    }

    private suspend fun ensureFontFile(): File {
        val file = File(iconCacheDir(), FONT_FILE_NAME)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            context.assets.open(FONT_ASSET_NAME).use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file
    }

    private fun iconCacheDir(): File =
        File(context.filesDir, CACHE_DIR_NAME)

    private fun getBitmapFile(iconName: String): File =
        File(File(iconCacheDir(), BITMAPS_DIR_NAME), "$iconName$BITMAP_EXTENSION")

    private companion object {
        const val FONT_ASSET_NAME = "MaterialIconsOutlined-Regular.otf"
        const val CODEPOINTS_ASSET_NAME = "MaterialIconsOutlined-Regular.codepoints"
        const val CACHE_DIR_NAME = "material_icons"
        const val BITMAPS_DIR_NAME = "bitmaps"
        const val FONT_FILE_NAME = "MaterialIconsOutlined-Regular.otf"
        const val BITMAP_EXTENSION = ".png"
        const val TILE_ICON_SIZE_PX = 96
        const val PNG_QUALITY = 100
        const val FONT_NOT_LOADED_MESSAGE = "Font not loaded"
    }
}
