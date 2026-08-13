package `in`.hridayan.ashell.shell.common.presentation.components.text

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import `in`.hridayan.ashell.shell.common.presentation.util.highlightQueryText

@Composable
fun OutputLineText(
    modifier: Modifier = Modifier,
    line: OutputLine,
    isSearchVisible: Boolean,
    searchQuery: String,
    textStyle: TextStyle,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    val text = if (!isSearchVisible) {
        line.text
    } else {
        line.text.takeIf {
            line.text.contains(searchQuery, ignoreCase = true)
        }
    }

    val isCommandLine = text?.startsWith("$ ")

    val lineColor = MaterialTheme.colorScheme.run {
        if (isCommandLine == true) {
            primary
        } else if (line.isError) {
            error
        } else {
            onSurface
        }
    }

    text?.let {
        val annotatedText =
            if (isSearchVisible && !searchQuery.isBlank()) {
                val highlightBgColor = MaterialTheme.colorScheme.run {
                    if (line.isError) errorContainer else primaryContainer
                }

                val highlightTextColor = MaterialTheme.colorScheme.run {
                    if (line.isError) onErrorContainer else onPrimaryContainer
                }

                highlightQueryText(
                    text = text,
                    query = searchQuery,
                    highlightBgColor = highlightBgColor,
                    highlightTextColor = highlightTextColor
                )
            } else {
                AnnotatedString(text)
            }

        Text(
            text = annotatedText,
            style = textStyle,
            color = lineColor,
            onTextLayout = onTextLayout,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isCommandLine == true) {
                        Modifier.padding(
                            top = 20.dp,
                            bottom = 10.dp
                        )
                    } else {
                        Modifier
                    }
                )
                .then(modifier)
        )
    }
}
