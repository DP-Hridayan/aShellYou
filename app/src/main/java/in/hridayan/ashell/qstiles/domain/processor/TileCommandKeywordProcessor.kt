package `in`.hridayan.ashell.qstiles.domain.processor

import javax.inject.Inject

class TileCommandKeywordProcessor @Inject constructor() {
    fun extractKeywords(command: String): List<String> {
        return command
            .lowercase()
            .split(" ", "-", "_", ".", "/")
            .filter { it.isNotBlank() }
    }
}