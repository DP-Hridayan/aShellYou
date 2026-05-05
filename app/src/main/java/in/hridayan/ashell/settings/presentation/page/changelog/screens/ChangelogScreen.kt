@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.changelog.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.scrollbar.DraggableScrollThumb
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.BulletPointsTextLayout
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.utils.splitStringToLines
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.page.changelog.viewmodel.ChangelogViewModel
import kotlinx.coroutines.delay

@Composable
fun ChangelogScreen(
    modifier: Modifier = Modifier,
    changelogViewModel: ChangelogViewModel = hiltViewModel()
) {
    val changelogs = changelogViewModel.changelogs.value

    val processedChangelogs = remember(changelogs) {
        changelogs.map { item ->
            Triple(
                item,
                item.versionName == BuildConfig.VERSION_NAME.removeSuffix("-debug"),
                splitStringToLines(item.changelog)
            )
        }
    }

    // STAGED LOADING: Start with a few items and load the rest in the background
    // This makes the screen open instantly while the rest of the history "fills in"
    var visibleItemsCount by remember { mutableIntStateOf(5) }

    LaunchedEffect(processedChangelogs) {
        if (processedChangelogs.isNotEmpty()) {
            delay(100) // Let the first 5 items render
            // Load the rest in small batches to keep the main thread smooth
            while (visibleItemsCount < processedChangelogs.size) {
                visibleItemsCount = (visibleItemsCount + 10).coerceAtMost(processedChangelogs.size)
                delay(16) // Wait for about one frame
            }
        }
    }

    val scrollState = rememberScrollState()

    SettingsScaffold(
        modifier = modifier,
        scrollState = scrollState,
        topBarTitle = stringResource(R.string.changelogs),
        content = { innerPadding, topBarScrollBehavior ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                        .verticalScroll(scrollState),
                ) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(innerPadding.calculateTopPadding() + 25.dp)
                    )

                    // Only render the items that are currently "loaded"
                    processedChangelogs.take(visibleItemsCount)
                        .forEach { (item, isLatestVersion, textLines) ->
                            val cardColors = CardDefaults.cardColors(
                                containerColor = if (isLatestVersion) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = if (isLatestVersion) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                            )

                            CustomCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 15.dp),
                                pressedScale = 1f,
                                shape = CardCornerShape.FIRST_CARD,
                                colors = cardColors
                            ) {
                                AutoResizeableText(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp),
                                    text = stringResource(R.string.version) + "\t\t${item.versionName}",
                                    style = if (isLatestVersion) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                                    color = if (isLatestVersion) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                            )

                            CustomCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 15.dp),
                                pressedScale = 1f,
                                shape = CardCornerShape.LAST_CARD,
                                colors = cardColors
                            ) {
                                BulletPointsTextLayout(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp),
                                    textLines = textLines,
                                    textColor = if (isLatestVersion) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
                                    bulletColor = if (isLatestVersion) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                            )
                        }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(innerPadding.calculateBottomPadding() + 25.dp)
                    )
                }

                DraggableScrollThumb(
                    scrollState = scrollState,
                    thumbSize = 42,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(innerPadding)
                        .padding(end = 15.dp)
                )
            }
        })
}
