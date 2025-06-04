@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.components.bottomsheet

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.domain.model.DownloadState
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.utils.installApk
import `in`.hridayan.ashell.core.utils.openUrl
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.viewmodel.AutoUpdateViewModel
import java.io.File

@Composable
fun UpdateBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    latestVersion: String = "",
    apkUrl: String = "",
    viewModel: AutoUpdateViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val activity = context as? Activity
    val isDirectDownloadEnabled = LocalSettings.current.enableDirectDownload
    val interactionSources = remember { List(2) { MutableInteractionSource() } }
    val downloadState by viewModel.downloadState.collectAsState()
    val apkName = "update.apk"
    val apkFile = remember { File(context.getExternalFilesDir(null), apkName) }
    var pendingInstall by rememberSaveable { mutableStateOf(false) }
    var permissionPromptShown by rememberSaveable { mutableStateOf(false) }
    var showDownloadButton by rememberSaveable { mutableStateOf(true) }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val canInstall =
            context.packageManager.canRequestPackageInstalls()

        if (canInstall && pendingInstall) {
            activity?.installApk(apkFile)
            pendingInstall = false
        }
    }

    LaunchedEffect(downloadState) {
        when (val state = downloadState) {
            is DownloadState.Success -> {
                showDownloadButton = true

                val file = state.file

                val canInstall = context.packageManager.canRequestPackageInstalls()

                if (canInstall) {
                    activity?.installApk(file)
                } else if (!permissionPromptShown) {
                    pendingInstall = true
                    permissionPromptShown = true
                    val intent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        "package:${context.packageName}".toUri()
                    )
                    settingsLauncher.launch(intent)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.unknown_sources_install_permission_not_granted),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            is DownloadState.Started -> {
                showDownloadButton = false
            }

            is DownloadState.Progress -> {
                showDownloadButton = false
            }

            is DownloadState.Cancelled -> {
                showDownloadButton = true
            }

            is DownloadState.Error -> {
                showDownloadButton = true
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT)
                    .show()
            }

            else -> Unit
        }
    }

    val currentProgress = when (downloadState) {
        is DownloadState.Progress -> (downloadState as DownloadState.Progress).percent
        else -> 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)
    )

    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = null,
        onDismissRequest = {
            onDismiss()
            permissionPromptShown = false
        }
    ) {
        Text(
            modifier = Modifier.padding(20.dp),
            text = stringResource(R.string.update_available),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            text = stringResource(R.string.latest_version) + " : $latestVersion",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = stringResource(R.string.current_version) + " : ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelLarge,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {

            when (downloadState) {
                is DownloadState.Started -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DownloadState.Progress -> {
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .align(Alignment.Center),
                        progress = { animatedProgress }
                    )
                }

                else -> {
                    AutoResizeableText(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .align(Alignment.CenterStart),
                        maxLines = 3,
                        text = stringResource(R.string.visit_repo_to_download),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

        }


        @Suppress("DEPRECATION")
        ButtonGroup(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            OutlinedButton(
                onClick = {
                    weakHaptic()
                    permissionPromptShown = false

                    if (showDownloadButton) {
                        onDismiss()
                    } else {
                        viewModel.cancelDownload()
                    }
                },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionSources[0]),
                interactionSource = interactionSources[0],
            ) {
                Text(text = stringResource(R.string.cancel))
            }

            if (showDownloadButton)
                Button(
                    onClick = {
                        if (isDirectDownloadEnabled) {
                            permissionPromptShown = false
                            viewModel.downloadApk(apkUrl, apkName)
                        } else {
                            openUrl(
                                context = context,
                                url = "https://github.com/dp-hridayan/driftly/releases/tag/$latestVersion"
                            )
                        }
                        weakHaptic()
                    },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(interactionSources[1]),
                    interactionSource = interactionSources[1],
                ) {
                    Text(text = stringResource(R.string.download))
                }
        }
    }
}