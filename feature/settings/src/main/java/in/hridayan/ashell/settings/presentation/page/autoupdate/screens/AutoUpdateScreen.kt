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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.datastore.preferences.core.emptyPreferences
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.progress.LoadingSpinner
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.components.shape.SineWaveShape
import `in`.hridayan.ashell.core.presentation.components.shape.WaveEdge
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.provider.RadioGroupOptionsProvider
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.settings.domain.model.UpdateResult
import `in`.hridayan.ashell.settings.presentation.components.bottomsheet.UpdateBottomSheet
import `in`.hridayan.ashell.settings.presentation.components.dialog.LatestVersionDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.SettingsDialogKey
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.viewmodel.AutoUpdateViewModel
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.ui.SettingsColumn

private const val ITEM_KEY_TOP_SPACER = "topSpacer"
private const val ITEM_KEY_WARNING_BOX = "warningBox"

@Composable
fun AutoUpdateScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    autoUpdateViewModel: AutoUpdateViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val dialogManager = LocalDialogManager.current
    val hapticsEnabled = LocalSettings.current[SettingsKeys.HapticsAndVibration]
    val prefs by settingsViewModel.preferences.collectAsState(initial = emptyPreferences())
    var showLoading by rememberSaveable { mutableStateOf(false) }
    var showUpdateSheet by rememberSaveable { mutableStateOf(false) }
    var tagName by rememberSaveable {
        mutableStateOf(
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            ).versionName ?: ""
        )
    }
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
                        dialogManager.show(SettingsDialogKey.LatestVersion)
                    }
                }

                UpdateResult.NetworkError -> showToast(context, networkError)
                UpdateResult.Timeout -> showToast(context, requestTimeout)
                UpdateResult.UnknownError -> showToast(context, unKnownError)
            }
        }
    }

    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topAppBarState = topAppBarState,
        topBarTitle = stringResource(R.string.auto_update),
        content = { innerPadding, topBarScrollBehavior ->
            SettingsColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                listState = listState,
                contentPadding = innerPadding,
                topAppBarState = topAppBarState,
                hapticsEnabled = hapticsEnabled,
            ) {
                item(ITEM_KEY_TOP_SPACER) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(15.dp)
                    )
                }

                group {
                    switchBannerItem(SettingsKeys.AutoUpdate) {
                        title(R.string.enable_auto_update)
                    }
                }

                group(R.string.update_channel) {
                    radioGroupItem(SettingsKeys.GithubReleaseType) {
                        options(RadioGroupOptionsProvider.updateChannelOptions)
                        onIntChanged { key, value ->
                            @Suppress("UNCHECKED_CAST")
                            settingsViewModel.setInt(key as SettingsKeys<Int>, value)
                        }
                    }
                }

                group(R.string.additional_settings) {
                    switchItem(SettingsKeys.EnableDirectDownload) {
                        title(R.string.enable_direct_download)
                        description(R.string.des_enable_direct_download)
                        icon(Icons.Rounded.Downloading)
                    }
                }

                item(ITEM_KEY_WARNING_BOX) {
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
                                .padding(
                                    start = 25.dp,
                                    end = 25.dp,
                                    top = 35.dp,
                                    bottom = 75.dp
                                ),
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
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(25.dp)
                            )
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
                    autoUpdateViewModel.checkForUpdates(
                        context.packageManager.getPackageInfo(
                            context.packageName,
                            0
                        ).versionName ?: " "
                    )
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

    SettingsDialogKey.LatestVersion.createDialog {
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
            if (showLoading) {
                LoadingSpinner(modifier = Modifier.size(24.dp))
            } else {
                Icon(imageVector = Icons.Rounded.Update, contentDescription = null)
            }
        },
        text = {
            AutoResizeableText(
                text = stringResource(R.string.check_updates),
                style = MaterialTheme.typography.labelLarge
            )
        },
    )
}
