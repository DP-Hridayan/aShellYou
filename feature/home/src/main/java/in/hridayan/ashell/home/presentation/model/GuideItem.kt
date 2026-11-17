package `in`.hridayan.ashell.home.presentation.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.resources.R

enum class GuideCategory(
    @StringRes val titleRes: Int
) {
    Connection(R.string.connection_and_shell),
    Advanced(R.string.fastboot_and_recovery),
    Utilities(R.string.utilities),
    Community(R.string.community_and_support)
}

data class GuideItem(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val url: String,
    val category: GuideCategory
)

object GuideCatalog {

    fun getAllGuides(): List<GuideItem> {
        return listOf(
            GuideItem(
                id = "local_adb",
                titleRes = R.string.local_adb,
                descriptionRes = R.string.guide_local_adb_desc,
                iconRes = R.drawable.ic_adb,
                url = UrlConst.URL_WIKI_LOCAL_ADB,
                category = GuideCategory.Connection
            ),
            GuideItem(
                id = "otg_adb",
                titleRes = R.string.otg,
                descriptionRes = R.string.guide_otg_adb_desc,
                iconRes = R.drawable.ic_otg,
                url = UrlConst.URL_OTG_INSTRUCTIONS,
                category = GuideCategory.Connection
            ),
            GuideItem(
                id = "wireless_debugging",
                titleRes = R.string.wireless_debugging,
                descriptionRes = R.string.guide_wireless_debugging_desc,
                iconRes = R.drawable.ic_wireless,
                url = UrlConst.URL_WIRELESS_DEBUGGING_INSTRUCTIONS,
                category = GuideCategory.Connection
            ),
            GuideItem(
                id = "fastboot",
                titleRes = R.string.fastboot,
                descriptionRes = R.string.guide_fastboot_desc,
                iconRes = R.drawable.ic_flash_on,
                url = UrlConst.URL_WIKI_FASTBOOT_OTG,
                category = GuideCategory.Advanced
            ),
            GuideItem(
                id = "adb_sideload",
                titleRes = R.string.adb_sideload,
                descriptionRes = R.string.guide_sideload_desc,
                iconRes = R.drawable.ic_mobile_arrow_down,
                url = UrlConst.URL_WIKI_ADB_SIDELOAD,
                category = GuideCategory.Advanced
            ),
            GuideItem(
                id = "screen_mirror",
                titleRes = R.string.screen_mirror,
                descriptionRes = R.string.guide_screen_mirror_desc,
                iconRes = R.drawable.ic_screen_cast,
                url = UrlConst.URL_WIKI_SCREEN_MIRRORING,
                category = GuideCategory.Advanced
            ),
            GuideItem(
                id = "logcat",
                titleRes = R.string.logcat,
                descriptionRes = R.string.guide_logcat_desc,
                iconRes = R.drawable.ic_bug,
                url = UrlConst.URL_WIKI_LOGCAT_VIEWER,
                category = GuideCategory.Utilities
            ),
            GuideItem(
                id = "port_forwarding",
                titleRes = R.string.port_forwarding,
                descriptionRes = R.string.guide_port_forward_desc,
                iconRes = R.drawable.ic_port_forward,
                url = UrlConst.URL_WIKI_PORT_FORWARDING,
                category = GuideCategory.Utilities
            ),
            GuideItem(
                id = "file_browser",
                titleRes = R.string.file_browser,
                descriptionRes = R.string.guide_file_browser_desc,
                iconRes = R.drawable.ic_directory,
                url = UrlConst.URL_WIKI_FILE_BROWSER,
                category = GuideCategory.Utilities
            ),
            GuideItem(
                id = "qs_tiles",
                titleRes = R.string.qs_tiles,
                descriptionRes = R.string.guide_qs_tiles_desc,
                iconRes = R.drawable.ic_dashboard,
                url = UrlConst.URL_WIKI_QUICK_SETTINGS_TILES,
                category = GuideCategory.Utilities
            ),
            GuideItem(
                id = "command_examples",
                titleRes = R.string.commands,
                descriptionRes = R.string.guide_command_examples_desc,
                iconRes = R.drawable.ic_list_alt,
                url = UrlConst.URL_WIKI_COMMAND_EXAMPLES,
                category = GuideCategory.Utilities
            ),
            GuideItem(
                id = "ai_assistant",
                titleRes = R.string.ai_shell_assistant,
                descriptionRes = R.string.guide_ai_assistant_desc,
                iconRes = R.drawable.ic_help,
                url = UrlConst.URL_WIKI_AI_ASSISTANT,
                category = GuideCategory.Utilities
            ),
            GuideItem(
                id = "official_wiki",
                titleRes = R.string.official_wiki,
                descriptionRes = R.string.guide_wiki_desc,
                iconRes = R.drawable.ic_github,
                url = UrlConst.URL_WIKI_ROOT,
                category = GuideCategory.Community
            ),
            GuideItem(
                id = "faqs",
                titleRes = R.string.faqs,
                descriptionRes = R.string.guide_faqs_desc,
                iconRes = R.drawable.ic_help2,
                url = UrlConst.URL_WIKI_FAQS,
                category = GuideCategory.Community
            ),
            GuideItem(
                id = "telegram_community",
                titleRes = R.string.telegram,
                descriptionRes = R.string.guide_telegram_desc,
                iconRes = R.drawable.ic_telegram,
                url = UrlConst.URL_TELEGRAM_CHANNEL,
                category = GuideCategory.Community
            )
        )
    }
}
