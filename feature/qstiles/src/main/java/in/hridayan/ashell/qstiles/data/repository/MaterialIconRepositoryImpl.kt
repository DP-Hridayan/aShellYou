package `in`.hridayan.ashell.qstiles.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.qstiles.data.parser.CodepointsParser
import `in`.hridayan.ashell.qstiles.data.renderer.MaterialIconBitmapRenderer
import `in`.hridayan.ashell.qstiles.domain.model.FontLoadState
import `in`.hridayan.ashell.qstiles.domain.model.MaterialIconStyle
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

    private val _fontState =
        MutableStateFlow<FontLoadState>(FontLoadState.Loading(MaterialIconStyle.OUTLINED))
    override val fontState: StateFlow<FontLoadState> = _fontState.asStateFlow()

    private val loadMutex = Mutex()
    private val loadedFonts = mutableMapOf<MaterialIconStyle, FontLoadState.Ready>()

    override suspend fun getFontState(style: MaterialIconStyle): FontLoadState.Ready =
        withContext(Dispatchers.IO) {
            loadMutex.withLock {
                loadedFonts[style]?.let { return@withContext it }

                val fontFile = ensureFontFile(style)
                val codepointsText = context.assets.open(style.codepointsAssetName).bufferedReader()
                    .use { it.readText() }

                val typeface = Typeface.createFromFile(fontFile)
                val icons = codepointsParser.parse(codepointsText)

                val readyState =
                    FontLoadState.Ready(style = style, typeface = typeface, icons = icons)
                loadedFonts[style] = readyState
                readyState
            }
        }

    override suspend fun loadFont(style: MaterialIconStyle): FontLoadState.Ready {
        _fontState.value = FontLoadState.Loading(style)
        val readyState = getFontState(style)
        _fontState.value = readyState
        return readyState
    }

    override suspend fun renderAndCacheIcon(
        style: MaterialIconStyle,
        iconName: String,
        codepoint: Int,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val ready = _fontState.value as? FontLoadState.Ready
                ?: error(FONT_NOT_LOADED_MESSAGE)

            require(ready.style == style) { "Requested style does not match currently loaded style." }

            val bitmapFile = getBitmapFile(style, iconName)

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

    override fun getCachedIconBitmap(style: MaterialIconStyle, iconName: String): File? {
        val file = getBitmapFile(style, iconName)
        return if (file.exists()) file else null
    }

    private suspend fun ensureFontFile(style: MaterialIconStyle): File {
        val file = File(iconCacheDir(), style.fontAssetName)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            context.assets.open(style.fontAssetName).use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file
    }

    private fun iconCacheDir(): File =
        File(context.filesDir, CACHE_DIR_NAME)

    private fun getBitmapFile(style: MaterialIconStyle, iconName: String): File =
        File(File(File(iconCacheDir(), BITMAPS_DIR_NAME), style.name), "$iconName$BITMAP_EXTENSION")

    private companion object {
        const val CACHE_DIR_NAME = "material_icons"
        const val BITMAPS_DIR_NAME = "bitmaps"
        const val BITMAP_EXTENSION = ".png"
        const val TILE_ICON_SIZE_PX = 96
        const val PNG_QUALITY = 100
        const val FONT_NOT_LOADED_MESSAGE = "Font not loaded"
    }
}
