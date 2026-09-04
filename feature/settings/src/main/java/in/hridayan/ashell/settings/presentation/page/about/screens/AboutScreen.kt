@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.page.about.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.emptyPreferences
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.animatedcomposables.AnimatedAdbIcon
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.components.shape.SineWaveShape
import `in`.hridayan.ashell.core.presentation.components.shape.WaveEdge
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.appBranding
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.utils.syncedRotationAndScale
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.openUrl
import `in`.hridayan.ashell.settings.presentation.components.card.SupportMeCard
import `in`.hridayan.ashell.settings.presentation.components.image.ProfilePic
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.ui.SettingsColumn

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val hapticsEnabled = LocalSettings.current[SettingsKeys.HapticsAndVibration]
    val prefs by settingsViewModel.preferences.collectAsState(initial = emptyPreferences())
    val (angle, scale) = syncedRotationAndScale()

    // Removed uiEvent.collect as navigation is direct

    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topAppBarState = topAppBarState,
        topBarTitle = stringResource(R.string.about),
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
                item(key = "header_app_info") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .requiredSize(120.dp)
                                    .graphicsLayer {
                                        rotationZ = angle()
                                        scaleX = scale()
                                        scaleY = scale()
                                    }
                                    .clip(MaterialShapes.Cookie9Sided.toShape())
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            )
                            AnimatedAdbIcon(
                                modifier = Modifier.size(75.dp),
                                headColor = MaterialTheme.colorScheme.tertiary,
                                eyeColor = MaterialTheme.colorScheme.onTertiary
                            )
                        }

                        Image(
                            imageVector = DynamicColorImageVectors.appBranding(),
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            contentScale = ContentScale.Fit,
                            contentDescription = null,
                        )

                        FlowRow(
                            itemVerticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(
                                15.dp,
                                Alignment.CenterHorizontally
                            ),
                            verticalArrangement = Arrangement.spacedBy(15.dp),
                        ) {
                            AppHandlesChip(
                                icon = painterResource(R.drawable.ic_telegram),
                                title = stringResource(R.string.telegram),
                                description = stringResource(R.string.discussions),
                                onClick = { openUrl(UrlConst.URL_TELEGRAM_CHANNEL, context) }
                            )
                            AppHandlesChip(
                                icon = painterResource(R.drawable.ic_github),
                                title = stringResource(R.string.github),
                                description = stringResource(R.string.repository),
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                onClick = { openUrl(UrlConst.URL_GITHUB_REPO, context) }
                            )
                            AppHandlesChip(
                                icon = painterResource(R.drawable.ic_version_tag),
                                title = context.packageManager.getPackageInfo(
                                    context.packageName,
                                    0
                                ).versionName ?: "",
                                description = stringResource(R.string.current_version),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                onClick = { openUrl(UrlConst.URL_GITHUB_RELEASES, context) }
                            )
                            AppHandlesChip(
                                icon = painterResource(R.drawable.ic_license),
                                title = stringResource(R.string.gpl_3_0),
                                description = stringResource(R.string.license),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                onClick = { openUrl(UrlConst.URL_GITHUB_REPO_LICENSE, context) }
                            )
                            AppHandlesChip(
                                icon = painterResource(R.drawable.ic_crowdin),
                                title = stringResource(R.string.crowdin),
                                description = stringResource(R.string.translations),
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                onClick = { openUrl(UrlConst.URL_CROWDIN_PROJECT, context) }
                            )
                        }
                    }
                }

                item(key = "header_developer") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                SineWaveShape(
                                    amplitude = 10f,
                                    frequency = 5f,
                                    edge = WaveEdge.Both
                                )
                            )
                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.lead_developer),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 25.dp)
                                .align(Alignment.Start)
                        )
                        ProfilePic(model = R.mipmap.dp_hridayan, size = 150.dp)
                        Text(
                            text = "Hridayan",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.des_hridayan),
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic
                        )
                        SupportMeCard(
                            modifier = modifier.padding(start = 15.dp, end = 15.dp, bottom = 25.dp)
                        )
                    }
                }

                group(R.string.contributors) {
                    clickableItem(SettingsKeys.Contributors) {
                        title(R.string.contributors)
                        description(R.string.des_contributors)
                        icon(R.drawable.ic_crowdsource)
                        onClick { navController.navigate(NavRoutes.ContributorsScreen) }
                    }
                    clickableItem(SettingsKeys.Translators) {
                        title(R.string.translators)
                        description(R.string.des_translators)
                        icon(R.drawable.ic_translate)
                        onClick { navController.navigate(NavRoutes.TranslatorsScreen) }
                    }
                }

                group(R.string.app) {
                    clickableItem(SettingsKeys.Changelogs) {
                        title(R.string.changelogs)
                        description(R.string.des_changelogs)
                        icon(R.drawable.ic_changelog)
                        onClick { navController.navigate(NavRoutes.ChangelogScreen) }
                    }
                    clickableItem(SettingsKeys.Report) {
                        title(R.string.report_issue)
                        description(R.string.des_report_issue)
                        icon(R.drawable.ic_report)
                        onClick { openUrl(UrlConst.URL_GITHUB_ISSUE_REPORT, context) }
                    }
                    clickableItem(SettingsKeys.FeatureRequest) {
                        title(R.string.feature_request)
                        description(R.string.des_feature_request)
                        icon(R.drawable.ic_add_comment)
                        onClick { openUrl(UrlConst.URL_GITHUB_ISSUE_FEATURE_REQUEST, context) }
                    }
                    clickableItem(SettingsKeys.CrashHistory) {
                        title(R.string.crash_history)
                        description(R.string.des_crash_history)
                        icon(R.drawable.ic_bug)
                        onClick { navController.navigate(NavRoutes.CrashHistoryScreen) }
                    }
                    clickableItem(SettingsKeys.Licenses) {
                        title(R.string.libraries_and_licenses)
                        description(R.string.des_libraries_and_licenses)
                        icon(R.drawable.ic_license)
                        onClick { navController.navigate(NavRoutes.LicensesScreen) }
                    }
                    clickableItem(SettingsKeys.PrivacyPolicy) {
                        title(R.string.privacy_policy)
                        description(R.string.des_privacy_policy)
                        icon(R.drawable.ic_privacy_tip)
                        onClick { navController.navigate(NavRoutes.PrivacyPolicyScreen) }
                    }
                }

                item(key = "spacer_bottom") {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }
        },
    )
}

@Composable
private fun AppHandlesChip(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    description: String,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick: () -> Unit = {},
) {
    CustomCard(
        modifier = modifier,
        shape = CustomCardShape(50),
        colors = CardDefaults.cardColors(containerColor, contentColor),
        onClick = withHaptic(HapticFeedbackType.VirtualKey) { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(painter = icon, contentDescription = null, tint = contentColor)
            Column {
                AutoResizeableText(
                    text = title,
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
                AutoResizeableText(
                    text = description,
                    style = MaterialTheme.typography.bodySmallEmphasized,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}
