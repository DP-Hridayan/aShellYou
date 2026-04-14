package `in`.hridayan.ashell.qstiles.domain.usecase

import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.domain.processor.TileCommandKeywordProcessor
import `in`.hridayan.ashell.qstiles.domain.scorer.TileIconScorer
import javax.inject.Inject

class GetSuggestedIconsUseCase @Inject constructor(
    private val processor: TileCommandKeywordProcessor,
    private val scorer: TileIconScorer
) {

    operator fun invoke(command: String): List<String> {
        val keywords = processor.extractKeywords(command)

        return scorer
            .scoreIcons(keywords, TileIconProvider.icons)
            .map { it.id }
    }
}