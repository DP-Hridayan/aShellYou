package `in`.hridayan.ashell.ai.presentation.components.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.BoldHighlight
import dev.snipme.highlights.model.ColorHighlight
import dev.snipme.highlights.model.SyntaxThemes
import `in`.hridayan.ashell.ai.presentation.model.MessageComponent
import `in`.hridayan.ashell.ai.presentation.viewmodel.AiChatViewModel
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.ClipboardUtils
import `in`.hridayan.ashell.core.utils.showToast

@Composable
fun MarkdownMessageContent(
    modifier: Modifier = Modifier,
    textColor: Color,
    content: String,
    onUseCommand: (String) -> Unit,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val res = LocalResources.current
    val context = LocalContext.current
    val components = remember(content) { viewModel.parseMarkdown(content) }

    Column(modifier = modifier) {
        components.forEach { component ->
            when (component) {
                is MessageComponent.Text -> {
                    SelectionContainer {
                        Markdown(
                            modifier = Modifier.wrapContentWidth(),
                            content = component.text,
                            colors = markdownColor(text = textColor),
                        )
                    }
                }

                is MessageComponent.CodeBlock -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = component.language.ifBlank { "code" },
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                Row {
                                    TextButton(
                                        onClick = withHaptic {
                                            ClipboardUtils.copyToClipboard(
                                                text = AnnotatedString(component.code).text,
                                                context = context
                                            )

                                            showToast(
                                                context,
                                                res.getString(R.string.copied_to_clipboard)
                                            )
                                        }
                                    ) {
                                        Text(stringResource(R.string.copy))
                                    }

                                    if (isShellLanguage(component.language)) {
                                        TextButton(onClick = withHaptic { onUseCommand(component.code) }) {
                                            Text(stringResource(R.string.use))
                                        }
                                    }
                                }
                            }

                            HorizontalDivider()

                            val isDarkTheme = LocalDarkMode.current

                            val highlights =
                                remember(component.code, component.language, isDarkTheme) {
                                    try {
                                        Highlights.Builder()
                                            .code(component.code)
                                            .theme(
                                                SyntaxThemes.default(isDarkTheme)
                                            )
                                            .build()
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                            val annotatedString = remember(highlights, component.code) {
                                if (highlights != null) {
                                    buildAnnotatedString {
                                        append(component.code)
                                        highlights.getHighlights().forEach { highlight ->
                                            when (highlight) {
                                                is ColorHighlight -> {
                                                    addStyle(
                                                        style = SpanStyle(
                                                            color = Color(highlight.rgb or 0xFF000000.toInt())
                                                        ),
                                                        start = highlight.location.start,
                                                        end = highlight.location.end
                                                    )
                                                }

                                                is BoldHighlight -> {
                                                    addStyle(
                                                        style = SpanStyle(fontWeight = FontWeight.Bold),
                                                        start = highlight.location.start,
                                                        end = highlight.location.end
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    AnnotatedString(component.code)
                                }
                            }

                            SelectionContainer {
                                Text(
                                    text = annotatedString,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isShellLanguage(text: String): Boolean {
    val shellLanguages = listOf(
        "sh",
        "bash",
        "shell",
        "cmd",
        "powershell",
        "zsh",
        ""
    )
    return text.lowercase().trim() in shellLanguages
}
