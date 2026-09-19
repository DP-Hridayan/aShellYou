package `in`.hridayan.ashell.settings.data.repository

import android.content.Context
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.data.mapper.toGitHubContributor
import `in`.hridayan.ashell.settings.data.mapper.toTranslator
import `in`.hridayan.ashell.settings.data.parser.GitHubContributorParser
import `in`.hridayan.ashell.settings.data.parser.TranslatorParser
import `in`.hridayan.ashell.settings.domain.model.GitHubContributor
import `in`.hridayan.ashell.settings.domain.model.SpecialThanks
import `in`.hridayan.ashell.settings.domain.model.Translator
import `in`.hridayan.ashell.settings.domain.repository.ContributorsRepository

class ContributorsRepositoryImpl(
    private val context: Context
) : ContributorsRepository {

    override fun getTranslators(): List<Translator> {
        return TranslatorParser
            .loadJson(context)
            .sortedByDescending { it.translated }
            .map { it.toTranslator() }
    }

    override fun getGitHubContributors(): List<GitHubContributor> {
        return GitHubContributorParser
            .loadJson(context)
            .sortedByDescending { it.contributions }
            .map { it.toGitHubContributor() }
    }

    override fun getSpecialThanks(): List<SpecialThanks> {
        return listOf(
            SpecialThanks(
                name = "DrDisagree",
                descriptionRes = R.string.special_thanks_mahmud,
                url = "https://github.com/Mahmud0808",
                avatarAssetPath = "github/contributors_pfp/special_thanks_Mahmud0808.png"
            ),
            SpecialThanks(
                name = "RikkaApps",
                descriptionRes = R.string.special_thanks_shizuku,
                url = "https://github.com/RikkaApps/Shizuku",
                avatarAssetPath = "github/contributors_pfp/special_thanks_RikkaApps.png"
            ),
            SpecialThanks(
                name = "John Wu",
                descriptionRes = R.string.special_thanks_libsu,
                url = "https://github.com/topjohnwu/libsu",
                avatarAssetPath = "github/contributors_pfp/special_thanks_topjohnwu.png"
            ),
            SpecialThanks(
                name = "LSPosed",
                descriptionRes = R.string.special_thanks_hidden_api_bypass,
                url = "https://github.com/LSPosed/AndroidHiddenApiBypass",
                avatarAssetPath = "github/contributors_pfp/special_thanks_LSPosed.png"
            ),
            SpecialThanks(
                name = "Nayuki",
                descriptionRes = R.string.special_thanks_qrcodegen,
                url = "https://github.com/nayuki/QR-Code-generator",
                avatarAssetPath = "github/contributors_pfp/special_thanks_nayuki.png"
            ),
            SpecialThanks(
                name = "Muntashir Al-Islam",
                descriptionRes = R.string.special_thanks_sun_security,
                url = "https://github.com/MuntashirAkon",
                avatarAssetPath = "github/contributors_pfp/special_thanks_MuntashirAkon.png"
            )
        )
    }
}
