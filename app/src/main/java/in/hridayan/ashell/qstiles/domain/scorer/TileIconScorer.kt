package `in`.hridayan.ashell.qstiles.domain.scorer

import `in`.hridayan.ashell.qstiles.data.model.TileIcon
import javax.inject.Inject

class TileIconScorer @Inject constructor() {

    fun scoreIcons(
        keywords: List<String>,
        icons: List<TileIcon>
    ): List<TileIcon> {

        val scores = mutableMapOf<TileIcon, Int>()

        for (icon in icons) {
            var score = 0

            for (k in keywords) {
                if (icon.keywords.contains(k)) score += 2
                if (icon.id.contains(k)) score += 1
            }

            if (score > 0) scores[icon] = score
        }

        return scores
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }
}