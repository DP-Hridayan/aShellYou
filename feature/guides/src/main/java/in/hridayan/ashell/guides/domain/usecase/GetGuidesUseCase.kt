package `in`.hridayan.ashell.guides.domain.usecase

import `in`.hridayan.ashell.guides.domain.model.GuideCategory
import `in`.hridayan.ashell.guides.domain.model.GuideItem
import `in`.hridayan.ashell.guides.domain.repository.GuidesRepository
import javax.inject.Inject

class GetGuidesUseCase @Inject constructor(
    private val repository: GuidesRepository
) {
    operator fun invoke(): Map<GuideCategory, List<GuideItem>> {
        return repository.getGuides().groupBy { it.category }
    }
}
