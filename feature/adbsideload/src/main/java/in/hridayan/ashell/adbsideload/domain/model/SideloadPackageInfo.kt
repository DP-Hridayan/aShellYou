package `in`.hridayan.ashell.adbsideload.domain.model

import android.net.Uri

data class SideloadPackageInfo(
    val uri: Uri,
    val name: String,
    val size: Long,
)
