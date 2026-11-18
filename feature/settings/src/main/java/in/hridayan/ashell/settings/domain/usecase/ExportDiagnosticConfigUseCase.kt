package `in`.hridayan.ashell.settings.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

private const val CONFIG_FILE_NAME = "ashellyou_diagnostic_config.txt"
private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

class ExportDiagnosticConfigUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    operator fun invoke(reportText: String): Result<Uri> {
        return runCatching {
            val file = File(context.cacheDir, CONFIG_FILE_NAME)
            file.writeText(reportText)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}$FILE_PROVIDER_SUFFIX",
                file
            )
        }
    }
}
