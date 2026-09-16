package `in`.hridayan.ashell.adbsideload.data.file

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import `in`.hridayan.ashell.adbsideload.data.protocol.FileBlockReader
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadPackageInfo
import `in`.hridayan.ashell.adbsideload.domain.protocol.SideloadBlockReader
import java.io.Closeable
import java.io.FileInputStream

/**
 * An opened update package: its display name, byte size and a random-access block reader.
 */
class SideloadPackage private constructor(
    private val descriptor: ParcelFileDescriptor,
    val name: String,
    val size: Long,
) : Closeable {

    private val input = FileInputStream(descriptor.fileDescriptor)

    val reader: SideloadBlockReader = FileBlockReader(input.channel)

    override fun close() {
        runCatching { input.close() }
        runCatching { descriptor.close() }
    }

    companion object {
        private const val DEFAULT_NAME = "package.zip"

        fun open(context: Context, uri: Uri): SideloadPackage? {
            val descriptor = runCatching { context.contentResolver.openFileDescriptor(uri, "r") }
                .getOrNull() ?: return null
            val size = descriptor.statSize.takeIf { it > 0 } ?: querySize(context, uri)
            return SideloadPackage(descriptor, resolveName(context, uri), size)
        }

        fun inspect(context: Context, uri: Uri): SideloadPackageInfo? =
            open(context, uri)?.use { SideloadPackageInfo(uri, it.name, it.size) }

        fun resolveName(context: Context, uri: Uri): String =
            queryColumn(context, uri, OpenableColumns.DISPLAY_NAME) { it.getString(0) }
                ?: uri.lastPathSegment?.substringAfterLast('/')
                ?: DEFAULT_NAME

        private fun querySize(context: Context, uri: Uri): Long =
            queryColumn(context, uri, OpenableColumns.SIZE) { it.getLong(0) } ?: -1L

        private fun <T> queryColumn(
            context: Context,
            uri: Uri,
            column: String,
            read: (android.database.Cursor) -> T?,
        ): T? = runCatching {
            context.contentResolver.query(uri, arrayOf(column), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst() && !cursor.isNull(0)) read(cursor) else null
            }
        }.getOrNull()
    }
}
