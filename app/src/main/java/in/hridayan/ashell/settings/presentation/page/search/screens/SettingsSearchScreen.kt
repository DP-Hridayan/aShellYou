package `in`.hridayan.ashell.settings.presentation.page.search.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.noSearchResult
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.settings.presentation.page.search.viewmodel.SettingsSearchViewModel
import `in`.hridayan.ashell.settings.presentation.provider.SettingsProvider
import `in`.hridayan.settingsdsl.search.SearchEntry

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

    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(query) {
        if (textFieldValue.text != query) {
            textFieldValue = TextFieldValue(query, TextRange(query.length))
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(modifier = modifier) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
            ),
        ) {
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                )
            }

            item(key = "search_bar") {
                CustomSearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .focusRequester(focusRequester),
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        viewModel.onQueryChanged(newValue.text)
                    },
                    hint = stringResource(R.string.search_settings),
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.clickable(
                                enabled = true,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                    navController.popBackStack()
                                }),
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            Icon(
                                modifier = Modifier.clickable(
                                    enabled = true,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                        textFieldValue = TextFieldValue("")
                                        viewModel.onQueryChanged("")
                                    }),
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null
                            )
                        }
                    },
                )
            }

            if (query.isNotBlank()) {
                // Group results by parent screen
                val grouped = results.groupBy { it.screenTitle }

                if (results.isEmpty()) {
                    item(key = "empty") {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            SearchSomethingUi(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                text = stringResource(R.string.no_search_results_found)
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

                    items(
                        entries,
                        key = { "result_${it.screenId}_${it.key.name}" }) { entry ->
                        SearchResultRow(
                            entry = entry,
                            isRecent = false,
                            onClick = {
                                viewModel.onResultClicked(entry)
                                navController.navigate(
                                    SettingsProvider.resolveNavRoute(entry.screenId, entry.key.name)
                                )
                            },
                        )
                    }
                }
            } else {
                if (recentEntries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            SearchSomethingUi(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                text = buildString {
                                    append(stringResource(R.string.search_something))
                                    append("...")
                                }
                            )
                        }
                    }
                } else {
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

                    items(
                        recentEntries,
                        key = { "recent_${it.screenId}_${it.key.name}" }) { entry ->
                        SearchResultRow(
                            entry = entry,
                            isRecent = true,
                            onClick = {
                                viewModel.onResultClicked(entry)
                                navController.navigate(
                                    SettingsProvider.resolveNavRoute(entry.screenId, entry.key.name)
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
    entry: SearchEntry,
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
        }
    }
}

@Composable
private fun SearchSomethingUi(
    modifier: Modifier = Modifier,
    text: String
) {
    Column(modifier = modifier) {
        Image(
            imageVector = DynamicColorImageVectors.noSearchResult(),
            contentDescription = null,
        )

        AutoResizeableText(
            text = text,
            style = MaterialTheme.typography.bodyMediumEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )
    }
}

