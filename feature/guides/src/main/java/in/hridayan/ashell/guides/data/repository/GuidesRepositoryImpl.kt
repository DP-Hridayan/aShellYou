package `in`.hridayan.ashell.guides.data.repository

import `in`.hridayan.ashell.guides.data.source.GuideCatalog
import `in`.hridayan.ashell.guides.domain.model.GuideItem
import `in`.hridayan.ashell.guides.domain.repository.GuidesRepository
import javax.inject.Inject

class GuidesRepositoryImpl @Inject constructor() : GuidesRepository {
    override fun getGuides(): List<GuideItem> {
        return GuideCatalog.getAllGuides()
    }
}
