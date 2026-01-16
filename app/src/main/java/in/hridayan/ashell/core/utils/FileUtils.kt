package `in`.hridayan.ashell.core.utils

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import `in`.hridayan.ashell.core.domain.model.SaveProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.io.IOException
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.OutputStreamWriter

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndexOpenableColumnName()
        cursor.moveToFirst()
        if (nameIndex != -1) cursor.getString(nameIndex) else null
    }
}

fun Cursor.getColumnIndexOpenableColumnName(): Int {
    return getColumnIndex(OpenableColumns.DISPLAY_NAME)
}

fun getFullPathFromTreeUri(uri: Uri, context: Context): String? {
    val docId = DocumentsContract.getTreeDocumentId(uri)
    val split = docId.split(":")
    if (split.size < 2) return null
    val type = split[0]
    val relativePath = split[1]

    val storagePath = when (type) {
        "primary" -> Environment.getExternalStorageDirectory().path
        else -> "/storage/$type"
    }

    return "$storagePath/$relativePath"
}

fun read(file: File): String? {
    return try {
        file.bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun create(text: String, path: File) {
    try {
        FileWriter(path).use { writer ->
            writer.write(text)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

/**
 * Streaming save function that writes output line-by-line to avoid OOM.
 * Emits progress updates during the save operation.
 *
 * @param lines Sequence of lines to write (lazy evaluation)
 * @param totalLines Total number of lines for progress calculation
 * @param activity Activity context for file operations
 * @param fileName Name of the file to create
 * @param savePathUri URI of the directory to save to
 * @return Flow that emits [SaveProgress] updates
 */
fun saveToFileStreamingFlow(
    lines: Sequence<String>,
    totalLines: Int,
    activity: Activity,
    fileName: String,
    savePathUri: Uri
): Flow<SaveProgress> = flow {
    try {
        val uri: Uri? = when {
            savePathUri.scheme == "content" -> {
                val documentFile = DocumentFile.fromTreeUri(activity, savePathUri)
                    ?: throw IOException("Cannot access directory")
                val file = documentFile.createFile("text/plain", fileName)
                    ?: throw IOException("Cannot create file")
                file.uri
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                activity.contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    values
                )
            }

            else -> {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        0
                    )
                    emit(SaveProgress.Error("Storage permission required"))
                    return@flow
                }
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                )
                // For pre-Q, we'll write directly to the file
                FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.provider",
                    file
                )
            }
        }

        if (uri == null) {
            emit(SaveProgress.Error("Failed to create file"))
            return@flow
        }

        activity.contentResolver.openOutputStream(uri)?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                var currentLine = 0
                var lastEmitTime = System.currentTimeMillis()
                val emitIntervalMs = 100L

                lines.forEach { line ->
                    writer.write(line)
                    writer.newLine()
                    currentLine++

                    val now = System.currentTimeMillis()
                    if (now - lastEmitTime >= emitIntervalMs) {
                        emit(SaveProgress.Saving(currentLine, totalLines))
                        lastEmitTime = now
                    }
                }
            }
        } ?: throw IOException("Cannot open output stream")

        emit(SaveProgress.Success(uri))
    } catch (e: Exception) {
        Log.e("FileUtils", "Error in streaming save", e)
        emit(SaveProgress.Error(e.message ?: "Unknown error"))
    }
}.flowOn(Dispatchers.IO)