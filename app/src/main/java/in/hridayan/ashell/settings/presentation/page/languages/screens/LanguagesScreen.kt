package `in`.hridayan.ashell.settings.presentation.page.languages.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.CrowdinContributeCard
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.utils.MiUiCheck
import `in`.hridayan.ashell.settings.domain.model.SupportedLocale
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.page.languages.viewmodel.LanguagesViewModel

@Composable
fun LanguagesScreen(
    modifier: Modifier = Modifier,
    viewModel: LanguagesViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val currentTag by viewModel.currentLocaleTag.collectAsStateWithLifecycle()
    val locales = viewModel.supportedLocales
    val listState = rememberLazyListState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshCurrentTag()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val systemDefault = SupportedLocale(
        tag = "",
        displayName = stringResource(R.string.system_default),
        nativeName = "",
    )

    val allLocales = listOf(systemDefault) + locales

    val showSystemSettingsButton =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !MiUiCheck.isMiui

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.languages),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding,
            ) {
                // "Open app language settings" button — A13+ and not MIUI
                if (showSystemSettingsButton) {
                    item(key = "system_settings_button") {
                        CustomCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp, vertical = 15.dp),
                            shape = CustomCardShape(50),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            onClick = withHaptic {
                                val intent =
                                    Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                                        data = "package:${context.packageName}".toUri()
                                    }
                                context.startActivity(intent)
                            },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_language),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = stringResource(R.string.open_app_language_settings),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }

                // Locale list
                itemsIndexed(
                    allLocales,
                    key = { _, locale -> "locale_${locale.tag}" }) { index, locale ->
                    val isSelected = locale.tag == currentTag

                    val shape = if (isSelected) CustomCardShape(50)
                    else getRoundedShape(index, allLocales.size)

                    LocaleCard(
                        locale = locale,
                        isSelected = isSelected,
                        shape = shape,
                        onClick = { viewModel.setLocale(locale.tag) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 1.dp)
                            .animateItem(),
                    )
                }

                item(key = "crowdin_card") {
                    CrowdinContributeCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 20.dp),
                    )
                }

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
private fun LocaleCard(
    locale: SupportedLocale,
    isSelected: Boolean,
    shape: CustomCardShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = MaterialTheme.colorScheme.run {
        if (isSelected) primaryContainer else surfaceContainer
    }

    val contentColor = MaterialTheme.colorScheme.run {
        if (isSelected) onPrimaryContainer else onSurface
    }

    CustomCard(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        onClick = withHaptic { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = locale.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )

                if (locale.nativeName.isNotBlank() && locale.nativeName != locale.displayName) {
                    Text(
                        text = locale.nativeName,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.alpha(0.7f),
                    )
                }
            }

            // Translation progress
            locale.translationProgress?.let { progress ->
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress / 100f,
                        label = "translation_progress"
                    )

                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        color = MaterialTheme.colorScheme.run {
                            if (isSelected) onPrimaryContainer else primary
                        },
                        trackColor = MaterialTheme.colorScheme.run {
                            if (isSelected) onPrimaryContainer.copy(alpha = 0.2f)
                            else primary.copy(alpha = 0.15f)
                        },
                        strokeWidth = 3.dp
                    )

                    AutoResizeableText(
                        text = "$progress%",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .alpha(0.7f)
                            .padding(2.dp),
                    )
                }
            }
        }
    }
}