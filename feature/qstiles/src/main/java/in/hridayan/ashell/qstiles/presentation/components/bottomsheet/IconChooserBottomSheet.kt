@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.qstiles.presentation.components.bottomsheet

import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.noSearchResult
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.qstiles.data.model.MaterialIconEntry
import `in`.hridayan.ashell.qstiles.data.model.TileIcon
import `in`.hridayan.ashell.qstiles.domain.model.FontLoadState
import `in`.hridayan.ashell.qstiles.presentation.model.IconTab

private const val GRID_COLUMN_COUNT = 5
private const val ICON_CELL_SIZE = 48
private const val ICON_INNER_SIZE = 24
private const val GLYPH_FONT_SIZE = 24
private const val GRID_HEIGHT = 400
private const val HORIZONTAL_PADDING = 24
private const val BOTTOM_PADDING = 32

@Composable
fun IconChooserBottomSheet(
    onDismiss: () -> Unit,
    bundledIcons: List<TileIcon>,
    materialIcons: List<MaterialIconEntry>,
    fontLoadState: FontLoadState,
    activeTab: IconTab,
    searchQuery: TextFieldValue,
    selectedIconId: String,
    onQueryChange: (TextFieldValue) -> Unit,
    onBundledIconSelected: (String) -> Unit,
    onCloudIconSelected: (MaterialIconEntry) -> Unit,
    onTabChange: (IconTab) -> Unit,
    onDownloadClick: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Expanded,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = HORIZONTAL_PADDING.dp)
                .padding(bottom = BOTTOM_PADDING.dp),
        ) {
            Text(
                text = stringResource(R.string.choose_icon),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomSearchBar(
                modifier = Modifier.fillMaxWidth(),
                value = searchQuery,
                onValueChange = onQueryChange,
                hint = stringResource(R.string.search_icons_here),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Ascii
                ),
                trailingIcon = {
                    if (searchQuery.text.isNotEmpty()) {
                        IconButton(
                            onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                onQueryChange(TextFieldValue(""))
                                focusManager.clearFocus()
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_clear),
                                contentDescription = null,
                            )
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(
                activeTab = activeTab,
                onTabChange = onTabChange,
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (activeTab) {
                IconTab.BUNDLED -> BundledIconGrid(
                    icons = bundledIcons,
                    selectedIconId = selectedIconId,
                    onIconSelected = onBundledIconSelected,
                )

                IconTab.MATERIAL -> MaterialIconContent(
                    icons = materialIcons,
                    fontLoadState = fontLoadState,
                    selectedIconId = selectedIconId,
                    onIconSelected = onCloudIconSelected,
                    onDownloadClick = onDownloadClick,
                )
            }
        }
    }
}

@Composable
private fun TabRow(
    activeTab: IconTab,
    onTabChange: (IconTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = activeTab == IconTab.BUNDLED,
            onClick = withHaptic { onTabChange(IconTab.BUNDLED) },
            label = { Text(stringResource(R.string.bundled)) },
        )
        FilterChip(
            selected = activeTab == IconTab.MATERIAL,
            onClick = withHaptic { onTabChange(IconTab.MATERIAL) },
            label = { Text(stringResource(R.string.material_icons)) },
        )
    }
}

@Composable
private fun BundledIconGrid(
    icons: List<TileIcon>,
    selectedIconId: String,
    onIconSelected: (String) -> Unit,
) {
    if (icons.isEmpty()) {
        NoSearchResultUi(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 24.dp)
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMN_COUNT),
        modifier = Modifier
            .fillMaxWidth()
            .height(GRID_HEIGHT.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(icons, key = { it.id }) { icon ->
            IconCell(
                isSelected = icon.id == selectedIconId,
                onClick = { onIconSelected(icon.id) },
            ) {
                Icon(
                    modifier = Modifier.size(ICON_INNER_SIZE.dp),
                    painter = painterResource(icon.resId),
                    contentDescription = icon.id,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun MaterialIconContent(
    icons: List<MaterialIconEntry>,
    fontLoadState: FontLoadState,
    selectedIconId: String,
    onIconSelected: (MaterialIconEntry) -> Unit,
    onDownloadClick: () -> Unit,
) {
    when (fontLoadState) {
        is FontLoadState.NotDownloaded -> DownloadPrompt(onDownloadClick = onDownloadClick)

        is FontLoadState.Loading -> LoadingIndicator()

        is FontLoadState.Error -> ErrorState(
            message = fontLoadState.message,
            onRetryClick = onDownloadClick,
        )

        is FontLoadState.Ready -> MaterialIconGrid(
            icons = icons,
            typeface = fontLoadState.typeface,
            selectedIconId = selectedIconId,
            onIconSelected = onIconSelected,
        )
    }
}

@Composable
private fun MaterialIconGrid(
    icons: List<MaterialIconEntry>,
    typeface: Typeface,
    selectedIconId: String,
    onIconSelected: (MaterialIconEntry) -> Unit,
) {
    val fontFamily = remember(typeface) {
        FontFamily(androidx.compose.ui.text.font.Typeface(typeface))
    }

    if (icons.isEmpty()) {
        NoSearchResultUi(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 24.dp)
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMN_COUNT),
        modifier = Modifier
            .fillMaxWidth()
            .height(GRID_HEIGHT.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(icons, key = { it.name }) { entry ->
            val cloudIconId = "material:${entry.name}"

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconCell(
                    isSelected = cloudIconId == selectedIconId,
                    onClick = { onIconSelected(entry) },
                ) {
                    Text(
                        text = String(Character.toChars(entry.codepoint)),
                        fontFamily = fontFamily,
                        fontSize = GLYPH_FONT_SIZE.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = entry.name.replace('_', ' '),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(ICON_CELL_SIZE.dp)
                        .padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun IconCell(
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Box(
        modifier = Modifier
            .size(ICON_CELL_SIZE.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = withHaptic { onClick() }),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun DownloadPrompt(onDownloadClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_cloud_download),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = stringResource(R.string.material_icons_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        FilledTonalButton(onClick = withHaptic { onDownloadClick() }) {
            Text(text = stringResource(R.string.download_material_icons))
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CircularProgressIndicator()

        Text(
            text = stringResource(R.string.downloading_icon_pack),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )

        FilledTonalButton(
            onClick = withHaptic { onRetryClick() },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
private fun NoSearchResultUi(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = DynamicColorImageVectors.noSearchResult(),
            contentDescription = null,
        )

        AutoResizeableText(
            text = stringResource(R.string.no_search_results_found),
            style = MaterialTheme.typography.bodyMediumEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
        )
    }
}
