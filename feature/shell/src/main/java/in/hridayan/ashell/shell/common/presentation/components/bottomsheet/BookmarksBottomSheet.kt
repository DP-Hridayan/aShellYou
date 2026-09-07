package `in`.hridayan.ashell.shell.common.presentation.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.chip.LabelChip
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.tooltip.TooltipContent
import `in`.hridayan.ashell.core.presentation.model.ButtonConfig
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.utils.isKeyboardVisible
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.common.data.model.BookmarkEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onBookmarkClicked: (command: String) -> Unit,
    onDelete: () -> Unit,
    onDeleteSelectedBookmarks: (Set<Int>) -> Unit,
    onUpdateBookmarksPinState: (Set<Int>, Boolean) -> Unit,
    setSortType: (sortType: Int) -> Unit,
    onSort: () -> Unit,
    bookmarks: List<BookmarkEntity>,
    bookmarkCount: Int,
    searchQuery: TextFieldValue,
    onQueryChanged: (TextFieldValue) -> Unit,
) {
    val sortType = LocalSettings.current[SettingsKeys.BookmarkSortType]

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    val isKeyboardVisible by isKeyboardVisible()

    var selectedBookmarkIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    val inSelectionMode = selectedBookmarkIds.isNotEmpty()

    LaunchedEffect(sortType, Unit) {
        setSortType(sortType)
    }

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = {
            onQueryChanged(TextFieldValue(""))
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

            if (inSelectionMode) {
                val selectedCount = selectedBookmarkIds.size
                val allSelectedPinned =
                    selectedBookmarkIds.all { id -> bookmarks.find { it.id == id }?.isPinned == true }
                val allBookmarksSelected = selectedBookmarkIds.size == bookmarks.size

                SelectionActionsRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedCount = selectedCount,
                    allSelectedPinned = allSelectedPinned,
                    allBookmarksSelected = allBookmarksSelected,
                    onPinUpdate = {
                        onUpdateBookmarksPinState(selectedBookmarkIds, !allSelectedPinned)
                        selectedBookmarkIds = emptySet()
                    },
                    onDelete = {
                        onDeleteSelectedBookmarks(selectedBookmarkIds)
                        selectedBookmarkIds = emptySet()
                    },
                    onSelectToggle = {
                        if (allBookmarksSelected) {
                            selectedBookmarkIds = emptySet()
                        } else {
                            selectedBookmarkIds = bookmarks.map { it.id }.toSet()
                        }
                    },
                    onCancel = {
                        selectedBookmarkIds = emptySet()
                    }
                )
            } else {
                CustomSearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    value = searchQuery,
                    hint = stringResource(R.string.search_bookmarks_here),
                    onValueChange = { onQueryChanged(it) },
                    trailingIcon = {
                        if (searchQuery.text.isNotEmpty()) {
                            IconButton(
                                onClick = withHaptic {
                                    onQueryChanged(TextFieldValue(""))
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_clear),
                                    contentDescription = null,
                                )
                            }
                        } else {
                            IconButton(onClick = withHaptic { onSort() }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_sort),
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                )
            }

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

            val pinnedBookmarks = bookmarks.filter { it.isPinned }
            val unpinnedBookmarks = bookmarks.filter { !it.isPinned }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                if (pinnedBookmarks.isNotEmpty()) {
                    items(pinnedBookmarks.size) { index ->
                        val bookmark = pinnedBookmarks[index]
                        val isSelected = bookmark.id in selectedBookmarkIds
                        val roundedShape = if (isSelected) {
                            CustomCardShape(50)
                        } else {
                            getRoundedShape(index = index, size = pinnedBookmarks.size)
                        }

                        BookmarkItem(
                            bookmark = bookmark,
                            isSelected = isSelected,
                            inSelectionMode = inSelectionMode,
                            roundedShape = roundedShape,
                            containerColor = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            onToggleSelection = {
                                selectedBookmarkIds = if (isSelected) {
                                    selectedBookmarkIds - bookmark.id
                                } else {
                                    selectedBookmarkIds + bookmark.id
                                }
                            },
                            onExecute = {
                                onBookmarkClicked(bookmark.command)
                                onQueryChanged(TextFieldValue(""))
                            },
                            onLongClick = {
                                selectedBookmarkIds = setOf(bookmark.id)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                items(unpinnedBookmarks.size) { index ->
                    val bookmark = unpinnedBookmarks[index]
                    val isSelected = bookmark.id in selectedBookmarkIds
                    val roundedShape = if (isSelected) {
                        CustomCardShape(50)
                    } else {
                        getRoundedShape(index = index, size = unpinnedBookmarks.size)
                    }

                    BookmarkItem(
                        bookmark = bookmark,
                        isSelected = isSelected,
                        inSelectionMode = inSelectionMode,
                        roundedShape = roundedShape,
                        containerColor = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
                        onToggleSelection = {
                            selectedBookmarkIds = if (isSelected) {
                                selectedBookmarkIds - bookmark.id
                            } else {
                                selectedBookmarkIds + bookmark.id
                            }
                        },
                        onExecute = {
                            onBookmarkClicked(bookmark.command)
                            onQueryChanged(TextFieldValue(""))
                        },
                        onLongClick = {
                            selectedBookmarkIds = setOf(bookmark.id)
                        }
                    )
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

@Composable
private fun SelectionActionsRow(
    modifier: Modifier = Modifier,
    selectedCount: Int,
    allSelectedPinned: Boolean,
    allBookmarksSelected: Boolean,
    onPinUpdate: () -> Unit,
    onDelete: () -> Unit,
    onSelectToggle: () -> Unit,
    onCancel: () -> Unit
) {

    Column(modifier = modifier) {
        CustomCard(
            modifier = modifier,
            shape = CardCornerShape.FIRST_CARD,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = {},
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_list_alt),
                        contentDescription = null,
                    )
                }

                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.selected_items, selectedCount),
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        CustomCard(
            modifier = modifier,
            shape = CardCornerShape.LAST_CARD,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                TooltipContent(text = stringResource(if (allSelectedPinned) R.string.unpin_all else R.string.pin_all)) {
                    IconButton(onClick = withHaptic { onPinUpdate() }) {
                        Icon(
                            painter = painterResource(if (allSelectedPinned) R.drawable.ic_unpin else R.drawable.ic_pin),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                TooltipContent(text = stringResource(R.string.delete)) {
                    IconButton(onClick = withHaptic { onDelete() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                TooltipContent(text = stringResource(if (allBookmarksSelected) R.string.deselect_all else R.string.select_all)) {
                    IconButton(onClick = withHaptic { onSelectToggle() }) {
                        Icon(
                            painter = painterResource(if (allBookmarksSelected) R.drawable.ic_deselect_all else R.drawable.ic_select_all),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                TooltipContent(text = stringResource(R.string.cancel_selection)) {
                    IconButton(onClick = withHaptic { onCancel() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cross),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkItem(
    bookmark: BookmarkEntity,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    roundedShape: CustomCardShape,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onToggleSelection: () -> Unit,
    onExecute: () -> Unit,
    onLongClick: () -> Unit
) {
    CustomCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        onClick = withHaptic {
            if (inSelectionMode) {
                onToggleSelection()
            } else {
                onExecute()
            }
        },
        onLongClick = withHaptic(HapticFeedbackType.LongPress) {
            onLongClick()
        },
        shape = roundedShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
            )

            Text(
                text = bookmark.command,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null
                )
            }
        }
    }
}
