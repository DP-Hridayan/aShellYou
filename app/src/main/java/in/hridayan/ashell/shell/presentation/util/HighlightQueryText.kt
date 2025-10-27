package `in`.hridayan.ashell.shell.presentation.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun highlightQueryText(
    text: String,
    query: String,
    highlightBgColor: Color = MaterialTheme.colorScheme.primaryContainer,
    highlightTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)

    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()

    val startIndexes = buildList {
        var startIndex = lowerText.indexOf(lowerQuery)
        while (startIndex >= 0) {
            add(startIndex)
            startIndex = lowerText.indexOf(lowerQuery, startIndex + lowerQuery.length)
        }
    }

    return buildAnnotatedString {
        var currentIndex = 0
        for (start in startIndexes) {
            val end = start + query.length
            append(text.substring(currentIndex, start))
            withStyle(
                SpanStyle(
                    background = highlightBgColor,
                    color = highlightTextColor,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(start, end))
            }
            currentIndex = end
        }
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
