@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.core.presentation.components.bottomsheet

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.settings.presentation.components.item.ChangelogItemLayout
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

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.5f),
        ) {
            itemsIndexed(items = recentChangelog) { index, item ->
                ChangelogItemLayout(
                    modifier = Modifier.padding(25.dp),
                    versionName = item.versionName,
                    changelog = changelogViewModel.splitStringToLines(item.changelog)
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
    }
}