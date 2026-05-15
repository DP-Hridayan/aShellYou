package `in`.hridayan.ashell.settings.presentation.page.languages.viewmodel

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.settings.domain.model.SupportedLocale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LanguagesViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    /** All supported locales, English first, then sorted alphabetically by displayName. */
    val supportedLocales: List<SupportedLocale> = buildSupportedLocales()

    private val _currentLocaleTag = MutableStateFlow(resolveCurrentTag())
    val currentLocaleTag: StateFlow<String> = _currentLocaleTag.asStateFlow()

    fun setLocale(tag: String) {
        val localeList = if (tag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
        _currentLocaleTag.value = tag
    }

    /**
     * Re-read the current locale tag from the system.
     * Call this on lifecycle resume to detect external locale changes
     * (e.g. user changed language via system settings).
     */
    fun refreshCurrentTag() {
        _currentLocaleTag.value = resolveCurrentTag()
    }

    private fun resolveCurrentTag(): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        return if (appLocales.isEmpty) "" else appLocales[0]?.toLanguageTag().orEmpty()
    }

    private fun buildSupportedLocales(): List<SupportedLocale> {
        // BCP 47 tags matching our values-* resource dirs
        val tags = listOf(
            "ar-SA", "de-DE", "fa-IR", "fr-FR", "hi-IN",
            "ja-JP", "pt-PT", "ru-RU", "tr-TR", "uk-UA", "zh-CN"
        )

        val currentLocale = Locale.getDefault()

        val locales = tags.map { tag ->
            val locale = Locale.forLanguageTag(tag)
            SupportedLocale(
                tag = tag,
                displayName = locale.getDisplayName(currentLocale)
                    .replaceFirstChar { it.uppercase(currentLocale) },
                nativeName = locale.getDisplayName(locale)
                    .replaceFirstChar { it.uppercase(locale) },
            )
        }.sortedBy { it.displayName.lowercase() }

        // English (default) at the beginning
        val english = run {
            val enLocale = Locale.ENGLISH
            SupportedLocale(
                tag = "en",
                displayName = enLocale.getDisplayName(currentLocale)
                    .replaceFirstChar { it.uppercase(currentLocale) },
                nativeName = enLocale.getDisplayName(enLocale)
                    .replaceFirstChar { it.uppercase(enLocale) },
            )
        }

        return listOf(english) + locales
    }
}
