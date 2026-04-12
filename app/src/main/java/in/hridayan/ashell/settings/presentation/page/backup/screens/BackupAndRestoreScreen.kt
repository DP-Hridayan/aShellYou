@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.page.backup.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.presentation.components.card.RoundedCornerCard
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.shape.CardCornerShape
import `in`.hridayan.ashell.core.presentation.components.shape.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.utils.getFileNameFromUri
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.settings.domain.model.BackupOption
import `in`.hridayan.ashell.settings.domain.model.GoogleUserState
import `in`.hridayan.ashell.settings.presentation.components.dialog.BackupDestinationDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.CloudOperationDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.ResetSettingsDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.RestoreBackupDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.RestoreSourceDialog
import `in`.hridayan.ashell.settings.presentation.components.item.PreferenceItemView
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.ashell.settings.presentation.model.PreferenceGroup
import `in`.hridayan.ashell.settings.presentation.page.backup.viewmodel.BackupAndRestoreViewModel
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun BackupAndRestoreScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    backupAndRestoreViewModel: BackupAndRestoreViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val res = LocalResources.current
    val settings = settingsViewModel.backupPageList
    val dialogManager = LocalDialogManager.current
    val backupTime by backupAndRestoreViewModel.backupTime.collectAsState()
    val lastLocalBackupTime = LocalSettings.current.lastLocalBackupTime
    val lastCloudBackupTime = LocalSettings.current.lastCloudBackupTime
    val isFetchingCloudBackupTime by backupAndRestoreViewModel.isFetchingCloudBackupTime.collectAsState()

    val googleUserState by backupAndRestoreViewModel.googleUserState.collectAsState()
    val isSigningIn by backupAndRestoreViewModel.isSigningIn.collectAsState()
    val cloudOperationMessage by backupAndRestoreViewModel.cloudOperationMessage.collectAsState()

    // Cloud restore confirm state
    val showCloudRestoreConfirm by backupAndRestoreViewModel.showCloudRestoreConfirm.collectAsState()
    val cloudBackupTime by backupAndRestoreViewModel.cloudBackupTime.collectAsState()

    var restoreFileUri by rememberSaveable { mutableStateOf("".toUri()) }

    val launcherBackup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { backupAndRestoreViewModel.performLocalBackup(it) }
    }

    val launcherRestore = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val fileName = getFileNameFromUri(context, it)
            if (fileName?.endsWith(".ashellyou") == true) {
                restoreFileUri = it
                backupAndRestoreViewModel.loadBackupTime(it)
                dialogManager.show(DialogKey.Settings.RestoreBackup)
            } else {
                showToast(context, res.getString(R.string.pick_ashellyou_extension))
            }
        }
    }

    // Consent launcher for Drive scope authorization
    val consentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            backupAndRestoreViewModel.onConsentGranted()
        } else {
            backupAndRestoreViewModel.onConsentDenied()
        }
    }

    // Observe consent requests from Drive repo and launch consent UI
    LaunchedEffect(Unit) {
        backupAndRestoreViewModel.consentIntentSender.collect { intentSender ->
            consentLauncher.launch(
                androidx.activity.result.IntentSenderRequest.Builder(intentSender).build()
            )
        }
    }

    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.ShowDialog -> {
                    dialogManager.show(event.key)
                }

                is SettingsUiEvent.RequestDocumentUriForBackup -> {
                    backupAndRestoreViewModel.initiateBackup(event.backupOption)
                    launcherBackup.launch("backup_${System.currentTimeMillis()}.ashellyou")
                }

                is SettingsUiEvent.RequestDocumentUriForRestore -> {
                    launcherRestore.launch(arrayOf("application/octet-stream"))
                }

                is SettingsUiEvent.RequestGoogleDriveBackup -> {
                    backupAndRestoreViewModel.backupToGoogleDrive(event.backupOption)
                }

                is SettingsUiEvent.RequestGoogleDriveRestore -> {
                    backupAndRestoreViewModel.downloadFromGoogleDrive()
                }

                is SettingsUiEvent.RequestGoogleSignIn -> {
                    backupAndRestoreViewModel.signInWithGoogle()
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
                            when (group.label) {
                                "google_sign_in" -> {
                                    GoogleSignInCard(
                                        isSignedIn = googleUserState.isSignedIn,
                                        userEmail = googleUserState.email,
                                        userName = googleUserState.name,
                                        userPhotoUrl = googleUserState.photoUrl,
                                        isLoading = isSigningIn || cloudOperationMessage != null,
                                        onSignInClick = {
                                            backupAndRestoreViewModel.signInWithGoogle()
                                        },
                                        onSignOutClick = {
                                            backupAndRestoreViewModel.signOut()
                                        }
                                    )
                                }

                                "last_backup_time" -> {
                                    LastBackupTimeCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 20.dp),
                                        lastLocalBackupTime = lastLocalBackupTime,
                                        lastCloudBackupTime = lastCloudBackupTime,
                                        userState = googleUserState,
                                        isFetching = isFetchingCloudBackupTime,
                                    )
                                }
                            }
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

    DialogKey.Settings.ResetSettings.createDialog {
        ResetSettingsDialog(
            onDismiss = { it.dismiss() },
            onConfirm = { backupAndRestoreViewModel.resetSettingsToDefault() })
    }

    DialogKey.Settings.RestoreBackup.createDialog {
        RestoreBackupDialog(
            onDismiss = { it.dismiss() },
            onConfirm = { backupAndRestoreViewModel.performRestore(restoreFileUri) },
            backupTime = backupTime
        )
    }

    DialogKey.Settings.BackupDestination(
        backupOption = backupAndRestoreViewModel.run {
            BackupOption.SETTINGS_AND_DATABASE
        }
    ).createDialog { dialogViewModel ->
        val activeKey = dialogManager.activeDialog
        val backupOption = (activeKey as? DialogKey.Settings.BackupDestination)?.backupOption
            ?: BackupOption.SETTINGS_AND_DATABASE

        BackupDestinationDialog(
            onDismiss = { dialogViewModel.dismiss() },
            onLocalBackup = {
                backupAndRestoreViewModel.initiateBackup(backupOption)
                launcherBackup.launch("backup_${System.currentTimeMillis()}.ashellyou")
            },
            onGoogleDriveBackup = {
                backupAndRestoreViewModel.backupToGoogleDrive(backupOption)
            }
        )
    }

    DialogKey.Settings.RestoreSource.createDialog { dialogViewModel ->
        RestoreSourceDialog(
            onDismiss = { dialogViewModel.dismiss() },
            onLocalRestore = {
                launcherRestore.launch(arrayOf("application/octet-stream"))
            },
            onGoogleDriveRestore = {
                backupAndRestoreViewModel.downloadFromGoogleDrive()
            }
        )
    }

    cloudOperationMessage?.let { message ->
        CloudOperationDialog(message = message)
    }

    if (showCloudRestoreConfirm) {
        RestoreBackupDialog(
            onDismiss = { backupAndRestoreViewModel.cancelCloudRestore() },
            onConfirm = { backupAndRestoreViewModel.confirmCloudRestore() },
            backupTime = cloudBackupTime
        )
    }
}

@Composable
private fun LastBackupTimeCard(
    modifier: Modifier = Modifier,
    lastLocalBackupTime: String,
    lastCloudBackupTime: String,
    userState: GoogleUserState,
    isFetching: Boolean
) {
    val perUserLastCloudBackupTime = when {
        !userState.isSignedIn -> stringResource(R.string.not_signed_in)

        isFetching -> stringResource(R.string.fetching_backup_time)

        lastCloudBackupTime.isNotEmpty() ->
            lastCloudBackupTime.split(" ").let { parts ->
                val date = parts.getOrNull(0).orEmpty()
                val time = parts.getOrNull(1).orEmpty()
                "$date | $time"
            }
        else -> stringResource(R.string.none)
    }

    val formattedLastLocalBackupTime =
        lastLocalBackupTime.split(" ").let { parts ->
            val date = parts.getOrNull(0).orEmpty()
            val time = parts.getOrNull(1).orEmpty()
            "$date | $time"
        }

    Column(modifier = modifier) {
        RoundedCornerCard(
            modifier = Modifier.fillMaxWidth(),
            roundedCornerShape = CardCornerShape.FIRST_CARD
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 25.dp, vertical = 10.dp),
                text = stringResource(R.string.last_backup_details),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        TimeCard(
            modifier = Modifier.fillMaxWidth(),
            roundedCornerShape = CardCornerShape.MIDDLE_CARD,
            icon = painterResource(R.drawable.ic_mobile),
            title = stringResource(R.string.device_backup_local),
            timeDescription = formattedLastLocalBackupTime
        )

        TimeCard(
            modifier = Modifier.fillMaxWidth(),
            roundedCornerShape = CardCornerShape.LAST_CARD,
            icon = painterResource(R.drawable.ic_cloud_done),
            title = stringResource(R.string.cloud_backup_google_drive),
            timeDescription = perUserLastCloudBackupTime
        )
    }
}


@Composable
private fun TimeCard(
    modifier: Modifier = Modifier,
    roundedCornerShape: RoundedCornerShape,
    icon: Painter,
    title: String,
    timeDescription: String,
) {
    RoundedCornerCard(
        modifier = modifier,
        roundedCornerShape = roundedCornerShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )

                Text(
                    text = timeDescription,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.9f)
                )
            }
        }
    }
}