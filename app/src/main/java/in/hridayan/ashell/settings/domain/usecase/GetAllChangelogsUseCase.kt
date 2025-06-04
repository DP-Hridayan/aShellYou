package `in`.hridayan.ashell.settings.domain.usecase

import android.annotation.SuppressLint
import android.content.Context
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.settings.data.local.model.ChangelogItem
import `in`.hridayan.ashell.settings.data.local.model.versionList

class GetAllChangelogsUseCase(
    private val context: Context,
    private val versions: List<String> = versionList
) {
    @SuppressLint("DiscouragedApi")
    operator fun invoke(): List<ChangelogItem> {
        val res = context.resources
        val pkg = context.packageName

        return versions.map { version ->
            val resourceName = "changelog_" + version.replace('.', '_')

            val resId = res.getIdentifier(resourceName, "string", pkg)

            val text = context.getString(
                if (resId != 0) resId else R.string.no_changelog_found
            )

            ChangelogItem(versionName = version, changelog = text)
        }
    }
}