@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.page.backup.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.utils.getFileNameFromUri
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.model.PreferenceGroup
import `in`.hridayan.ashell.settings.presentation.components.dialog.ResetSettingsDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.RestoreBackupDialog
import `in`.hridayan.ashell.settings.presentation.components.item.PreferenceItemView
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.components.shape.getRoundedShape
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.ashell.settings.presentation.page.backup.viewmodel.BackupAndRestoreViewModel
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun BackupAndRestoreScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    backupAndRestoreViewModel: BackupAndRestoreViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings = settingsViewModel.backupPageList
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var showRestoreBackupDialog by rememberSaveable { mutableStateOf(false) }
    val backupTime by backupAndRestoreViewModel.backupTime.collectAsState()
    val lastBackupTime by settingsViewModel.getString(SettingsKeys.LAST_BACKUP_TIME)
        .collectAsState(initial = "")

    var restoreFileUri by rememberSaveable { mutableStateOf("".toUri()) }

    val launcherBackup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { backupAndRestoreViewModel.performBackup(it) }
    }

    val launcherRestore = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val fileName = getFileNameFromUri(context, it)
            if (fileName?.endsWith(".ashellyou") == true) {
                restoreFileUri = it
                backupAndRestoreViewModel.loadBackupTime(it)
                showRestoreBackupDialog = true
            } else {
                showToast(context, context.getString(R.string.pick_ashellyou_extension))
            }
        }
    }

    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.ShowDialog -> {
                    showResetDialog = event.key == SettingsKeys.RESET_APP_SETTINGS
                }

                is SettingsUiEvent.RequestDocumentUriForBackup -> {
                    backupAndRestoreViewModel.initiateBackup(event.backupOption)
                    launcherBackup.launch("backup_${System.currentTimeMillis()}.ashellyou")
                }

                is SettingsUiEvent.RequestDocumentUriForRestore -> {
                    launcherRestore.launch(arrayOf("application/octet-stream"))
                }

                else -> {}
            }
        }
    }

    LaunchedEffect(Unit) {
        backupAndRestoreViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.ShowToast -> showToast(context, event.message)
                else -> {}
            }
        }
    }

    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.backup_and_restore),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                contentPadding = innerPadding
            ) {
                itemsIndexed(settings) { index, group ->
                    when (group) {
                        is PreferenceGroup.Category -> {
                            Text(
                                text = stringResource(group.categoryNameResId),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .animateItem()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 30.dp,
                                        bottom = 10.dp
                                    )
                            )
                            val visibleItems = group.items.filter { it.isLayoutVisible }

                            visibleItems.forEachIndexed { i, item ->
                                val shape = getRoundedShape(i, visibleItems.size)

                                PreferenceItemView(
                                    item = item,
                                    modifier = Modifier.animateItem(),
                                    roundedShape = shape
                                )
                            }
                        }

                        is PreferenceGroup.Items -> {
                            val visibleItems = group.items.filter { it.isLayoutVisible }

                            visibleItems.forEachIndexed { i, item ->
                                val shape = getRoundedShape(i, visibleItems.size)

                                PreferenceItemView(
                                    item = item,
                                    modifier = Modifier.animateItem(),
                                    roundedShape = shape
                                )
                            }
                        }

                        is PreferenceGroup.CustomComposable -> {
                            LastBackupTimeCard(
                                lastBackupTime = lastBackupTime
                            )
                        }

                        else -> {}
                    }
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }
        })

    if (showResetDialog) {
        ResetSettingsDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = { backupAndRestoreViewModel.resetSettingsToDefault() })
    }

    if (showRestoreBackupDialog) {
        RestoreBackupDialog(
            onDismiss = { showRestoreBackupDialog = false },
            onConfirm = { backupAndRestoreViewModel.performRestore(restoreFileUri) },
            backupTime = backupTime
        )
    }
}

@Composable
private fun LastBackupTimeCard(modifier: Modifier = Modifier, lastBackupTime: String) {

    val (date, time) = (lastBackupTime).split(" ").let {
        Pair(
            it.getOrNull(0) ?: "",
            it.getOrNull(1) ?: ""
        )
    }

    AnimatedVisibility(
        visible = date.isNotEmpty() && time.isNotEmpty(),
        enter = scaleIn(
            animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        ),
        exit = scaleOut(animationSpec = MaterialTheme.motionScheme.slowEffectsSpec())
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 15.dp, bottom = 20.dp)
                .clip(
                    MaterialTheme.shapes.large
                ),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = CardDefaults.outlinedCardBorder()
        ) {
            AutoResizeableText(
                text = stringResource(R.string.last_backup_time) + " : " + time,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 25.dp, vertical = 15.dp)
            )

            AutoResizeableText(
                text = stringResource(R.string.last_backup_date) + " : " + date,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    start = 25.dp,
                    end = 25.dp,
                    bottom = 20.dp
                )
            )
        }
    }
}