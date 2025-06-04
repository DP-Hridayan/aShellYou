package `in`.hridayan.ashell.core.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

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
