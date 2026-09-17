package `in`.hridayan.ashell.adbsideload.data.file

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadPackageInfo
import `in`.hridayan.ashell.adbsideload.domain.repository.SideloadPackageInspector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SideloadPackageInspectorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SideloadPackageInspector {

    override suspend fun inspect(uri: Uri): SideloadPackageInfo? = withContext(Dispatchers.IO) {
        SideloadPackage.inspect(context, uri)
    }
}
