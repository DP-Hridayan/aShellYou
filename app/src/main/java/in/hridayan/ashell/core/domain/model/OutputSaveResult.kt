package `in`.hridayan.ashell.core.domain.model

import android.net.Uri

data class OutputSaveResult(
    val success: Boolean,
    val uri: Uri? = null
)
