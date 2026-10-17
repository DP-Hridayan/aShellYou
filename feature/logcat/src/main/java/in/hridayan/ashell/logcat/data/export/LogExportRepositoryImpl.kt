package `in`.hridayan.ashell.logcat.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.logcat.domain.model.ExportTarget
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.logcat.domain.repository.LogExportRepository
import `in`.hridayan.ashell.logcat.domain.util.toLogcatLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject

private const val SHARE_CACHE_DIR = "logcat_export"
private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

/**
 * Streams entries line by line, so a full buffer is never held as one string. Share files live in
 * a cache folder that is emptied before each share, so only the latest export takes space.
 */
class LogExportRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : LogExportRepository {

    override suspend fun writeTo(target: ExportTarget, entries: List<LogEntry>): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(write(target, entries).toString())
            } catch (e: IOException) {
                Result.failure(e)
            } catch (e: SecurityException) {
                Result.failure(e)
            } catch (e: IllegalArgumentException) {
                Result.failure(e)
            }
        }

    private fun write(target: ExportTarget, entries: List<LogEntry>): Uri = when (target) {
        is ExportTarget.Document -> writeDocument(target.uriString.toUri(), entries)
        is ExportTarget.ShareCache -> writeShareFile(target.fileName, entries)
    }

    private fun writeDocument(uri: Uri, entries: List<LogEntry>): Uri {
        val stream = context.contentResolver.openOutputStream(uri)
            ?: throw IOException("Cannot open $uri")
        stream.writeEntries(entries)
        return uri
    }

    private fun writeShareFile(fileName: String, entries: List<LogEntry>): Uri {
        val file = File(freshShareDir(), fileName)
        file.outputStream().writeEntries(entries)
        return FileProvider.getUriForFile(context, context.packageName + FILE_PROVIDER_SUFFIX, file)
    }

    private fun freshShareDir(): File {
        val dir = File(context.cacheDir, SHARE_CACHE_DIR)
        dir.deleteRecursively()
        if (!dir.mkdirs()) throw IOException("Cannot create $dir")
        return dir
    }

    private fun OutputStream.writeEntries(entries: List<LogEntry>) {
        bufferedWriter().use { writer ->
            entries.forEach { entry ->
                writer.write(entry.toLogcatLine())
                writer.newLine()
            }
        }
    }
}
