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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.presentation.model.LogcatTab

/**
 * Tabs + search bar placed directly below [LogcatTopBar].
 *
 * - Tab row is always visible.
 * - Search bar appears/disappears based on [searchVisible], with a clear button once typed in.
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
                    onClick = withHaptic { onTabSelected(tab) },
                    text = { Text(stringResource(tab.labelRes())) },
                )
            }
        }

        AnimatedVisibility(
            visible = searchVisible,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            CustomSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                hint = stringResource(R.string.logcat_filter_search_hint),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) ClearSearchButton(onClear = { onSearchQueryChange("") })
                },
            )
        }
    }
}

@Composable
private fun ClearSearchButton(onClear: () -> Unit) {
    IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) { onClear() }) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.clear),
        )
    }
}

private fun LogcatTab.labelRes(): Int = when (this) {
    LogcatTab.THIS_DEVICE -> R.string.this_device
    LogcatTab.OTHER_DEVICE -> R.string.other_device
}
