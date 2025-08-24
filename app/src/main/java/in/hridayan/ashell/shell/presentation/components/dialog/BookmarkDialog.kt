@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.presentation.components.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.viewmodel.BookmarkViewModel
import `in`.hridayan.ashell.settings.presentation.components.card.RoundedCornerCard
import `in`.hridayan.ashell.settings.presentation.components.shape.getRoundedShape

@Composable
fun BookmarkDialog(
    modifier: Modifier = Modifier,
    onBookmarkClicked: (command: String) -> Unit,
    onDelete: () -> Unit,
    onSort: () -> Unit,
    onDismiss: () -> Unit,
    bookmarkViewModel: BookmarkViewModel = hiltViewModel(),
) {
    val weakHaptic = LocalWeakHaptic.current
    val sortType = LocalSettings.current.bookmarkSortType
    val bookmarks = bookmarkViewModel.getAllBookmarks(sortType).value
    val bookmarkCount by bookmarkViewModel.getBookmarkCount.collectAsState(initial = 0)

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = modifier
                    .padding(vertical = 24.dp)
                    .widthIn(min = 280.dp)
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.bookmarks) + " ($bookmarkCount)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    items(bookmarks.size) { index ->
                        val roundedShape = getRoundedShape(
                            index = index,
                            size = bookmarks.size
                        )
                        val bookmark = bookmarks[index]

                        RoundedCornerCard(
                            roundedShape = roundedShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = true, onClick = {
                                    weakHaptic()
                                    onBookmarkClicked(bookmark.command)
                                }),
                            paddingValues = PaddingValues(vertical = 1.dp),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Text(
                                text = bookmark.command,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        modifier = Modifier,
                        onClick = {
                            weakHaptic()
                            onDelete()
                        },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        AutoResizeableText(
                            text = stringResource(R.string.delete_all),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    TextButton(
                        modifier = Modifier,
                        onClick = {
                            weakHaptic()
                            onSort()
                        },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        AutoResizeableText(text = stringResource(R.string.sort))
                    }

                    TextButton(
                        modifier = Modifier,
                        onClick = {
                            weakHaptic()
                            onDismiss()
                        },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        AutoResizeableText(text = stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}
