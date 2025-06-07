@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.common.config.URL_OTG_INSTRUCTIONS
import `in`.hridayan.ashell.core.common.config.URL_WIRELESS_DEBUGGING_INSTRUCTIONS
import `in`.hridayan.ashell.core.presentation.components.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.components.button.OutlinedIconButtonWithText
import `in`.hridayan.ashell.core.presentation.components.card.NavigationCard
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens
import `in`.hridayan.ashell.core.utils.UrlUtils
import `in`.hridayan.ashell.home.presentation.viewmodel.HomeViewModel
import `in`.hridayan.ashell.navigation.LocalAdbScreen
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.SettingsScreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current

    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(it)
                .padding(Dimens.paddingExtraLarge),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                AppNameText(modifier = Modifier.weight(1f))
                SettingsButton(onClick = {
                    weakHaptic()
                    navController.navigate(SettingsScreen)
                })
            }

            LocalAdbCard(
                modifier = Modifier.padding(top = 10.dp, bottom = 15.dp),
                onClick = {
                    navController.navigate(LocalAdbScreen)
                }
            )
            WirelessDebuggingCard()
            OtgAdbCard()
        }
    }
}

@Composable
fun SettingsButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Image(
        painter = painterResource(id = R.drawable.ic_settings),
        contentDescription = null,
        colorFilter = ColorFilter.tint(
            MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
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
fun LocalAdbCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    NavigationCard(
        title = stringResource(R.string.local_adb),
        description = stringResource(R.string.local_adb_summary),
        icon = painterResource(R.drawable.ic_adb2),
        modifier = modifier,
        onClick = onClick,
    )
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
                Modifier.padding(
                    top = 35.dp,
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
            OtgInstructionButton(Modifier.padding(top = 35.dp))
        })
}


@Composable
fun WirelessDebuggingInstructionButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    OutlinedIconButtonWithText(
        modifier = modifier,
        text = stringResource(R.string.instructions),
        painter = painterResource(R.drawable.ic_open_in_new),
        onClick = {
            UrlUtils.openUrl(
                url = URL_WIRELESS_DEBUGGING_INSTRUCTIONS,
                context = context
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

    OutlinedIconButtonWithText(
        modifier = modifier,
        text = stringResource(R.string.instructions),
        painter = painterResource(R.drawable.ic_open_in_new),
        onClick = {
            UrlUtils.openUrl(
                url = URL_OTG_INSTRUCTIONS,
                context = context
            )
        })
}