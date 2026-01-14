@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.core.presentation.components.bottomsheet

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.BulletPointsTextLayout
import `in`.hridayan.ashell.core.utils.splitStringToLines
import `in`.hridayan.ashell.settings.presentation.page.changelog.viewmodel.ChangelogViewModel

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ChangelogBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    changelogViewModel: ChangelogViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val recentChangelog = changelogViewModel.changelogs.value.take(3)

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        Text(
            modifier = Modifier.padding(20.dp),
            text = stringResource(R.string.whats_new),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = screenHeight * 0.5f),
            ) {
                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }

                itemsIndexed(items = recentChangelog) { index, item ->
                    val isLatestVersion =
                        item.versionName == BuildConfig.VERSION_NAME.removeSuffix("-debug")

                    AutoResizeableText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp),
                        text = stringResource(R.string.version) + "\t\t${item.versionName}",
                        style =
                            if (isLatestVersion) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                        color =
                            if (isLatestVersion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    BulletPointsTextLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(25.dp),
                        textLines = splitStringToLines(item.changelog)
                    )

                    if (index != recentChangelog.lastIndex) {
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
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
        }
    }
}