package `in`.hridayan.ashell.guides.domain.repository

import `in`.hridayan.ashell.guides.domain.model.GuideItem

interface GuidesRepository {
    fun getGuides(): List<GuideItem>
}
