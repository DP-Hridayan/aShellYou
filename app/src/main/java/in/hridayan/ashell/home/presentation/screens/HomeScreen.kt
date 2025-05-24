package `in`.hridayan.ashell.home.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.config.URL_OTG_INSTRUCTIONS
import `in`.hridayan.ashell.core.common.config.URL_WIRELESS_DEBUGGING_INSTRUCTIONS
import `in`.hridayan.ashell.core.common.utils.UrlUtils
import `in`.hridayan.ashell.core.presentation.ui.component.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.ui.component.card.BottomCornerRoundedCard
import `in`.hridayan.ashell.core.presentation.ui.component.card.NavigationCard
import `in`.hridayan.ashell.core.presentation.ui.component.card.TopCornerRoundedCard
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens
import `in`.hridayan.ashell.home.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(it)
                .padding(Dimens.paddingExtraLarge),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 45.dp, bottom = 25.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                AppNameText(modifier = Modifier.weight(1f))
                SettingsButton()
            }

            LocalAdbCard()
            WirelessDebuggingCard()
            OtgAdbCard()
        }
    }
}

@Composable
fun SettingsButton(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_settings),
        contentDescription = null,
        colorFilter = ColorFilter.Companion.tint(
            MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                })
    )
}

@Composable
fun AppNameText(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.app_name),
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier
            .alpha(0.9f)
    )
}

@Composable
fun LocalAdbCard(modifier: Modifier = Modifier) {
    NavigationCard(
        title = stringResource(R.string.local_adb),
        description = stringResource(R.string.local_adb_summary),
        icon = painterResource(R.drawable.ic_adb2),
        modifier = modifier.padding(bottom = 13.dp),
        onClick = { },
        content = {
            ShizukuAccessCard(modifier)
            RootAccessCard(modifier)
        })
}

@Composable
fun WirelessDebuggingCard(modifier: Modifier = Modifier) {
    NavigationCard(
        title = stringResource(R.string.adb_via_wireless_debugging),
        description = stringResource(R.string.adb_via_wireless_debugging_summary),
        icon = painterResource(R.drawable.ic_wireless),
        modifier = modifier.padding(bottom = 13.dp),
        onClick = { },
        content = {
            WirelessDebuggingInstructionButton(
                Modifier.Companion.padding(
                    top = 10.dp,
                    bottom = 5.dp
                )
            )
            WirelessDebuggingStartButton()
        })
}

@Composable
fun OtgAdbCard(modifier: Modifier = Modifier) {
    NavigationCard(
        title = stringResource(R.string.adb_through_otg),
        description = stringResource(R.string.adb_through_otg_summary),
        icon = painterResource(R.drawable.ic_otg),
        modifier = modifier,
        onClick = { },
        content = {
            OtgInstructionButton(Modifier.Companion.padding(top = 10.dp))
        })
}

@Composable
fun ShizukuAccessCard(modifier: Modifier) {
    TopCornerRoundedCard(
        modifier = modifier.padding(bottom = 5.dp),
        onClick = {},
        content = {
            Row(
                modifier = Modifier.Companion.padding(horizontal = 15.dp, vertical = 10.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_error),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.Companion
                        .align(Alignment.Companion.CenterVertically)
                        .size(20.dp)
                )
                Column(modifier = Modifier.Companion.padding(start = 15.dp)) {
                    Text(
                        text = stringResource(R.string.shizuku_access),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.Companion.alpha(0.95f)
                    )

                    Text(
                        text = stringResource(R.string.version),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.Companion.alpha(0.95f)
                    )
                }
            }
        })
}

@Composable
fun RootAccessCard(modifier: Modifier) {
    BottomCornerRoundedCard(modifier = modifier, onClick = {}, content = {
        Row(
            modifier = Modifier.Companion
                .padding(horizontal = 15.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_error),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.Companion
                    .align(Alignment.Companion.CenterVertically)
                    .size(20.dp)
            )
            Column(modifier = Modifier.Companion.padding(start = 15.dp)) {
                Text(
                    text = stringResource(R.string.root_access),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.Companion.alpha(0.95f),
                )

                Text(
                    text = stringResource(R.string.root_provider),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.Companion.alpha(0.95f),
                )

                Text(
                    text = stringResource(R.string.version),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.Companion.alpha(0.95f),
                )
            }
        }
    })
}

@Composable
fun WirelessDebuggingInstructionButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    IconWithTextButton(
        icon = painterResource(R.drawable.ic_open_in_new),
        text = stringResource(R.string.instructions),
        contentDescription = null,
        modifier = modifier,
        onClick = {
            UrlUtils.Companion.openUrl(
                url = URL_WIRELESS_DEBUGGING_INSTRUCTIONS,
                context
            )
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WirelessDebuggingStartButton(modifier: Modifier = Modifier) {
    IconWithTextButton(
        icon = painterResource(R.drawable.ic_play),
        text = stringResource(R.string.start),
        contentDescription = null,
        modifier = modifier,
        onClick = {
        })
}

@Composable
fun OtgInstructionButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    IconWithTextButton(
        icon = painterResource(R.drawable.ic_open_in_new),
        text = stringResource(R.string.instructions),
        contentDescription = null,
        modifier = modifier,
        onClick = {
            UrlUtils.Companion.openUrl(
                url = URL_OTG_INSTRUCTIONS,
                context
            )
        })
}