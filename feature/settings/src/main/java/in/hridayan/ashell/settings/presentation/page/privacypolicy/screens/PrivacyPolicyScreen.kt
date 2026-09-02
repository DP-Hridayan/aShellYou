@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.privacypolicy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.privacypolicy.model.PolicyBlock
import `in`.hridayan.ashell.settings.presentation.page.privacypolicy.viewmodel.PrivacyPolicyViewModel

@Composable
fun PrivacyPolicyScreen(
    modifier: Modifier = Modifier,
    viewModel: PrivacyPolicyViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val listState = rememberLazyListState()
    val blocks by viewModel.blocks

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.privacy_policy),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                contentPadding = innerPadding,
            ) {
                itemsIndexed(
                    items = blocks,
                    key = { index, _ -> index },
                    contentType = { _, block ->
                        when (block) {
                            is PolicyBlock.Heading -> "heading"
                            is PolicyBlock.Paragraph -> "paragraph"
                            is PolicyBlock.BulletItem -> "bullet"
                            is PolicyBlock.TableData -> "table"
                            is PolicyBlock.BlockQuote -> "blockquote"
                            PolicyBlock.HorizontalRule -> "divider"
                            PolicyBlock.BlankLine -> "blank"
                        }
                    },
                ) { _, block ->
                    PolicyBlockView(
                        block = block,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        },
    )
}


@Composable
private fun PolicyBlockView(block: PolicyBlock, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography

    when (block) {

        is PolicyBlock.Heading -> {
            val (style, color, topPad) = when (block.level) {
                1 -> Triple(tp.headlineMedium.copy(fontWeight = FontWeight.Bold), cs.primary, 0.dp)
                2 -> Triple(tp.titleLarge.copy(fontWeight = FontWeight.Bold), cs.primary, 16.dp)
                3 -> Triple(
                    tp.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    cs.tertiary,
                    12.dp
                )

                else -> Triple(
                    tp.titleSmall.copy(fontWeight = FontWeight.Medium),
                    cs.tertiary,
                    8.dp
                )
            }
            Text(
                text = block.text,
                style = style,
                color = color,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = topPad, bottom = 2.dp),
            )
        }

        is PolicyBlock.Paragraph -> {
            InlineText(
                text = block.text,
                style = tp.bodyMedium.copy(color = cs.onSurface),
                modifier = modifier.fillMaxWidth(),
            )
        }

        is PolicyBlock.BulletItem -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = (block.depth * 16).dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                val isTopLevel = block.depth == 0
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .alignBy { it.measuredHeight }
                        .then(
                            if (isTopLevel) Modifier.background(
                                cs.primary,
                                androidx.compose.foundation.shape.CircleShape
                            )
                            else Modifier.border(
                                2.dp,
                                cs.primary,
                                androidx.compose.foundation.shape.CircleShape
                            )
                        )
                )

                InlineText(
                    text = block.text,
                    style = tp.bodyMedium.copy(color = cs.onSurface),
                    modifier = Modifier
                        .weight(1f)
                        .alignBy(androidx.compose.ui.layout.FirstBaseline),
                )
            }
        }

        is PolicyBlock.TableData -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerLow),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cs.primaryContainer)
                        .height(IntrinsicSize.Min),
                ) {
                    block.headers.forEachIndexed { idx, header ->
                        if (idx > 0) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(cs.primary.copy(alpha = 0.3f)),
                            )
                        }
                        InlineText(
                            text = header,
                            style = tp.labelMedium.copy(
                                color = cs.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }

                HorizontalDivider(color = cs.outlineVariant)

                block.rows.forEachIndexed { rowIdx, row ->
                    if (rowIdx > 0) HorizontalDivider(color = cs.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                    ) {
                        val cellCount = block.headers.size.coerceAtLeast(row.size)
                        repeat(cellCount) { colIdx ->
                            if (colIdx > 0) {
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(cs.outlineVariant),
                                )
                            }
                            InlineText(
                                text = row.getOrElse(colIdx) { "" },
                                style = tp.bodySmall.copy(color = cs.onSurface),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
            }
        }

        is PolicyBlock.BlockQuote -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(IntrinsicSize.Min)
                        .padding(top = 2.dp, bottom = 2.dp)
                        .background(cs.tertiary, MaterialTheme.shapes.small),
                )
                Spacer(modifier = Modifier.width(10.dp))
                InlineText(
                    text = block.text,
                    style = tp.bodyMedium.copy(
                        color = cs.onSurface,
                        fontStyle = FontStyle.Italic,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        PolicyBlock.HorizontalRule -> {
            HorizontalDivider(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                color = cs.outlineVariant,
            )
        }

        PolicyBlock.BlankLine -> {
            Spacer(modifier = modifier.height(8.dp))
        }
    }
}

@Composable
private fun InlineText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme

    val normalSpan = SpanStyle(color = cs.onSurface)
    val boldSpan = SpanStyle(color = cs.onSurface, fontWeight = FontWeight.Bold)
    val italicSpan = SpanStyle(color = cs.onSurface, fontStyle = FontStyle.Italic)

    val codeSpan = SpanStyle(
        color = cs.onSurfaceVariant,
        fontFamily = FontFamily.Monospace,
        background = cs.surfaceContainerHigh,
    )
    val linkSpan = SpanStyle(
        color = cs.primary,
        textDecoration = TextDecoration.Underline,
    )

    val annotated = remember(text) {
        buildInline(text, normalSpan, boldSpan, italicSpan, codeSpan, linkSpan)
    }
    Text(text = annotated, style = style, modifier = modifier)
}

private fun buildInline(
    text: String,
    normal: SpanStyle,
    bold: SpanStyle,
    italic: SpanStyle,
    code: SpanStyle,
    link: SpanStyle,
) = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            // Bold: **text**
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(bold) { append(text.substring(i + 2, end)) }
                    i = end + 2
                } else {
                    withStyle(normal) { append(text[i]) }; i++
                }
            }

            // Italic: *text*
            text[i] == '*' -> {
                val end = text.indexOf('*', i + 1)
                if (end != -1) {
                    withStyle(italic) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else {
                    withStyle(normal) { append(text[i]) }; i++
                }
            }

            // Inline code: `text`
            text[i] == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end != -1) {
                    withStyle(code) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else {
                    withStyle(normal) { append(text[i]) }; i++
                }
            }

            // Link: [text](url) -- uses LinkAnnotation.Url, opened automatically by Text
            text[i] == '[' -> {
                val bEnd = text.indexOf(']', i + 1)
                val pEnd = if (bEnd != -1 && text.getOrNull(bEnd + 1) == '(')
                    text.indexOf(')', bEnd + 2) else -1
                if (bEnd != -1 && pEnd != -1) {
                    val linkText = text.substring(i + 1, bEnd)
                    val url = text.substring(bEnd + 2, pEnd)
                    withLink(
                        LinkAnnotation.Url(
                            url = url,
                            styles = TextLinkStyles(style = link),
                        )
                    ) { append(linkText) }
                    i = pEnd + 1
                } else {
                    withStyle(normal) { append(text[i]) }; i++
                }
            }

            else -> {
                withStyle(normal) { append(text[i]) }; i++
            }
        }
    }
}