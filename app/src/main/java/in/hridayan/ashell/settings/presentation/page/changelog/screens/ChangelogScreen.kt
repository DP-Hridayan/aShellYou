@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.changelog.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.BulletPointsTextLayout
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
            LazyColumn(
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

                    AutoResizeableText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp),
                        text = stringResource(R.string.version) + "\t\t${item.versionName}",
                        style = if (isLatestVersion) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                        color = if (isLatestVersion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    BulletPointsTextLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(25.dp),
                        textLines = splitStringToLines(item.changelog)
                    )

                    if (index != changelogs.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(25.dp)
                                .alpha(0.5f),
                            color = MaterialTheme.colorScheme.onSurface,
                            thickness = 1.dp
                        )
                    }
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }
        })
}
