@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.page.backup.screens


import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.datastore.preferences.core.emptyPreferences
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.components.dialog.AutoBackupTimePickerDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.SelectBackupFolderDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.SettingsDialogKey
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.ashell.settings.presentation.provider.BackupScreenCustomSlots
import `in`.hridayan.ashell.settings.presentation.state.settingsContent
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.resolver.resolveAll

@Composable
fun BackupSchedulerScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val dialogManager = LocalDialogManager.current
    val prefs by settingsViewModel.preferences.collectAsState(initial = emptyPreferences())
    val settings = LocalSettings.current
    val hapticsEnabled = settings[SettingsKeys.HapticsAndVibration]
    val autoBackupEnabled = settings[SettingsKeys.AutoBackupEnabled]
    val autoBackupFolderName = settings[SettingsKeys.AutoBackupFolderName]
    val autoBackupFolderUri = settings[SettingsKeys.AutoBackupFolderUri]
    val isBackingUp by settingsViewModel.isBackingUp.collectAsState()
    var showFolderDialog by remember { mutableStateOf(false) }
    // Set to true when the folder picker is opened from the "Enable auto backup" toggle
    // so the callback knows to also enable the toggle once a folder is chosen.
    var pendingEnableAfterFolderPick by remember { mutableStateOf(false) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { newUri ->
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            // Release the old grant before taking the new one (Android caps persisted grants per app)
            if (autoBackupFolderUri.isNotEmpty()) {
                runCatching {
                    context.contentResolver.releasePersistableUriPermission(
                        autoBackupFolderUri.toUri(),
                        flags
                    )
                }
            }

            context.contentResolver.takePersistableUriPermission(newUri, flags)

            settingsViewModel.setString(SettingsKeys.AutoBackupFolderUri, newUri.toString())
            val folderName =
                DocumentFile.fromTreeUri(context, newUri)?.name ?: newUri.lastPathSegment ?: ""
            settingsViewModel.setString(SettingsKeys.AutoBackupFolderName, folderName)

            if (pendingEnableAfterFolderPick) {
                pendingEnableAfterFolderPick = false
                settingsViewModel.onToggle(SettingsKeys.AutoBackupEnabled)
                settingsViewModel.rescheduleAutoBackup(enabled = true)
            }
        }
        if (uri == null) pendingEnableAfterFolderPick = false
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.ShowDialog -> dialogManager.show(event.key)
                is SettingsUiEvent.RequestAutoBackupFolderPicker -> folderPickerLauncher.launch(null)
                else -> {}
            }
        }
    }

    val page = remember { settingsViewModel.backupSchedulerPage }
    val resolvedGroups = page.resolveAll()

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.backup_scheduler),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding,
            ) {
                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(15.dp)
                    )
                }

                settingsContent(
                    groups = resolvedGroups,
                    viewModel = settingsViewModel,
                    prefs = prefs,
                    onItemClick = { key ->
                        settingsViewModel.onItemClicked(key as SettingsKeys<*>)
                    },
                    onBooleanToggle = { key ->
                        if (key == SettingsKeys.AutoBackupEnabled &&
                            !autoBackupEnabled &&
                            autoBackupFolderName.isEmpty()
                        ) {
                            // No folder yet — prompt the user to pick one first;
                            // the picker callback will enable the toggle after selection.
                            pendingEnableAfterFolderPick = true
                            showFolderDialog = true
                        } else {
                            settingsViewModel.onToggle(key as SettingsKeys<Boolean>)
                            if (key == SettingsKeys.AutoBackupEnabled) {
                                settingsViewModel.rescheduleAutoBackup(enabled = !autoBackupEnabled)
                            }
                        }
                    },
                    onIntChanged = { key, value ->
                        settingsViewModel.setInt(key as SettingsKeys<Int>, value)
                        if (key == SettingsKeys.AutoBackupFrequency) {
                            settingsViewModel.rescheduleAutoBackup(
                                enabled = autoBackupEnabled,
                                frequency = value,
                            )
                        }
                    },
                    hapticsEnabled = hapticsEnabled,
                    customSlotContent = { slot ->
                        when (slot) {
                            is BackupScreenCustomSlots.SchedulerStatus -> {
                                if (settings[SettingsKeys.AutoBackupEnabled]) {
                                    LastBackupStatusCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 15.dp, vertical = 10.dp),
                                        lastAutoBackupLocalSuccessTime = settings[SettingsKeys.LastAutoBackupLocalSuccessTime],
                                        lastAutoBackupLocalError = settings[SettingsKeys.LastAutoBackupLocalError],
                                        lastAutoBackupCloudSuccessTime = settings[SettingsKeys.LastAutoBackupCloudSuccessTime],
                                        lastAutoBackupCloudError = settings[SettingsKeys.LastAutoBackupCloudError],
                                        isBackingUp = isBackingUp,
                                        onBackupNow = {
                                            if (autoBackupFolderName.isEmpty()) {
                                                showFolderDialog = true
                                            } else {
                                                settingsViewModel.backupNow()
                                            }
                                        },
                                    )
                                }
                            }

                            else -> {}
                        }
                    },
                )

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }
        },
    )

    SettingsDialogKey.AutoBackupTimePicker.createDialog {
        AutoBackupTimePickerDialog(
            initialHour = settings[SettingsKeys.AutoBackupTimeHour],
            initialMinute = settings[SettingsKeys.AutoBackupTimeMinute],
            onDismiss = { dialogManager.dismiss() },
            onConfirm = { hour, minute ->
                settingsViewModel.setInt(SettingsKeys.AutoBackupTimeHour, hour)
                settingsViewModel.setInt(SettingsKeys.AutoBackupTimeMinute, minute)
                settingsViewModel.rescheduleAutoBackup(
                    enabled = autoBackupEnabled,
                    hour = hour,
                    minute = minute,
                )
                dialogManager.dismiss()
            }
        )
    }

    if (showFolderDialog) {
        SelectBackupFolderDialog(
            onDismiss = {
                showFolderDialog = false
                pendingEnableAfterFolderPick = false
            },
            onSelectFolder = {
                showFolderDialog = false
                folderPickerLauncher.launch(null)
            },
        )
    }
}

@Composable
private fun LastBackupStatusCard(
    modifier: Modifier = Modifier,
    lastAutoBackupLocalSuccessTime: String,
    lastAutoBackupLocalError: String,
    lastAutoBackupCloudSuccessTime: String,
    lastAutoBackupCloudError: String,
    isBackingUp: Boolean,
    onBackupNow: () -> Unit,
) {
    CustomCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        ),
        pressedScale = 1f
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.backup_status),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            BackupStatusRow(
                label = stringResource(R.string.last_auto_backup_local),
                successTime = lastAutoBackupLocalSuccessTime,
                error = lastAutoBackupLocalError,
            )

            if (lastAutoBackupCloudSuccessTime.isNotEmpty() ||
                lastAutoBackupCloudError.isNotEmpty()
            ) {
                BackupStatusRow(
                    label = stringResource(R.string.last_auto_backup_cloud),
                    successTime = lastAutoBackupCloudSuccessTime,
                    error = lastAutoBackupCloudError,
                )
            }

            OutlinedButton(
                onClick = onBackupNow,
                enabled = !isBackingUp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isBackingUp) {
                    LoadingIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.backing_up))
                } else {
                    Text(text = stringResource(R.string.backup_now))
                }
            }
        }
    }
}

@Composable
private fun BackupStatusRow(
    label: String,
    successTime: String,
    error: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                error.isNotEmpty() -> {
                    Icon(
                        imageVector = Icons.Outlined.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp),
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                successTime.isNotEmpty() -> {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp),
                    )
                    Text(
                        text = successTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp),
                    )
                    Text(
                        text = stringResource(R.string.no_auto_backup_yet),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}





