package `in`.hridayan.ashell.shell.file_browser.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.ui.graphics.vector.ImageVector
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile

/**
 * Maps file extensions to appropriate icons for dynamic file type display.
 */
object FileIconMapper {

    private val extensionIconMap: Map<String, ImageVector> by lazy {
        buildMap {
            // Audio files
            listOf("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus", "alac").forEach {
                put(it, Icons.Rounded.AudioFile)
            }

            // Video files
            listOf("mp4", "mkv", "avi", "mov", "webm", "3gp", "wmv", "flv", "m4v", "mpeg", "mpg").forEach {
                put(it, Icons.Rounded.VideoFile)
            }

            // Image files
            listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "ico", "tiff", "heic", "heif", "raw").forEach {
                put(it, Icons.Rounded.Image)
            }

            // PDF
            put("pdf", Icons.Rounded.PictureAsPdf)

            // Text/Document files
            listOf("txt", "doc", "docx", "rtf", "odt", "md", "log", "csv").forEach {
                put(it, Icons.Rounded.Description)
            }

            // Archive files
            listOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "tgz", "tbz2", "lz4", "zst").forEach {
                put(it, Icons.Rounded.FolderZip)
            }

            // APK files
            listOf("apk", "apks", "xapk", "aab").forEach {
                put(it, Icons.Rounded.Android)
            }

            // JSON/Data files
            listOf("json", "yaml", "yml", "toml", "ini", "cfg", "conf", "properties").forEach {
                put(it, Icons.Rounded.DataObject)
            }

            // Code/Markup files
            listOf("xml", "html", "htm", "xhtml", "css", "scss", "less", "kt", "java", "c", "cpp", "h", "hpp",
                "js", "ts", "jsx", "tsx", "vue", "svelte", "go", "rs", "rb", "php", "swift", "dart", "cs",
                "gradle", "kts", "groovy", "scala", "r", "sql", "graphql", "proto").forEach {
                put(it, Icons.Rounded.Code)
            }

            // Script files
            listOf("sh", "bash", "zsh", "fish", "bat", "cmd", "ps1", "py", "pl", "awk", "sed").forEach {
                put(it, Icons.Rounded.Terminal)
            }

            // Binary/System files
            listOf("so", "dll", "exe", "bin", "elf", "o", "a", "dylib", "dex", "odex", "vdex", "oat").forEach {
                put(it, Icons.Rounded.Settings)
            }
        }
    }

    /**
     * Get the appropriate icon for a file based on its type.
     */
    fun getIcon(file: RemoteFile): ImageVector {
        return when {
            file.isParentDirectory -> Icons.Rounded.FolderOpen
            file.isDirectory -> Icons.Rounded.Folder
            else -> getIconForExtension(file.name)
        }
    }

    /**
     * Get icon based on file extension.
     */
    private fun getIconForExtension(fileName: String): ImageVector {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension.isEmpty() || extension == fileName.lowercase()) {
            return Icons.AutoMirrored.Rounded.InsertDriveFile
        }
        return extensionIconMap[extension] ?: Icons.AutoMirrored.Rounded.InsertDriveFile
    }
}
