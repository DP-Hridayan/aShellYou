package `in`.hridayan.ashell.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the seam between the settings UI DSL and the settings search DSL.
 *
 * The two declare titles independently by design, which keeps UI graphs idiomatic but lets them
 * drift silently. These checks read source text rather than a running composition, so they cost
 * nothing at runtime and need no hooks in production code.
 */
class SettingsSearchDriftTest {

    @Test
    fun `extraction finds both declarations`() {
        val uiCount = uiTitlesByKey().size
        val searchCount = searchTitlesByKey().size
        assertTrue(
            "Only $uiCount settings items were extracted from the UI DSL. The regex has likely " +
                    "stopped matching, which would make the drift checks pass vacuously.",
            uiCount >= MIN_EXPECTED_ITEMS,
        )
        assertTrue(
            "Only $searchCount entries were extracted from $SEARCH_GRAPH_FILE. The regex has " +
                    "likely stopped matching, which would make the drift checks pass vacuously.",
            searchCount >= MIN_EXPECTED_ITEMS,
        )
    }

    @Test
    fun `every titled settings item is searchable`() {
        val missing = uiTitlesByKey().keys - searchTitlesByKey().keys
        assertTrue(
            "Settings items are rendered but never indexed for search: $missing\n" +
                    "Add an entry(...) for each in $SEARCH_GRAPH_FILE.",
            missing.isEmpty(),
        )
    }

    @Test
    fun `search entries point at real settings items`() {
        val orphans = searchTitlesByKey().keys - uiTitlesByKey().keys - UNTITLED_UI_ITEMS
        assertTrue(
            "Search indexes keys with no matching settings item: $orphans\n" +
                    "Remove them, or add them to UNTITLED_UI_ITEMS if the item renders without a title.",
            orphans.isEmpty(),
        )
    }

    @Test
    fun `search and ui agree on titles`() {
        val ui = uiTitlesByKey()
        val search = searchTitlesByKey()
        val drift = ui.keys.intersect(search.keys)
            .filter { key -> ui.getValue(key).intersect(search.getValue(key)).isEmpty() }
            .map { key -> "$key: ui=${ui.getValue(key)} search=${search.getValue(key)}" }
        assertTrue("Title drift between the UI and search DSLs:\n${drift.joinToString("\n")}", drift.isEmpty())
    }

    @Test
    fun `search dsl does not depend on compose`() {
        Konsist.scopeFromProject()
            .files
            .filter { it.path.replace('\\', '/').contains(SEARCH_PACKAGE_PATH) }
            .assertTrue { file -> file.imports.none { it.name.startsWith("androidx.compose") } }
    }

    private fun uiTitlesByKey(): Map<String, Set<String>> = Konsist.scopeFromProject()
        .files
        .filter { it.text.contains(SETTINGS_COLUMN_MARKER) }
        .flatMap { UI_ITEM_REGEX.findAll(it.text).toList() }
        .groupBy({ it.groupValues[2] }, { it.groupValues[3] })
        .mapValues { it.value.toSet() }

    private fun searchTitlesByKey(): Map<String, Set<String>> = Konsist.scopeFromProject()
        .files
        .filter { it.name == SEARCH_GRAPH_FILE_NAME }
        .flatMap { SEARCH_ENTRY_REGEX.findAll(it.text).toList() }
        .groupBy({ it.groupValues[1] }, { it.groupValues[2] })
        .mapValues { it.value.toSet() }

    private companion object {
        const val SETTINGS_COLUMN_MARKER = "SettingsColumn("
        const val SEARCH_GRAPH_FILE_NAME = "SettingsSearchGraph"
        const val SEARCH_GRAPH_FILE = "SettingsSearchGraph.kt"
        const val SEARCH_PACKAGE_PATH = "settingsdsl/search"
        const val MIN_EXPECTED_ITEMS = 40

        val UI_ITEM_REGEX = Regex(
            """(switchItem|switchBannerItem|clickableItem)\(\s*(SettingsKeys\.\w+)\s*\)\s*\{\s*title\(\s*(R\.string\.\w+)\s*\)"""
        )

        val SEARCH_ENTRY_REGEX = Regex(
            """entry\(\s*key\s*=\s*(SettingsKeys\.\w+)\s*,\s*title\s*=\s*(R\.string\.\w+)"""
        )

        /** Radio and button group items render without a title, so they are titled only in search. */
        val UNTITLED_UI_ITEMS = setOf(
            "SettingsKeys.ThemeMode",
            "SettingsKeys.GithubReleaseType",
            "SettingsKeys.LocalAdbWorkingMode",
            "SettingsKeys.TerminalFontStyle",
            "SettingsKeys.AutoBackupFrequency",
            "SettingsKeys.AutoBackupType",
        )
    }
}
