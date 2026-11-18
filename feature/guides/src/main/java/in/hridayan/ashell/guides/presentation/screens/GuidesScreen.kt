package `in`.hridayan.ashell.guides.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.UrlUtils
import `in`.hridayan.ashell.guides.data.source.GuideCatalog
import `in`.hridayan.ashell.guides.domain.model.GuideCategory
import `in`.hridayan.ashell.guides.domain.model.GuideItem
import `in`.hridayan.ashell.guides.presentation.components.GuideCategorySectionHeader
import `in`.hridayan.ashell.guides.presentation.components.GuideItemCard
import `in`.hridayan.ashell.guides.presentation.viewmodel.GuidesViewModel

@Composable
fun GuidesScreen(
    modifier: Modifier = Modifier,
    viewModel: GuidesViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    GuidesScreenContent(
        modifier = modifier,
        groupedGuides = uiState.groupedGuides,
        onNavigateBack = { navController.navigateBack() },
        onGuideClick = { guide ->
            UrlUtils.openUrl(guide.url, context)
        }
    )
}

@Composable
fun GuidesScreenContent(
    modifier: Modifier = Modifier,
    groupedGuides: Map<GuideCategory, List<GuideItem>>,
    onNavigateBack: () -> Unit,
    onGuideClick: (GuideItem) -> Unit
) {
    val listState = rememberLazyListState()

    AppScaffold(
        modifier = modifier,
        topBarTitle = stringResource(R.string.feature_guides),
        listState = listState,
        onNavigateBack = onNavigateBack,
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding
            ) {
                item {
                    Text(
                        modifier = Modifier.padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 8.dp,
                            bottom = 4.dp
                        ),
                        text = stringResource(R.string.feature_guides_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                GuideCategory.entries.forEach { category ->
                    val categoryGuides = groupedGuides[category].orEmpty()
                    if (categoryGuides.isNotEmpty()) {
                        item(key = category.name) {
                            GuideCategorySectionHeader(
                                modifier = Modifier.padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 30.dp,
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
    )
}

@Preview(showBackground = true)
@Composable
private fun GuidesScreenContentPreview() {
    val sampleGuides = GuideCatalog.getAllGuides().groupBy { it.category }

    MaterialTheme {
        GuidesScreenContent(
            groupedGuides = sampleGuides,
            onNavigateBack = {},
            onGuideClick = {}
        )
    }
}
