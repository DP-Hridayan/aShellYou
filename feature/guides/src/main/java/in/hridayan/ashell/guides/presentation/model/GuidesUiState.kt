package `in`.hridayan.ashell.guides.presentation.model

import `in`.hridayan.ashell.guides.domain.model.GuideCategory
import `in`.hridayan.ashell.guides.domain.model.GuideItem

data class GuidesUiState(
    val groupedGuides: Map<GuideCategory, List<GuideItem>> = emptyMap()
)
