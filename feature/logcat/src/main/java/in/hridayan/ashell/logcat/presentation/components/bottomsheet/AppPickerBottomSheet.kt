@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.domain.model.InstalledApp
import `in`.hridayan.ashell.logcat.presentation.components.filter.AppPickerRow

private val ListMaxHeight = 420.dp
private val PlaceholderHeight = 120.dp

/**
 * Picks the apps a filter profile matches, from those installed on this device. Any number can be
 * selected; each tap adds or removes that app's package in the profile.
 *
 * @param apps installed apps matching [query], or null while the list is still loading.
 */
@Composable
fun AppPickerBottomSheet(
    apps: List<InstalledApp>?,
    query: String,
    selectedPackages: Set<String>,
    onQueryChange: (String) -> Unit,
    onToggle: (packageName: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AutoResizeableText(
                text = stringResource(R.string.select_apps),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            CustomSearchBar(
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = onQueryChange,
                hint = stringResource(R.string.search_apps),
            )

            when {
                apps == null -> Placeholder { CircularProgressIndicator() }
                apps.isEmpty() -> Placeholder { NoAppsFound() }
                else -> AppList(apps, selectedPackages, onToggle)
            }

            OverflowButtonGroup(
                items = listOf(
                    ButtonGroupItem(text = stringResource(R.string.done), onClick = onDismiss),
                ),
            )
        }
    }
}

@Composable
private fun AppList(
    apps: List<InstalledApp>,
    selectedPackages: Set<String>,
    onToggle: (packageName: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = ListMaxHeight)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        itemsIndexed(items = apps, key = { _, app -> app.packageName }) { index, app ->
            AppPickerRow(
                app = app,
                isSelected = app.packageName in selectedPackages,
                shape = getRoundedShape(index, apps.size),
                onToggle = onToggle,
            )
        }
    }
}

@Composable
private fun Placeholder(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = PlaceholderHeight),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun NoAppsFound() {
    Text(
        text = stringResource(R.string.no_apps_found),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
