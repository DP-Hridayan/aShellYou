package `in`.hridayan.ashell.settings.presentation.page.privacyandsecurity.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.common.domain.model.AuthenticationTimeout
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.ui.biometric.BiometricError
import `in`.hridayan.ashell.core.ui.biometric.BiometricPromptManager
import `in`.hridayan.ashell.settings.presentation.components.dialog.AuthenticationTimeoutDialog
import `in`.hridayan.settingsgraph.ui.SettingsColumn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen() {
    val navController = LocalNavController.current
    val settings = LocalSettings.current
    val context = LocalContext.current
    val res = LocalResources.current

    val hapticsEnabled = settings[SettingsKeys.HapticsAndVibration]
    val requireAuth = settings[SettingsKeys.RequireAuthentication]
    val currentTimeout = settings[SettingsKeys.AuthenticationTimeout]

    val timeoutText = getTimeoutText(currentTimeout)

    var showTimeoutDialog by remember { mutableStateOf(false) }
    var showNoLockScreenDialog by remember { mutableStateOf(false) }

    if (showTimeoutDialog) {
        AuthenticationTimeoutDialog(onDismiss = { showTimeoutDialog = false })
    }

    if (showNoLockScreenDialog) {
        AlertDialog(
            onDismissRequest = { showNoLockScreenDialog = false },
            title = { Text(stringResource(R.string.require_authentication)) },
            text = { Text(stringResource(R.string.app_lock_setup_error_msg)) },
            confirmButton = {
                TextButton(onClick = { showNoLockScreenDialog = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }


    val launchAppPermissionsSettingsIntent: () -> Unit = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    val onAuthenticate: () -> Unit = {
        triggerBiometricPrompt(
            context = context,
            title = res.getString(R.string.biometric_prompt_title),
            description = res.getString(R.string.biometric_prompt_description),
            onSuccess = {
                settings.set(
                    key = SettingsKeys.RequireAuthentication,
                    value = !requireAuth
                )
            },
            onError = { error ->
                when (error) {
                    BiometricError.NoneEnrolled -> {
                        if (!requireAuth) {
                            showNoLockScreenDialog = true
                        }
                    }

                    BiometricError.NoHardware,
                    BiometricError.Unsupported -> {
                        Toast.makeText(
                            context,
                            res.getString(R.string.device_not_supported),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {}
                }
            }
        )
    }

    val listState = rememberLazyListState()

    val topAppBarState = rememberTopAppBarState()

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        listState = listState,
        topAppBarState = topAppBarState,
        topBarTitle = stringResource(R.string.privacy_and_security),
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
                group(R.string.authenticate) {
                    switchItem(SettingsKeys.RequireAuthentication) {
                        title(R.string.require_authentication)
                        description(R.string.des_require_authentication)
                        icon(if (requireAuth) R.drawable.ic_fingerprint else R.drawable.ic_fingerprint_off)
                        onClick { onAuthenticate() }
                    }

                    clickableItem(SettingsKeys.AuthenticationTimeout) {
                        title(R.string.authentication_timeout)
                        description(timeoutText)
                        icon(R.drawable.ic_lock_clock)
                        visible { requireAuth }
                        onClick { showTimeoutDialog = true }
                    }

                    switchItem(SettingsKeys.RequireAuthenticationForBackups) {
                        title(R.string.require_auth_for_backups)
                        description(R.string.des_require_auth_for_backups)
                        icon(R.drawable.ic_sync_lock)
                        visible { requireAuth }
                    }
                }

                group(R.string.permissions) {
                    clickableItem(SettingsKeys.AppPermissions) {
                        title(R.string.app_permissions)
                        description(R.string.des_app_permissions)
                        icon(R.drawable.ic_search_gear)
                        onClick { launchAppPermissionsSettingsIntent() }
                    }
                }

                group(R.string.legal_and_policies) {
                    clickableItem(SettingsKeys.PrivacyPolicy) {
                        title(R.string.privacy_policy)
                        description(R.string.des_privacy_policy)
                        icon(R.drawable.ic_policy)
                        onClick { navController.navigate(NavRoutes.PrivacyPolicyScreen) }
                    }

                    clickableItem(SettingsKeys.Licenses) {
                        title(R.string.libraries_and_licenses)
                        description(R.string.des_licenses)
                        icon(R.drawable.ic_license)
                        onClick { navController.navigate(NavRoutes.LicensesScreen) }
                    }
                }
            }
        })
}

private fun triggerBiometricPrompt(
    context: Context,
    title: String,
    description: String,
    onSuccess: () -> Unit,
    onError: (BiometricError) -> Unit
) {
    val activity = context as? AppCompatActivity ?: return

    BiometricPromptManager(activity).showBiometricPrompt(
        title = title,
        description = description,
        onSuccess = onSuccess,
        onError = { onError(it) }
    )
}

@Composable
private fun getTimeoutText(currentTimeout: Int): String {
    val timeoutText = when (currentTimeout) {
        AuthenticationTimeout.NEVER -> stringResource(R.string.timeout_never)
        AuthenticationTimeout.IMMEDIATE -> stringResource(R.string.timeout_immediate)
        AuthenticationTimeout.MIN_1 -> pluralStringResource(R.plurals.timeout_minutes, 1, 1)
        AuthenticationTimeout.MIN_5 -> pluralStringResource(R.plurals.timeout_minutes, 5, 5)
        AuthenticationTimeout.MIN_10 -> pluralStringResource(R.plurals.timeout_minutes, 10, 10)
        else -> stringResource(R.string.timeout_immediate)
    }

    return timeoutText
}