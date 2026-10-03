@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.screens

import android.content.Context
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.ToastUtils
import `in`.hridayan.ashell.logcat.presentation.components.bottomsheet.AppPickerBottomSheet
import `in`.hridayan.ashell.logcat.presentation.components.filter.FilterEditorContent
import `in`.hridayan.ashell.logcat.presentation.event.FilterEditorEvent
import `in`.hridayan.ashell.logcat.presentation.model.FilterEditorActions
import `in`.hridayan.ashell.logcat.presentation.viewmodel.LogcatFilterEditorViewModel

/** Creates a filter profile, or edits one when opened with a profile id. */
@Composable
fun LogcatFilterEditorScreen(
    modifier: Modifier = Modifier,
    viewModel: LogcatFilterEditorViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val form by viewModel.form.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    var showAppPicker by rememberSaveable { mutableStateOf(false) }

    val actions = remember(viewModel, navController) {
        FilterEditorActions(
            onNameChange = viewModel::onNameChange,
            onModeChange = viewModel::onModeChange,
            onLevelToggle = viewModel::onLevelToggle,
            onTagsChange = viewModel::onTagsChange,
            onPidsChange = viewModel::onPidsChange,
            onTidsChange = viewModel::onTidsChange,
            onPackagesChange = viewModel::onPackagesChange,
            onPickApps = {
                viewModel.loadInstalledApps()
                showAppPicker = true
            },
            onCancel = { navController.navigateBack() },
            onSave = viewModel::save,
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event -> handleEvent(event, navController, context) }
    }

    AppScaffold(
        modifier = modifier,
        topBarTitle = stringResource(
            if (viewModel.isEditing) R.string.edit_profile else R.string.new_profile
        ),
        listState = listState,
        topAppBarState = topAppBarState,
        onNavigateBack = { navController.navigateBack() },
        content = { innerPadding, topBarScrollBehavior ->
            FilterEditorContent(
                form = form,
                actions = actions,
                listState = listState,
                contentPadding = innerPadding,
                modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
            )
        },
    )

    if (showAppPicker) {
        AppPicker(
            viewModel = viewModel,
            selectedPackages = form.selectedPackages,
            onDismiss = { showAppPicker = false },
        )
    }
}

@Composable
private fun AppPicker(
    viewModel: LogcatFilterEditorViewModel,
    selectedPackages: Set<String>,
    onDismiss: () -> Unit,
) {
    val apps by viewModel.visibleApps.collectAsStateWithLifecycle()
    val query by viewModel.appQuery.collectAsStateWithLifecycle()
    AppPickerBottomSheet(
        apps = apps,
        query = query,
        selectedPackages = selectedPackages,
        onQueryChange = viewModel::onAppQueryChange,
        onToggle = viewModel::onPackageToggle,
        onDismiss = onDismiss,
    )
}

private fun handleEvent(event: FilterEditorEvent, navController: NavController, context: Context) {
    when (event) {
        FilterEditorEvent.Saved -> navController.navigateBack()
        FilterEditorEvent.SaveFailed ->
            ToastUtils.makeToast(context, context.getString(R.string.profile_save_failed))
    }
}
