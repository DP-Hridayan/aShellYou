package `in`.hridayan.ashell.qstiles.data.parser

import `in`.hridayan.ashell.qstiles.data.model.MaterialIconEntry
import javax.inject.Inject

class CodepointsParser @Inject constructor() {
    fun parse(raw: String): List<MaterialIconEntry> {
        return raw.split(NEW_LINE_DELIMITER)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                runCatching {
                    val parts = line.split(SPACE_DELIMITER)
                    val name = parts[NAME_INDEX]
                    val hex = parts[HEX_INDEX]

                    MaterialIconEntry(
                        name = name,
                        codepoint = Integer.parseInt(hex, HEX_RADIX)
                    )
                }.getOrNull()
            }
    }

    companion object {
        private const val HEX_RADIX = 16
        private const val NEW_LINE_DELIMITER = "\n"
        private const val SPACE_DELIMITER = " "
        private const val NAME_INDEX = 0
        private const val HEX_INDEX = 1
    }
}
