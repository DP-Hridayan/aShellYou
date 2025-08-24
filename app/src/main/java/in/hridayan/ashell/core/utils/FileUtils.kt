package `in`.hridayan.ashell.core.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.IOException
import java.io.File
import java.io.FileWriter

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

fun getDownloadsFolderUri(context: Context): Uri? {
    val downloadsFolder =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val docId = "primary:Download"
        DocumentsContract.buildTreeDocumentUri(
            "com.android.externalstorage.documents",
            docId
        )
    } else {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            downloadsFolder
        )
    }
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

fun saveToFileFlow(
    sb: String,
    activity: Activity,
    fileName: String,
    savePathUri: Uri
): Flow<Boolean> = flow {
    val result = when {
        savePathUri.scheme == "content" -> saveToCustomDirectory(
            sb,
            activity,
            fileName,
            savePathUri
        )

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> saveToFileApi29AndAbove(
            sb,
            activity,
            fileName
        )

        else -> saveToFileBelowApi29(sb, activity, fileName)
    }
    emit(result)
}

private fun saveToCustomDirectory(
    sb: String,
    activity: Activity,
    fileName: String,
    treeUri: Uri
): Boolean {
    val documentFile = DocumentFile.fromTreeUri(activity, treeUri) ?: return false
    val file = documentFile.createFile("text/plain", fileName) ?: return false

    return try {
        activity.contentResolver.openOutputStream(file.uri)?.use { outputStream ->
            outputStream.write(sb.toByteArray())
            true
        } ?: false
    } catch (e: IOException) {
        Log.e("FileUtils", "Error writing file", e)
        false
    }
}

private fun saveToFileApi29AndAbove(
    sb: String,
    activity: Activity,
    fileName: String
): Boolean = try {
    val values = ContentValues().apply {
        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }

    val uri = activity.contentResolver.insert(
        android.provider.MediaStore.Files.getContentUri("external"),
        values
    )

    uri?.let {
        activity.contentResolver.openOutputStream(it)?.use { outputStream ->
            outputStream.write(sb.toByteArray())
            true
        }
    } ?: false
} catch (e: Exception) {
    Log.e("FileUtils", "Error saving file API29+", e)
    false
}

private fun saveToFileBelowApi29(
    sb: String,
    activity: Activity,
    fileName: String
): Boolean {
    if (ActivityCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
        return false
    }

    return try {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        file.writeText(sb)
        true
    } catch (e: Exception) {
        Log.e("FileUtils", "Error saving file below API29", e)
        false
    }
}
