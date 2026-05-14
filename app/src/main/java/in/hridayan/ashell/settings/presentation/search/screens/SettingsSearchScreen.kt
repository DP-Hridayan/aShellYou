@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.search.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.settings.domain.model.SearchableSettingsEntry
import `in`.hridayan.ashell.settings.domain.model.SettingsScreenId
import `in`.hridayan.ashell.settings.presentation.search.viewmodel.SettingsSearchViewModel

@Composable
fun SettingsSearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsSearchViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.filteredResults.collectAsStateWithLifecycle()
    val recentEntries by viewModel.recentEntries.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = query,
                                onQueryChange = viewModel::onQueryChanged,
                                onSearch = {},
                                expanded = false,
                                onExpandedChange = {},
                                placeholder = { Text(stringResource(R.string.search_settings)) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Search, contentDescription = null)
                                },
                                trailingIcon = {
                                    if (query.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                                            Icon(Icons.Rounded.Close, contentDescription = null)
                                        }
                                    }
                                },
                                modifier = Modifier.focusRequester(focusRequester),
                            )
                        },
                        expanded = false,
                        onExpandedChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        content = {},
                    )
                },
                navigationIcon = {
                    IconButton(onClick = withHaptic { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
            ),
        ) {
            if (query.isBlank()) {
                // Show recent searches
                if (recentEntries.isNotEmpty()) {
                    item(key = "recent_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(R.string.recent_searches),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            TextButton(onClick = viewModel::clearRecentSearches) {
                                Text(stringResource(R.string.clear))
                            }
                        }
                    }

                    items(recentEntries, key = { "recent_${it.screenId}_${it.settingsKey.name}" }) { entry ->
                        SearchResultRow(
                            entry = entry,
                            isRecent = true,
                            onClick = {
                                viewModel.onResultClicked(entry)
                                navController.navigate(
                                    entry.screenId.toNavRoute(entry.settingsKey.name)
                                )
                            },
                        )
                    }
                }
            } else {
                // Group results by parent screen
                val grouped = results.groupBy { it.parentScreenTitle }

                if (results.isEmpty()) {
                    item(key = "empty") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp),
                            )
                            Text(
                                text = stringResource(R.string.no_results_found),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                grouped.forEach { (screenTitle, entries) ->
                    item(key = "header_$screenTitle") {
                        Text(
                            text = screenTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                start = 20.dp, end = 20.dp,
                                top = 16.dp, bottom = 4.dp,
                            ),
                        )
                    }

                    items(entries, key = { "result_${it.screenId}_${it.settingsKey.name}" }) { entry ->
                        SearchResultRow(
                            entry = entry,
                            isRecent = false,
                            onClick = {
                                viewModel.onResultClicked(entry)
                                navController.navigate(
                                    entry.screenId.toNavRoute(entry.settingsKey.name)
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    entry: SearchableSettingsEntry,
    isRecent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = withHaptic { onClick() })
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Icon: history icon for recent, setting icon otherwise
        val icon = if (isRecent) {
            Icons.Rounded.History
        } else {
            entry.iconResId?.let { ImageVector.vectorResource(it) }
        }

        Icon(
            imageVector = icon ?: Icons.Rounded.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (entry.description.isNotBlank()) {
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Show breadcrumb for non-Settings-main items
            if (entry.screenId != SettingsScreenId.SETTINGS_MAIN && !isRecent) {
                Text(
                    text = entry.parentScreenTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alpha(0.7f),
                )
            }
        }
    }
}
