package `in`.hridayan.ashell.home.presentation.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideCatalogTest {

    @Test
    fun getAllGuides_returnsNonEmptyListWithoutDuplicates() {
        val guides = GuideCatalog.getAllGuides()
        assertTrue(guides.isNotEmpty())

        val ids = guides.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun getAllGuides_allUrlsAreValidHttps() {
        val guides = GuideCatalog.getAllGuides()
        for (guide in guides) {
            assertTrue(guide.url.startsWith("https://"))
        }
    }

    @Test
    fun getAllGuides_allCategoriesAreRepresented() {
        val guides = GuideCatalog.getAllGuides()
        val categories = guides.map { it.category }.toSet()
        assertEquals(GuideCategory.entries.toSet(), categories)
    }

    @Test
    fun getAllGuides_allFeatureGuidesTargetOfficialWikiOrTelegram() {
        val guides = GuideCatalog.getAllGuides()
        for (guide in guides) {
            val isOfficialWiki = guide.url.startsWith("https://github.com/DP-Hridayan/aShellYou/wiki")
            val isTelegram = guide.url.startsWith("https://t.me/")
            assertTrue(isOfficialWiki || isTelegram)
        }
    }
}
