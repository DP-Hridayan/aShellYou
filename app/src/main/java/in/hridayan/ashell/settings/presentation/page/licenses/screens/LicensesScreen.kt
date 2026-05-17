@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.page.licenses.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.utils.isKeyboardVisible
import `in`.hridayan.ashell.settings.domain.model.LibraryItem
import `in`.hridayan.ashell.settings.domain.model.LicensesUiState
import `in`.hridayan.ashell.settings.presentation.components.dialog.AppLicenseDetailDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.LicenseDetailDialog
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.page.licenses.components.AppLicenseCard
import `in`.hridayan.ashell.settings.presentation.page.licenses.components.LibraryListItem
import `in`.hridayan.ashell.settings.presentation.page.licenses.viewmodel.LicensesViewModel

/**
 * Root composable for the Open Source Licenses screen.
 * Wires the ViewModel to the stateless [LicensesContent] and handles dialogs.
 */
@Composable
fun LicensesScreen(
    modifier: Modifier = Modifier,
    viewModel: LicensesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Track whether the app-licenses dialog is open independently of the ViewModel
    var showAppLicenseDialog by rememberSaveable { mutableStateOf(false) }

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.licenses),
        content = { innerPadding, topBarScrollBehavior ->
            LicensesContent(
                uiState = uiState,
                listState = listState,
                innerPadding = innerPadding,
                modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onLibrarySelected = viewModel::onLibrarySelected,
                onShowAppLicenseDetail = { showAppLicenseDialog = true },
            )
        },
    )

    // App licenses full-text dialog
    if (showAppLicenseDialog) {
        AppLicenseDetailDialog(onDismiss = { showAppLicenseDialog = false })
    }

    uiState.selectedLibrary?.let { library ->
        LicenseDetailDialog(
            library = library,
            onDismiss = viewModel::onDismissDetail,
        )
    }
}

/**
 * Renders the entire screen content.
 * Stateless — receives all data and callbacks from [LicensesScreen].
 */
@Composable
private fun LicensesContent(
    uiState: LicensesUiState,
    listState: LazyListState,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onSearchQueryChanged: (String) -> Unit,
    onLibrarySelected: (LibraryItem) -> Unit,
    onShowAppLicenseDetail: () -> Unit,
) {
    AnimatedContent(
        targetState = uiState.isLoading,
        modifier = modifier.fillMaxSize(),
        label = "licenses_loading_content",
    ) { isLoading ->
        if (isLoading) {
            LicensesLoadingState(innerPadding)
        } else {
            LicensesLoadedState(
                uiState = uiState,
                listState = listState,
                innerPadding = innerPadding,
                onSearchQueryChanged = onSearchQueryChanged,
                onLibrarySelected = onLibrarySelected,
                onShowAppLicenseDetail = onShowAppLicenseDetail,
            )
        }
    }
}

@Composable
private fun LicensesLoadingState(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LoadingIndicator()

            Text(
                text = stringResource(R.string.loading_licenses),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LicensesLoadedState(
    uiState: LicensesUiState,
    listState: LazyListState,
    innerPadding: PaddingValues,
    onSearchQueryChanged: (String) -> Unit,
    onLibrarySelected: (LibraryItem) -> Unit,
    onShowAppLicenseDetail: () -> Unit,
) {
    val isKeyboardVisible = isKeyboardVisible().value

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        contentPadding = innerPadding,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (!isKeyboardVisible) {
            item(key = "app_license_card") {
                AppLicenseCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .padding(top = 16.dp, bottom = 8.dp)
                        .animateItem(
                            fadeInSpec = tween(durationMillis = 300),
                            fadeOutSpec = tween(durationMillis = 200),
                            placementSpec = tween(durationMillis = 300),
                        ),
                    onReadFullText = onShowAppLicenseDetail,
                )
            }
        }

        item(key = "third_party_header") {
            Text(
                text = stringResource(R.string.third_party_libraries),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .animateItem()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
            )
        }

        item(key = "library_count") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Rounded.Gavel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(
                        R.string.library_count,
                        uiState.filteredLibraries.size,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item(key = "search_bar") {
            LicensesSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
                    .padding(horizontal = 15.dp, vertical = 8.dp),
            )
        }

        if (uiState.searchQuery.isNotBlank() && uiState.filteredLibraries.isEmpty()) {
            item(key = "empty_search") {
                LicensesEmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .padding(32.dp),
                )
            }
        }

        itemsIndexed(
            items = uiState.filteredLibraries,
            key = { _, lib -> lib.uniqueId },
        ) { index, library ->
            val shape = getRoundedShape(index, uiState.filteredLibraries.size)

            LibraryListItem(
                library = library,
                shape = shape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 1.dp)
                    .animateItem(),
                onShowDetail = onLibrarySelected,
            )
        }

        item(key = "bottom_spacer") {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
            )
        }
    }
}

@Composable
private fun LicensesSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var active by rememberSaveable { mutableStateOf(false) }

    DockedSearchBar(
        modifier = modifier,
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = { active = false },
                expanded = false,
                onExpandedChange = {},
                placeholder = { Text(stringResource(R.string.search_libraries)) },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        content = {},
    )
}

@Composable
private fun LicensesEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
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
            text = stringResource(R.string.no_libraries_found),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.try_different_search),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}