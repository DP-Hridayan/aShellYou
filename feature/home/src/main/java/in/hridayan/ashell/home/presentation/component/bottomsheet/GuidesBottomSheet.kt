@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.home.presentation.component.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.haptic.scrollBottomHaptic
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.UrlUtils
import `in`.hridayan.ashell.home.presentation.model.GuideCatalog
import `in`.hridayan.ashell.home.presentation.model.GuideCategory
import `in`.hridayan.ashell.home.presentation.model.GuideItem

@Composable
fun GuidesBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
) {
    val context = LocalContext.current
    val allGuides = remember { GuideCatalog.getAllGuides() }
    val groupedGuides = remember(allGuides) {
        allGuides.groupBy { it.category }
    }

    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = null,
        onDismissRequest = onDismiss
    ) {
        GuidesBottomSheetContent(
            groupedGuides = groupedGuides,
            onGuideClick = { guide ->
                UrlUtils.openUrl(guide.url, context)
            }
        )
    }
}

@Composable
private fun GuidesBottomSheetContent(
    groupedGuides: Map<GuideCategory, List<GuideItem>>,
    onGuideClick: (GuideItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scrollBottomHaptic()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                GuidesBottomSheetHeader()
            }

            GuideCategory.entries.forEach { category ->
                val categoryGuides = groupedGuides[category].orEmpty()
                if (categoryGuides.isNotEmpty()) {
                    item {
                        GuideCategorySectionHeader(
                            modifier = modifier.padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = 25.dp,
                                bottom = 8.dp
                            ),
                            category = category
                        )
                    }

                    items(
                        count = categoryGuides.size,
                        key = { index -> categoryGuides[index].id }
                    ) { index ->
                        val guide = categoryGuides[index]
                        val shape = CardCornerShape.getRoundedShape(
                            index = index,
                            size = categoryGuides.size
                        )

                        if (index > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        GuideItemCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp),
                            guide = guide,
                            shape = shape,
                            onClick = { onGuideClick(guide) }
                        )
                    }
                }
            }

            item {
                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(28.dp)
                )
            }
        }
    }
}

@Composable
private fun GuidesBottomSheetHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.feature_guides),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.feature_guides_summary),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GuideCategorySectionHeader(
    category: GuideCategory,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = stringResource(category.titleRes),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Preview(showBackground = true)
@Composable
private fun GuidesBottomSheetContentPreview() {
    val allGuides = GuideCatalog.getAllGuides()
    val grouped = allGuides.groupBy { it.category }

    MaterialTheme {
        GuidesBottomSheetContent(
            groupedGuides = grouped,
            onGuideClick = {}
        )
    }
}
