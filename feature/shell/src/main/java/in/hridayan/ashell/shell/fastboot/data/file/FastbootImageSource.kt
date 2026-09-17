package `in`.hridayan.ashell.shell.fastboot.data.file

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import `in`.hridayan.fastboot.FastbootDataSource
import java.io.IOException
import java.io.InputStream

/**
 * An image chosen from storage, streamed to the device rather than read into memory: a partition
 * image can easily exceed the heap the app is allowed.
 */
class FastbootImageSource private constructor(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    override val name: String,
    override val length: Long,
) : FastbootDataSource {

    override fun open(): InputStream =
        contentResolver.openInputStream(uri) ?: throw IOException("Could not open $name")

    companion object {
        private const val DEFAULT_NAME = "image.img"

        fun from(context: Context, uri: Uri): FastbootImageSource? {
            val size = resolveSize(context, uri)
            if (size <= 0L) return null
            return FastbootImageSource(context.contentResolver, uri, resolveName(context, uri), size)
        }

        private fun resolveName(context: Context, uri: Uri): String =
            queryColumn(context, uri, OpenableColumns.DISPLAY_NAME) { it.getString(0) }
                ?: uri.lastPathSegment?.substringAfterLast('/')
                ?: DEFAULT_NAME

        private fun resolveSize(context: Context, uri: Uri): Long {
            val declared = queryColumn(context, uri, OpenableColumns.SIZE) { it.getLong(0) }
            if (declared != null && declared > 0L) return declared
            return runCatching {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize }
            }.getOrNull() ?: -1L
        }

        private fun <T> queryColumn(
            context: Context,
            uri: Uri,
            column: String,
            read: (Cursor) -> T?,
        ): T? = runCatching {
            context.contentResolver.query(uri, arrayOf(column), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst() && !cursor.isNull(0)) read(cursor) else null
            }
        }.getOrNull()
    }
}
