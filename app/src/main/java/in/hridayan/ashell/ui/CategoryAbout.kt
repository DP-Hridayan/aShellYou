package `in`.hridayan.ashell.ui

import `in`.hridayan.ashell.config.Const.Contributors

class CategoryAbout(@JvmField val name: String?) {
    class LeadDeveloperItem(
        @JvmField val title: String?,
        @JvmField val description: String?,
        @JvmField val imageResource: Int
    )

    class ContributorsItem(
        id: Contributors?,
        title: String?,
        description: String?,
        imageResource: Int
    ) {
        private val id: Contributors?

        @JvmField
        val title: String?

        @JvmField
        val description: String?

        @JvmField
        val imageResource: Int

        init {
            this.id = id
            this.title = title
            this.description = description
            this.imageResource = imageResource
        }

        fun getId(): Contributors? {
            return id
        }
    }

    class AppItem(
        @JvmField val id: String?,
        @JvmField val title: String?,
        @JvmField val description: String?,
        @JvmField val imageResource: Int
    )
}
