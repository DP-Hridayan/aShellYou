package `in`.hridayan.ashell.settings.domain.usecase

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

private const val NAME_TABLE_TAG = 0x6E616D65
private const val NAME_ID_FULL_NAME = 4
private const val NAME_ID_FAMILY_NAME = 1
private const val PLATFORM_ID_UNICODE = 0
private const val PLATFORM_ID_MICROSOFT = 3

data class FontNameResult(val name: String?, val tempFilePath: String)

class ReadFontNameUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(uri: Uri): Result<FontNameResult> = runCatching {
        val tempFile = File(context.cacheDir, "font_name_probe.ttf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }
        FontNameResult(name = parseFontName(tempFile), tempFilePath = tempFile.absolutePath)
    }

    private fun parseFontName(file: File): String? {
        val bytes = file.readBytes()
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)

        val numTables = buf.getShort(4).toInt() and 0xFFFF
        var nameTableOffset = -1

        for (i in 0 until numTables) {
            val baseOffset = 12 + i * 16
            val tag = buf.getInt(baseOffset)
            if (tag == NAME_TABLE_TAG) {
                nameTableOffset = buf.getInt(baseOffset + 8)
                break
            }
        }

        if (nameTableOffset < 0) return null

        val count = buf.getShort(nameTableOffset + 2).toInt() and 0xFFFF
        val stringOffset = buf.getShort(nameTableOffset + 4).toInt() and 0xFFFF

        var fullName: String? = null
        var familyName: String? = null

        for (i in 0 until count) {
            val recordBase = nameTableOffset + 6 + i * 12
            val platformId = buf.getShort(recordBase).toInt() and 0xFFFF
            val nameId = buf.getShort(recordBase + 6).toInt() and 0xFFFF
            val length = buf.getShort(recordBase + 8).toInt() and 0xFFFF
            val offset = buf.getShort(recordBase + 10).toInt() and 0xFFFF

            if (nameId != NAME_ID_FULL_NAME && nameId != NAME_ID_FAMILY_NAME) continue
            if (platformId != PLATFORM_ID_UNICODE && platformId != PLATFORM_ID_MICROSOFT) continue

            val absOffset = nameTableOffset + stringOffset + offset
            val nameBytes = bytes.copyOfRange(absOffset, absOffset + length)
            val name = String(nameBytes, Charsets.UTF_16BE).trim()

            when (nameId) {
                NAME_ID_FULL_NAME -> fullName = name
                NAME_ID_FAMILY_NAME -> if (familyName == null) familyName = name
            }
        }

        return fullName ?: familyName
    }
}
