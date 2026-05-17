package `in`.hridayan.ashell.settings.presentation.page.languages.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.settings.domain.model.SupportedLocale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.xmlpull.v1.XmlPullParser
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LanguagesViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    /** All supported locales, English first, then sorted alphabetically by displayName. */
    val supportedLocales: List<SupportedLocale> = buildSupportedLocales(application)

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

    companion object {

        @Serializable
        private data class TranslationProgressEntry(
            val languageId: String,
            val languageName: String,
            val androidCode: String,
            val editorCode: String,
            val locale: String,
            val translationProgress: Int,
            val approvalProgress: Int,
        )

        /**
         * Parses `crowdin/translation_progress.json` from assets and builds a
         * lookup map keyed by both the full locale tag (e.g. "ar-SA") and the
         * short languageId (e.g. "ar") so we can match against whichever format
         * the auto-generated locale config uses.
         */
        private fun loadTranslationProgressMap(context: Context): Map<String, Int> {
            return try {
                val json = context.assets
                    .open("crowdin/translation_progress.json")
                    .bufferedReader()
                    .use { it.readText() }

                val entries: List<TranslationProgressEntry> = Json.decodeFromString(json)
                buildMap {
                    for (entry in entries) {
                        put(entry.locale, entry.translationProgress)
                        put(entry.languageId, entry.translationProgress)
                    }
                }
            } catch (_: Exception) {
                emptyMap()
            }
        }

        /**
         * Looks up translation progress for a given BCP 47 tag.
         * Tries exact match first, then the language-only prefix.
         */
        private fun findProgress(tag: String, progressMap: Map<String, Int>): Int? {
            // Exact match (e.g. "zh-CN")
            progressMap[tag]?.let { return it }
            // Try language-only prefix (e.g. "ar" from "ar-SA")
            val language = tag.substringBefore('-')
            progressMap[language]?.let { return it }
            return null
        }

        /**
         * Reads the auto-generated `_generated_res_locale_config.xml` to discover
         * supported locale tags. This is produced by `generateLocaleConfig = true`
         * in build.gradle.kts, so the list stays in sync with the `values-*` dirs
         * without any manual maintenance.
         */
        @SuppressLint("DiscouragedApi")
        private fun parseLocaleTagsFromConfig(context: Context): List<String> {
            val tags = mutableListOf<String>()
            try {
                val resId = context.resources.getIdentifier(
                    "_generated_res_locale_config", "xml", context.packageName
                )
                if (resId == 0) return tags
                val parser = context.resources.getXml(resId)
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.name == "locale") {
                        val name = parser.getAttributeValue(
                            "http://schemas.android.com/apk/res/android", "name"
                        )
                        if (!name.isNullOrBlank()) {
                            tags.add(name)
                        }
                    }
                }
            } catch (_: Exception) {
                // Fallback: should never happen with generateLocaleConfig = true
            }
            return tags
        }

        private fun buildSupportedLocales(context: Context): List<SupportedLocale> {
            val tags = parseLocaleTagsFromConfig(context)
            val currentLocale = Locale.getDefault()
            val progressMap = loadTranslationProgressMap(context)

            // Separate English (default) from the rest
            val englishTag = tags.firstOrNull { it.startsWith("en") }
            val otherTags = tags.filter { !it.startsWith("en") }

            val otherLocales = otherTags.map { tag ->
                val locale = Locale.forLanguageTag(tag)
                SupportedLocale(
                    tag = tag,
                    displayName = locale.getDisplayName(currentLocale)
                        .replaceFirstChar { it.uppercase(currentLocale) },
                    nativeName = locale.getDisplayName(locale)
                        .replaceFirstChar { it.uppercase(locale) },
                    translationProgress = findProgress(tag, progressMap),
                )
            }.sortedBy { it.displayName.lowercase() }

            // English at the top of the list (source language = 100%)
            val english = run {
                val enLocale = if (englishTag != null) Locale.forLanguageTag(englishTag) else Locale.ENGLISH
                SupportedLocale(
                    tag = englishTag ?: "en",
                    displayName = enLocale.getDisplayName(currentLocale)
                        .replaceFirstChar { it.uppercase(currentLocale) },
                    nativeName = enLocale.getDisplayName(enLocale)
                        .replaceFirstChar { it.uppercase(enLocale) },
                    translationProgress = 100,
                )
            }

            return listOf(english) + otherLocales
        }
    }
}

