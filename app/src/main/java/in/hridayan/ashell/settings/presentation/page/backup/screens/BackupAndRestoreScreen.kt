@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.page.backup.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import android.provider.Settings
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDialogManager
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogKey
import `in`.hridayan.ashell.core.presentation.components.dialog.createDialog
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.utils.getFileNameFromUri
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.settings.domain.model.BackupType
import `in`.hridayan.ashell.settings.domain.model.GoogleUserState
import `in`.hridayan.ashell.settings.domain.model.LastBackupData
import `in`.hridayan.ashell.settings.presentation.components.dialog.BackupDestinationDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.CloudOperationDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.GoogleSignOutConfirmationDialog
import `in`.hridayan.ashell.settings.presentation.components.dialog.NoGoogleAccountDialog
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
    val navController = LocalNavController.current
    val settings = settingsViewModel.backupPageList
    val dialogManager = LocalDialogManager.current
    val localBackupTime by backupAndRestoreViewModel.localBackupTime.collectAsState()
    val localBackupType by backupAndRestoreViewModel.localBackupType.collectAsState()

    val cloudBackupTime by backupAndRestoreViewModel.cloudBackupTime.collectAsState()
    val cloudBackupType by backupAndRestoreViewModel.cloudBackupType.collectAsState()

    val lastBackupData by backupAndRestoreViewModel.lastBackupData.collectAsState()

    var isLastBackupDetailsCardExpanded by rememberSaveable { mutableStateOf(false) }

    val googleUserState by backupAndRestoreViewModel.googleUserState.collectAsState()
    val isSigningIn by backupAndRestoreViewModel.isSigningIn.collectAsState()
    val cloudOperationMessage by backupAndRestoreViewModel.cloudOperationMessage.collectAsState()

    val showCloudRestoreConfirm by backupAndRestoreViewModel.showCloudRestoreConfirm.collectAsState()
    val isCloudBackupAvailable = backupAndRestoreViewModel.isCloudBackupAvailable

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

    // Consent launcher for Drive scope authorization (GitHub flavor only)
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
    if (isCloudBackupAvailable) {
        LaunchedEffect(Unit) {
            backupAndRestoreViewModel.consentIntentSender.collect { intentSender ->
                consentLauncher.launch(
                    IntentSenderRequest.Builder(intentSender).build()
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.ShowDialog -> {
                    dialogManager.show(event.key)
                }

                is SettingsUiEvent.RequestDocumentUriForBackup -> {
                    backupAndRestoreViewModel.initiateBackup(event.backupType)
                    launcherBackup.launch("backup_${System.currentTimeMillis()}.ashellyou")
                }

                is SettingsUiEvent.RequestDocumentUriForRestore -> {
                    launcherRestore.launch(arrayOf("application/octet-stream"))
                }

                is SettingsUiEvent.RequestGoogleDriveBackup -> {
                    backupAndRestoreViewModel.backupToGoogleDrive(event.backupType)
                }

                is SettingsUiEvent.RequestGoogleDriveRestore -> {
                    backupAndRestoreViewModel.downloadFromGoogleDrive()
                }

                is SettingsUiEvent.RequestGoogleSignIn -> {
                    backupAndRestoreViewModel.signInWithGoogle()
                }

                is SettingsUiEvent.Navigate -> navController.navigate(event.route)

                else -> {}
            }
        }
    }

    LaunchedEffect(Unit) {
        backupAndRestoreViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.ShowToast -> showToast(context, event.message)
                is SettingsUiEvent.ShowDialog -> dialogManager.show(event.key)
                is SettingsUiEvent.Navigate -> navController.navigate(event.route)
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 15.dp, vertical = 1.dp)
                                        .animateItem(),
                                    shape = shape
                                )
                            }
                        }

                        is PreferenceGroup.Items -> {
                            val visibleItems = group.items.filter { it.isLayoutVisible }

                            visibleItems.forEachIndexed { i, item ->
                                val shape = getRoundedShape(i, visibleItems.size)

                                PreferenceItemView(
                                    item = item,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 15.dp, vertical = 1.dp)
                                        .animateItem(),
                                    shape = shape
                                )
                            }
                        }

                        is PreferenceGroup.CustomComposable -> {
                            when (group.label) {
                                "google_sign_in" -> {
                                    if (isCloudBackupAvailable) {
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
                                                dialogManager.show(DialogKey.Settings.ConfirmGoogleSignOut)
                                            }
                                        )
                                    }
                                }

                                "last_backup_time" -> {
                                    LastBackupTimeCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp, start = 15.dp, end = 15.dp),
                                        isCloudBackupAvailable = isCloudBackupAvailable,
                                        userState = googleUserState,
                                        lastBackupData = lastBackupData,
                                        isExpanded = isLastBackupDetailsCardExpanded,
                                        onClick = withHaptic {
                                            isLastBackupDetailsCardExpanded =
                                                !isLastBackupDetailsCardExpanded
                                        }
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
            backupTime = localBackupTime,
            backupType = localBackupType
        )
    }

    if (isCloudBackupAvailable) {
        DialogKey.Settings.BackupDestination(
            backupType = backupAndRestoreViewModel.run {
                BackupType.SETTINGS_AND_DATABASE
            }
        ).createDialog { dialogViewModel ->
            val activeKey = dialogManager.activeDialog
            val backupType = (activeKey as? DialogKey.Settings.BackupDestination)?.backupType
                ?: BackupType.SETTINGS_AND_DATABASE

            BackupDestinationDialog(
                onDismiss = { dialogViewModel.dismiss() },
                onLocalBackup = {
                    backupAndRestoreViewModel.initiateBackup(backupType)
                    launcherBackup.launch("backup_${System.currentTimeMillis()}.ashellyou")
                },
                onGoogleDriveBackup = {
                    backupAndRestoreViewModel.backupToGoogleDrive(backupType)
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

        DialogKey.Settings.ConfirmGoogleSignOut.createDialog { dialogViewModel ->
            GoogleSignOutConfirmationDialog(
                onDismiss = { dialogViewModel.dismiss() },
                onConfirm = { backupAndRestoreViewModel.signOut() }
            )
        }
    }

    if (isCloudBackupAvailable) {
        cloudOperationMessage?.let { message ->
            CloudOperationDialog(message = message)
        }

        if (showCloudRestoreConfirm) {
            RestoreBackupDialog(
                onDismiss = { backupAndRestoreViewModel.cancelCloudRestore() },
                onConfirm = { backupAndRestoreViewModel.confirmCloudRestore() },
                backupTime = cloudBackupTime,
                backupType = cloudBackupType
            )
        }
    }

    DialogKey.Settings.NoGoogleAccount.createDialog { dialogViewModel ->
        NoGoogleAccountDialog(
            onDismiss = { dialogViewModel.dismiss() },
            onAddAccount = {
                val intent = android.content.Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                    putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                }
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun LastBackupTimeCard(
    modifier: Modifier = Modifier,
    isCloudBackupAvailable: Boolean = false,
    lastBackupData: LastBackupData,
    userState: GoogleUserState,
    isExpanded: Boolean = false,
    onClick: () -> Unit = {}
) {
    val roundedCornerShape =
        if (isExpanded) CardCornerShape.FIRST_CARD else CustomCardShape(50)

    val cloudCardIcon =
        if (userState.isSignedIn) painterResource(R.drawable.ic_cloud_done) else painterResource(R.drawable.ic_cloud_off)

    Column(modifier = modifier.animateContentSize()) {
        CustomCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
            shape = roundedCornerShape
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(horizontal = 25.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                Text(
                    text = stringResource(R.string.last_backup_details),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                val rotateAngle by animateFloatAsState(if (isExpanded) 180f else 0f)

                Icon(
                    painter = painterResource(R.drawable.ic_expand),
                    contentDescription = "Expand",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .rotate(rotateAngle)
                )
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(2.dp))

            TimeCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CardCornerShape.run { if (isCloudBackupAvailable) MIDDLE_CARD else LAST_CARD },
                icon = painterResource(R.drawable.ic_mobile),
                title = stringResource(R.string.device_backup_local),
                backupType = lastBackupData.localType,
                dateTime = lastBackupData.localTime
            )

            if (isCloudBackupAvailable) {
                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp))

                TimeCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardCornerShape.LAST_CARD,
                    icon = cloudCardIcon,
                    title = stringResource(R.string.cloud_backup_google_drive),
                    backupType = lastBackupData.cloudType,
                    dateTime = lastBackupData.cloudTime
                )
            }
        }
    }
}

@Composable
private fun TimeCard(
    modifier: Modifier = Modifier,
    shape: CustomCardShape,
    icon: Painter,
    title: String,
    backupType: String,
    dateTime: String
) {
    val context = LocalContext.current
    val res = LocalResources.current

    val backupTypeText = when (backupType) {
        BackupType.SETTINGS_ONLY.name -> stringResource(R.string.settings_only)
        BackupType.DATABASE_ONLY.name -> stringResource(R.string.databases_only)
        BackupType.SETTINGS_AND_DATABASE.name -> stringResource(R.string.all_data)
        else -> backupType
    }

    CustomCard(
        modifier = modifier,
        shape = shape,
        onClick = withHaptic { showToast(context, res.getString(R.string.have_a_nice_day)) }
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
                    text = stringResource(R.string.backup_type) + " : " + backupTypeText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.7f)
                )

                if (dateTime.isNotEmpty()) {
                    Text(
                        text = dateTime,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.alpha(0.7f)
                    )
                }
            }
        }
    }
}