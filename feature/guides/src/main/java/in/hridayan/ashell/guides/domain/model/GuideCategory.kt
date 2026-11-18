package `in`.hridayan.ashell.guides.domain.model

import androidx.annotation.StringRes
import `in`.hridayan.ashell.core.resources.R

enum class GuideCategory(
    @StringRes val titleRes: Int
) {
    Connection(R.string.connection_and_shell),
    Advanced(R.string.fastboot_and_recovery),
    Utilities(R.string.utilities),
    Community(R.string.community_and_support)
}
