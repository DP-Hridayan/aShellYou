@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.qstiles.presentation.components.bottomsheet

import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.noSearchResult
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.qstiles.data.model.MaterialIconEntry
import `in`.hridayan.ashell.qstiles.domain.model.FontLoadState
import `in`.hridayan.ashell.qstiles.presentation.components.icon.MaterialIconGlyph

private const val ICON_CELL_SIZE = 48
private const val GLYPH_FONT_SIZE = 24

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconChooserBottomSheet(
    onDismiss: () -> Unit,
    icons: List<MaterialIconEntry>,
    fontLoadState: FontLoadState,
    searchQuery: TextFieldValue,
    selectedIconId: String,
    onQueryChange: (TextFieldValue) -> Unit,
    onIconSelected: (MaterialIconEntry) -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Expanded,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    val focusManager = LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.choose_icon),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            when (fontLoadState) {
                is FontLoadState.Ready -> IconGrid(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .fillMaxHeight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    icons = icons,
                    typeface = fontLoadState.typeface,
                    selectedIconId = selectedIconId,
                    onIconSelected = onIconSelected,
                )

                is FontLoadState.Loading -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IconGrid(
    modifier: Modifier = Modifier,
    icons: List<MaterialIconEntry>,
    typeface: Typeface,
    selectedIconId: String,
    onIconSelected: (MaterialIconEntry) -> Unit,
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
        modifier = modifier,
        columns = GridCells.Adaptive(ICON_CELL_SIZE.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(icons, key = { it.name }) { entry ->
            val isSelected = entry.name == selectedIconId

            val containerColor = MaterialTheme.colorScheme.run {
                if (isSelected) primaryContainer else surfaceContainerLowest
            }

            val contentColor = MaterialTheme.colorScheme.run {
                if (isSelected) onPrimaryContainer else onSurface
            }

            IconCell(
                containerColor = containerColor,
                onClick = { onIconSelected(entry) },
            ) {
                MaterialIconGlyph(
                    typeface = typeface,
                    codepoint = entry.codepoint,
                    color = contentColor,
                    fontSize = GLYPH_FONT_SIZE.sp,
                )
            }
        }
    }
}

@Composable
private fun IconCell(
    containerColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .size(ICON_CELL_SIZE.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = withHaptic { onClick() }),
        contentAlignment = Alignment.Center,
    ) {
        content()
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
