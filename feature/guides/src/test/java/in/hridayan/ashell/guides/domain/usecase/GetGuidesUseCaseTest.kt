package `in`.hridayan.ashell.guides.domain.usecase

import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.guides.domain.model.GuideCategory
import `in`.hridayan.ashell.guides.domain.model.GuideItem
import `in`.hridayan.ashell.guides.domain.repository.GuidesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetGuidesUseCaseTest {

    private class FakeGuidesRepository(private val items: List<GuideItem>) : GuidesRepository {
        override fun getGuides(): List<GuideItem> = items
    }

    @Test
    fun invoke_groupsGuidesByCategory() {
        val sampleItems = listOf(
            GuideItem(
                id = "guide_1",
                titleRes = R.string.local_adb,
                descriptionRes = R.string.guide_local_adb_desc,
                iconRes = R.drawable.ic_adb,
                url = "https://example.com/1",
                category = GuideCategory.Connection
            ),
            GuideItem(
                id = "guide_2",
                titleRes = R.string.fastboot,
                descriptionRes = R.string.guide_fastboot_desc,
                iconRes = R.drawable.ic_flash_on,
                url = "https://example.com/2",
                category = GuideCategory.Advanced
            ),
            GuideItem(
                id = "guide_3",
                titleRes = R.string.otg,
                descriptionRes = R.string.guide_otg_adb_desc,
                iconRes = R.drawable.ic_otg,
                url = "https://example.com/3",
                category = GuideCategory.Connection
            )
        )

        val repository = FakeGuidesRepository(sampleItems)
        val useCase = GetGuidesUseCase(repository)

        val result = useCase()

        assertEquals(2, result.size)
        assertEquals(2, result[GuideCategory.Connection]?.size)
        assertEquals(1, result[GuideCategory.Advanced]?.size)
        assertTrue(result.containsKey(GuideCategory.Connection))
        assertTrue(result.containsKey(GuideCategory.Advanced))
    }

    @Test
    fun invoke_whenRepositoryReturnsEmpty_returnsEmptyMap() {
        val repository = FakeGuidesRepository(emptyList())
        val useCase = GetGuidesUseCase(repository)

        val result = useCase()

        assertTrue(result.isEmpty())
    }
}
