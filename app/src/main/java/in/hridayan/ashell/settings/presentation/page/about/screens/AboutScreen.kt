@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.page.about.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.shape.SineWaveShape
import `in`.hridayan.ashell.core.presentation.components.shape.WaveEdge
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.utils.syncedRotationAndScale
import `in`.hridayan.ashell.core.utils.openUrl
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.components.card.SupportMeCard
import `in`.hridayan.ashell.settings.presentation.components.image.ProfilePic
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.ashell.settings.presentation.state.rememberController
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.resolver.resolveAll
import `in`.hridayan.settingsdsl.ui.highlight.rememberHighlightState
import `in`.hridayan.settingsdsl.ui.item.settingsContent

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    highlightKey: String? = null,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val hapticsEnabled = LocalSettings.current.isHapticEnabled
    val controller = settingsViewModel.rememberController()
    val (angle, scale) = syncedRotationAndScale()

    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.Navigate -> navController.navigate(event.route)
                is SettingsUiEvent.OpenUrl -> openUrl(event.url, context)
                else -> {}
            }
        }
    }

    val listState = rememberLazyListState()
    val highlightedKey = rememberHighlightState(
        highlightKeyName = highlightKey,
        page = settingsViewModel.aboutPage,
        listState = listState,
        headerItemCount = 2,
        keyResolver = { SettingsKeys.valueOfOrNull(it) },
    )

    val page = remember { settingsViewModel.aboutPage }
    val resolvedGroups = page.resolveAll(highlightedKey = highlightedKey)

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.about),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding,
            ) {
                // App info header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
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
                                    .graphicsLayer { rotationZ = angle }
                                    .scale(scale)
                                    .clip(MaterialShapes.Cookie9Sided.toShape())
                                    .clickable(onClick = withHaptic {})
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_adb2),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        AutoResizeableText(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.displayLargeEmphasized.copy(
                                letterSpacing = 0.025.em
                            )
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
                                onClick = { openUrl(UrlConst.URL_TELEGRAM_CHANNEL, context) })
                            AppHandlesChip(
                                icon = painterResource(R.drawable.ic_github),
                                title = stringResource(R.string.github),
                                description = stringResource(R.string.repository),
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                onClick = { openUrl(UrlConst.URL_GITHUB_REPO, context) })
                            AppHandlesChip(
                                icon = painterResource(R.drawable.ic_version_tag),
                                title = BuildConfig.VERSION_NAME,
                                description = stringResource(R.string.current_version),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                onClick = { openUrl(UrlConst.URL_GITHUB_RELEASES, context) })
                            AppHandlesChip(
                                icon = painterResource(R.drawable.ic_license),
                                title = stringResource(R.string.gpl_3_0),
                                description = stringResource(R.string.license),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                onClick = { openUrl(UrlConst.URL_GITHUB_REPO_LICENSE, context) })
                            AppHandlesChip(
                                icon = painterResource(R.drawable.ic_crowdin),
                                title = stringResource(R.string.crowdin),
                                description = stringResource(R.string.translations),
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                onClick = { openUrl(UrlConst.URL_CROWDIN_PROJECT, context) })
                        }
                    }
                }

                // Lead developer section
                item {
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
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .animateItem(),
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
                                .animateItem()
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
                            modifier = modifier.padding(
                                start = 15.dp,
                                end = 15.dp,
                                bottom = 25.dp
                            )
                        )
                    }
                }

                settingsContent(
                    groups = resolvedGroups,
                    controller = controller,
                    hapticsEnabled = hapticsEnabled
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
