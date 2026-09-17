package `in`.hridayan.ashell.adbsideload.domain.repository

import android.net.Uri
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadPackageInfo

/** Reads the name and size of a package the user picked, without opening it for transfer. */
interface SideloadPackageInspector {
    suspend fun inspect(uri: Uri): SideloadPackageInfo?
}
