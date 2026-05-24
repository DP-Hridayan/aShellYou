@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.page.autoupdate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.presentation.components.bottomsheet.UpdateBottomSheet
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.progress.LoadingSpinner
import `in`.hridayan.ashell.core.presentation.components.shape.SineWaveShape
import `in`.hridayan.ashell.core.presentation.components.shape.WaveEdge
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.UpdateResult
import `in`.hridayan.ashell.settings.presentation.components.dialog.LatestVersionDialog
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.viewmodel.AutoUpdateViewModel
import `in`.hridayan.ashell.settings.presentation.state.rememberController
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.resolver.resolveAll
import `in`.hridayan.settingsdsl.ui.highlight.rememberHighlightState
import `in`.hridayan.settingsdsl.ui.item.settingsContent

@Composable
fun AutoUpdateScreen(
    modifier: Modifier = Modifier,
    highlightKey: String? = null,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    autoUpdateViewModel: AutoUpdateViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val dialogManager = LocalDialogManager.current
    val hapticsEnabled = LocalSettings.current.isHapticEnabled
    val controller = settingsViewModel.rememberController()
    var showLoading by rememberSaveable { mutableStateOf(false) }
    var showUpdateSheet by rememberSaveable { mutableStateOf(false) }
    var tagName by rememberSaveable { mutableStateOf(BuildConfig.VERSION_NAME) }
    var apkUrl by rememberSaveable { mutableStateOf("") }
    var changelog by rememberSaveable { mutableStateOf("") }
    val networkError = stringResource(R.string.network_error)
    val requestTimeout = stringResource(R.string.request_timeout)
    val unKnownError = stringResource(R.string.unknown_error)

    LaunchedEffect(Unit) {
        autoUpdateViewModel.updateEvents.collect { result ->
            showLoading = false
            when (result) {
                is UpdateResult.Success -> {
                    if (result.isUpdateAvailable) {
                        tagName = result.release.tagName
                        apkUrl = result.release.apkUrl.toString()
                        changelog = result.release.body.toString()
                        showUpdateSheet = true
                    } else {
                        dialogManager.show(DialogKey.Settings.LatestVersion)
                    }
                }

                UpdateResult.NetworkError -> showToast(context, networkError)
                UpdateResult.Timeout -> showToast(context, requestTimeout)
                UpdateResult.UnknownError -> showToast(context, unKnownError)
            }
        }
    }

    val listState = rememberLazyListState()
    val highlightedKey = rememberHighlightState(
        highlightKeyName = highlightKey,
        page = settingsViewModel.autoUpdatePage,
        listState = listState,
        headerItemCount = 1,
        keyResolver = { SettingsKeys.valueOfOrNull(it) },
    )

    val page = remember { settingsViewModel.autoUpdatePage }
    val resolvedGroups = page.resolveAll(highlightedKey = highlightedKey)

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.auto_update),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding,
            ) {
                item { Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)) }

                settingsContent(
                    groups = resolvedGroups,
                    controller = controller,
                    hapticsEnabled = hapticsEnabled
                )

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp)
                            .clip(
                                SineWaveShape(
                                    amplitude = 15f,
                                    frequency = 5f,
                                    edge = WaveEdge.Top
                                )
                            )
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 25.dp, end = 25.dp, top = 35.dp, bottom = 75.dp),
                            verticalArrangement = Arrangement.spacedBy(15.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = stringResource(R.string.pre_release_warning),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = stringResource(R.string.pre_release_warning_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier
                                .fillMaxWidth()
                                .height(25.dp))
                        }
                    }
                }
            }
        },
        fabContent = { expanded ->
            CheckUpdateButton(
                showLoading = showLoading,
                expanded = expanded,
                onClick = withHaptic {
                    autoUpdateViewModel.checkForUpdates()
                    showLoading = true
                },
            )
        },
    )

    if (showUpdateSheet) {
        UpdateBottomSheet(
            onDismiss = { showUpdateSheet = false },
            latestVersion = tagName,
            apkUrl = apkUrl,
            body = changelog
        )
    }

    DialogKey.Settings.LatestVersion.createDialog {
        LatestVersionDialog(onDismiss = { it.dismiss() })
    }
}

@Composable
private fun CheckUpdateButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    expanded: Boolean = true,
    showLoading: Boolean,
) {
    ExtendedFloatingActionButton(
        modifier = modifier.padding(bottom = 10.dp),
        onClick = onClick,
        expanded = expanded,
        icon = {
            if (showLoading) LoadingSpinner(modifier = Modifier.size(24.dp))
            else Icon(imageVector = Icons.Rounded.Update, contentDescription = null)
        },
        text = {
            AutoResizeableText(
                text = stringResource(R.string.check_updates),
                style = MaterialTheme.typography.labelLarge
            )
        },
    )
}
