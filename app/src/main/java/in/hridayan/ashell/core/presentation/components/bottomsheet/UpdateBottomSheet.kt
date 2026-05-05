@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.core.presentation.components.bottomsheet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.domain.model.DownloadState
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.utils.installApk
import `in`.hridayan.ashell.core.utils.openUrl
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.settings.presentation.page.autoupdate.viewmodel.AutoUpdateViewModel
import java.io.File

@Composable
fun UpdateBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    latestVersion: String = "",
    apkUrl: String = "",
    body: String = "",
    viewModel: AutoUpdateViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            // Block swipe-to-dismiss
            newValue != SheetValue.Hidden
        }
    )
    val context = LocalContext.current
    val res = LocalResources.current
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
                    showToast(
                        context,
                        res.getString(R.string.unknown_sources_install_permission_not_granted),
                    )
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
                showToast(context, state.message)
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

    val latestChangelogText = remember(body) { parseChangelog(body, context) }
    val versionRange = BuildConfig.VERSION_NAME.removeSuffix("-debug") + "..." + latestVersion
    val fullChangelogUrl = UrlConst.URL_GITHUB_REPO + "/compare/" + versionRange

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

        InfoChip(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = stringResource(R.string.latest_version) + " : $latestVersion"
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
        )

        InfoChip(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = stringResource(R.string.current_version) + " : ${BuildConfig.VERSION_NAME}",
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        if (latestChangelogText.isNotEmpty()) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            )

            CustomCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = CardCornerShape.FIRST_CARD,
                pressedScale = 1f
            ) {
                AutoResizeableText(
                    modifier = Modifier.padding(10.dp),
                    text = stringResource(R.string.changelog),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )

            CustomCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = CardCornerShape.LAST_CARD,
                pressedScale = 1f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .heightIn(max = 250.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = latestChangelogText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        InfoChip(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp),
            text = versionRange,
            fontWeight = FontWeight.SemiBold,
            textDecoration = TextDecoration.Underline,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            onClick = withHaptic {
                openUrl(url = fullChangelogUrl, context = context)
            },
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
                onClick = withHaptic(HapticFeedbackType.Reject) {
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
                    onClick = withHaptic(HapticFeedbackType.Confirm) {
                        if (isDirectDownloadEnabled) {
                            permissionPromptShown = false
                            viewModel.downloadApk(apkUrl, apkName)
                        } else {
                            openUrl(
                                context = context,
                                url = "https://github.com/dp-hridayan/ashellyou/releases/tag/$latestVersion"
                            )
                        }
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

private fun parseChangelog(body: String?, context: Context): String {
    if (body == null) return context.resources.getString(R.string.no_changelog_found)

    val changelogRegex =
        Regex("""(?s)### Changelog\s*\n+(.*?)(?=\n+## |\n+\*\*Full Changelog\*\*|$)""")

    val changelogMatch = changelogRegex.find(body)
    val changelogText = changelogMatch?.groups?.get(1)?.value?.trim() ?: ""


    return changelogText
}

@Composable
private fun InfoChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.labelLargeEmphasized,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    textDecoration: TextDecoration? = null,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
) {
    CustomCard(
        modifier = modifier,
        shape = CustomCardShape(50),
        colors = colors,
        pressedCornerRadius = 8.dp,
        onClick = onClick
    ) {
        AutoResizeableText(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            text = text,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            textDecoration = textDecoration,
            style = textStyle,
        )
    }
}