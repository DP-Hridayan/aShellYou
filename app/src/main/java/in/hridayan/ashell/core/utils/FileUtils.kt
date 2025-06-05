package `in`.hridayan.ashell.core.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.core.content.FileProvider

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

