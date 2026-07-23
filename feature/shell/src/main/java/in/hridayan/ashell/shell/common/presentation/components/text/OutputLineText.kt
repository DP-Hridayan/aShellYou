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
import `in`.hridayan.ashell.core.domain.model.OutputLine
import `in`.hridayan.ashell.shell.common.presentation.model.ShellScreenState
import `in`.hridayan.ashell.shell.common.presentation.util.highlightQueryText

@Composable
fun OutputLineText(
    modifier: Modifier = Modifier,
    line: OutputLine,
    states: ShellScreenState,
    textStyle: TextStyle,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    val text = if (!states.search.isVisible) line.text else line.text.takeIf {
        line.text.contains(
            states.search.textFieldValue.text,
            ignoreCase = true
        )
    }

    val isCommandLine = text?.startsWith("$ ")

    val lineColor = MaterialTheme.colorScheme.run {
        if (isCommandLine == true) primary
        else if (line.isError) error
        else onSurface
    }

    text?.let {
        val annotatedText =
            if (states.search.isVisible && !states.search.textFieldValue.text.isBlank()) {

                val highlightBgColor = MaterialTheme.colorScheme.run {
                    if (line.isError) errorContainer else primaryContainer
                }

                val highlightTextColor = MaterialTheme.colorScheme.run {
                    if (line.isError) onErrorContainer else onPrimaryContainer
                }

                highlightQueryText(
                    text = text,
                    query = states.search.textFieldValue.text,
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
                    if (isCommandLine == true) Modifier.padding(
                        top = 20.dp,
                        bottom = 10.dp
                    ) else Modifier
                )
                .then(modifier)
        )
    }
}
