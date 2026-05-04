@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.changelog.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

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

@Composable
fun ChangelogScreen(
    modifier: Modifier = Modifier,
    changelogViewModel: ChangelogViewModel = hiltViewModel()
) {
    val changelogs = changelogViewModel.changelogs.value

    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.changelogs),
        content = { innerPadding, topBarScrollBehavior ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                    contentPadding = innerPadding
                ) {
                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(25.dp)
                        )
                    }

                    itemsIndexed(items = changelogs) { index, item ->
                        val isLatestVersion =
                            item.versionName == BuildConfig.VERSION_NAME.removeSuffix("-debug")

                        val cardColors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.run { if (isLatestVersion) tertiaryContainer else surfaceContainer },
                            contentColor = MaterialTheme.colorScheme.run { if (isLatestVersion) onTertiaryContainer else onSurface }
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
                                style = MaterialTheme.typography.run { if (isLatestVersion) headlineMedium else headlineSmall },
                                color = MaterialTheme.colorScheme.run { if (isLatestVersion) tertiary else onSurface },
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
                                textLines = splitStringToLines(item.changelog),
                                textColor = MaterialTheme.colorScheme.run { if (isLatestVersion) onTertiaryContainer else onSurface },
                                bulletColor = MaterialTheme.colorScheme.run { if (isLatestVersion) tertiary else primary }
                            )
                        }

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                        )
                    }

                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(25.dp)
                        )
                    }
                }

                DraggableScrollThumb(
                    listState = listState,
                    thumbSize = 48,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(innerPadding)
                        .padding(15.dp)
                )
            }
        })
}
