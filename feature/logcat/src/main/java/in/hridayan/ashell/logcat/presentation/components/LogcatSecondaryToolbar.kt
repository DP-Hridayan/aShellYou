@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.presentation.model.LogcatTab

/**
 * Tabs + search bar placed directly below [LogcatTopBar].
 *
 * - Tab row is always visible.
 * - Search field appears/disappears based on [searchVisible].
 */
@Composable
fun LogcatSecondaryToolbar(
    activeTab: LogcatTab,
    onTabSelected: (LogcatTab) -> Unit,
    searchVisible: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Column(modifier = modifier.fillMaxWidth()) {
        PrimaryTabRow(selectedTabIndex = activeTab.ordinal) {
            LogcatTab.entries.forEach { tab ->
                Tab(
                    selected = activeTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(stringResource(tab.labelRes())) },
                )
            }
        }

        AnimatedVisibility(
            visible = searchVisible,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(R.string.logcat_filter_search_hint),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                textStyle = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                shape = MaterialTheme.shapes.medium,
            )
        }
    }
}

private fun LogcatTab.labelRes(): Int = when (this) {
    LogcatTab.THIS_DEVICE -> R.string.this_device
    LogcatTab.OTHER_DEVICE -> R.string.other_device
}
