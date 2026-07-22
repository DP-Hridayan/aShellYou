@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.shell.common.presentation.components.bottomsheet


import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.chip.LabelChip
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfig
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.utils.isKeyboardVisible
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.common.presentation.viewmodel.BookmarkViewModel

@Composable
fun BookmarksBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onBookmarkClicked: (command: String) -> Unit,
    onDelete: () -> Unit,
    onSort: () -> Unit,
    bookmarkViewModel: BookmarkViewModel = hiltViewModel(),
) {
    val sortType = LocalSettings.current[SettingsKeys.BookmarkSortType]
    val bookmarks by bookmarkViewModel.searchedBookmarks.collectAsStateWithLifecycle()
    val bookmarkCount by bookmarkViewModel.getBookmarkCount.collectAsState(initial = 0)
    val searchQuery by bookmarkViewModel.bookmarksSearchQuery.collectAsStateWithLifecycle()

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    val isKeyboardVisible by isKeyboardVisible()

    LaunchedEffect(sortType, Unit) {
        bookmarkViewModel.setSortType(sortType)
    }

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = {
            bookmarkViewModel.onSearchQueryChange(TextFieldValue(""))
            onDismiss()
        },
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AutoResizeableText(
                    modifier = Modifier.weight(weight = 1f, fill = false),
                    text = stringResource(R.string.bookmarks),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                LabelChip(label = bookmarkCount.toString())
            }

            CustomSearchBar(
                modifier = Modifier.fillMaxWidth(),
                value = searchQuery,
                hint = stringResource(R.string.search_bookmarks_here),
                onValueChange = { bookmarkViewModel.onSearchQueryChange(it) },
                trailingIcon = {
                    if (searchQuery.text.isNotEmpty()) {
                        Icon(
                            painter = painterResource(R.drawable.ic_clear),
                            contentDescription = null,
                            modifier = Modifier.clickable(
                                enabled = true,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = withHaptic {
                                    bookmarkViewModel.onSearchQueryChange(TextFieldValue(""))
                                }
                            ))
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_sort),
                            contentDescription = null,
                            modifier = Modifier.clickable(
                                enabled = true,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = withHaptic {
                                    onSort()
                                }
                            ))
                    }
                }
            )

            if (bookmarks.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_error),
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = null
                    )

                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        text = stringResource(R.string.no_search_results_found),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                items(bookmarks.size) { index ->
                    val roundedShape = getRoundedShape(
                        index = index,
                        size = bookmarks.size
                    )
                    val bookmark = bookmarks[index]

                    CustomCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        onClick = withHaptic {
                            onBookmarkClicked(bookmark.command)
                        },
                        shape = roundedShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = bookmark.command,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            if (!isKeyboardVisible) {
                OverflowButtonGroup(
                    modifier = Modifier.fillMaxWidth(),
                    items = listOf(
                        ButtonGroupItem(
                            text = stringResource(R.string.delete_all),
                            buttonConfig = ButtonConfig(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ),
                            onClick = withHaptic(HapticFeedbackType.Confirm) {
                                onDelete()
                            }
                        ),
                        ButtonGroupItem(
                            text = stringResource(R.string.dismiss),
                            buttonConfig = ButtonConfig(
                                type = ButtonType.OutlinedButton
                            ),
                            onClick = withHaptic(HapticFeedbackType.Reject) {
                                onDismiss()
                            },
                        )
                    )
                )
            }
        }
    }
}
