package `in`.hridayan.ashell.core.presentation.theme.util

import android.util.Base64
import `in`.hridayan.ashell.core.presentation.theme.data.UserGeneratedColorSchemeEntity
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

object ColorSchemeSerializer {

    private val json = Json { ignoreUnknownKeys = true }

    fun serialize(entity: UserGeneratedColorSchemeEntity): String {
        val jsonString = json.encodeToString(entity)
        val compressedBytes = compress(jsonString.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(compressedBytes, Base64.NO_WRAP or Base64.URL_SAFE)
    }

    fun deserialize(base64String: String): UserGeneratedColorSchemeEntity {
        val compressedBytes = Base64.decode(base64String, Base64.NO_WRAP or Base64.URL_SAFE)
        val jsonBytes = decompress(compressedBytes)
        val jsonString = String(jsonBytes, Charsets.UTF_8)
        return json.decodeFromString<UserGeneratedColorSchemeEntity>(jsonString)
    }

    private fun compress(data: ByteArray): ByteArray {
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(data)
        deflater.finish()

        val outputStream = ByteArrayOutputStream(data.size)
        val buffer = ByteArray(1024)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        return outputStream.toByteArray()
    }

    private fun decompress(data: ByteArray): ByteArray {
        val inflater = Inflater()
        inflater.setInput(data)

        val outputStream = ByteArrayOutputStream(data.size)
        val buffer = ByteArray(1024)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        return outputStream.toByteArray()
    }
}
