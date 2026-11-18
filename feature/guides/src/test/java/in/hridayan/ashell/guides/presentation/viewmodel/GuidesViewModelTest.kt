package `in`.hridayan.ashell.guides.presentation.viewmodel

import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.guides.domain.model.GuideCategory
import `in`.hridayan.ashell.guides.domain.model.GuideItem
import `in`.hridayan.ashell.guides.domain.repository.GuidesRepository
import `in`.hridayan.ashell.guides.domain.usecase.GetGuidesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidesViewModelTest {

    private class FakeGuidesRepository(private val items: List<GuideItem>) : GuidesRepository {
        override fun getGuides(): List<GuideItem> = items
    }

    @Test
    fun uiState_initialState_populatesGroupedGuides() {
        val sampleItems = listOf(
            GuideItem(
                id = "item_1",
                titleRes = R.string.local_adb,
                descriptionRes = R.string.guide_local_adb_desc,
                iconRes = R.drawable.ic_adb,
                url = "https://example.com/item1",
                category = GuideCategory.Connection
            ),
            GuideItem(
                id = "item_2",
                titleRes = R.string.official_wiki,
                descriptionRes = R.string.guide_wiki_desc,
                iconRes = R.drawable.ic_github,
                url = "https://example.com/item2",
                category = GuideCategory.Community
            )
        )

        val repository = FakeGuidesRepository(sampleItems)
        val useCase = GetGuidesUseCase(repository)
        val viewModel = GuidesViewModel(useCase)

        val state = viewModel.uiState.value

        assertEquals(2, state.groupedGuides.size)
        assertTrue(state.groupedGuides.containsKey(GuideCategory.Connection))
        assertTrue(state.groupedGuides.containsKey(GuideCategory.Community))
        assertEquals(1, state.groupedGuides[GuideCategory.Connection]?.size)
        assertEquals(1, state.groupedGuides[GuideCategory.Community]?.size)
    }
}
