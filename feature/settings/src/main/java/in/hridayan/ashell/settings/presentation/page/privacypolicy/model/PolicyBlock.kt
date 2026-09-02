package `in`.hridayan.ashell.settings.presentation.page.privacypolicy.model

sealed class PolicyBlock {
    data class Heading(val level: Int, val text: String) : PolicyBlock()
    data class Paragraph(val text: String) : PolicyBlock()
    data class BulletItem(val text: String, val depth: Int) : PolicyBlock()
    data class TableData(val headers: List<String>, val rows: List<List<String>>) : PolicyBlock()
    data class BlockQuote(val text: String) : PolicyBlock()
    object HorizontalRule : PolicyBlock()
    object BlankLine : PolicyBlock()
}