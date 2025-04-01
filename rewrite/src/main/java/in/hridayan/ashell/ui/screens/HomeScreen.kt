package `in`.hridayan.ashell.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.common.config.URL_OTG_INSTRUCTIONS
import `in`.hridayan.ashell.common.config.URL_WIRELESS_DEBUGGING_INSTRUCTIONS
import `in`.hridayan.ashell.common.utils.UrlUtils
import `in`.hridayan.ashell.ui.component.bottomsheet.WirelessDebuggingPairingMenu
import `in`.hridayan.ashell.ui.component.button.IconWithTextButton
import `in`.hridayan.ashell.ui.component.card.BottomCornerRoundedCard
import `in`.hridayan.ashell.ui.component.card.NavigationCard
import `in`.hridayan.ashell.ui.component.card.TopCornerRoundedCard
import `in`.hridayan.ashell.ui.theme.AShellYouTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(25.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SettingsButton()
        AppNameText()
        LocalAdbCard()
        WirelessDebuggingCard()
        OtgAdbCard()
    }
}

@Composable
fun SettingsButton(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_settings),
        contentDescription = null,
        colorFilter = ColorFilter.tint(
            MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .padding(vertical = 45.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { })
    )
}

@Composable
fun AppNameText(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.app_name),
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier
            .padding(bottom = 20.dp)
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
            WirelessDebuggingInstructionButton(Modifier.padding(top = 10.dp, bottom = 5.dp))
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
            OtgInstructionButton(Modifier.padding(top = 10.dp))
        })
}

@Composable
fun ShizukuAccessCard(modifier: Modifier) {
    TopCornerRoundedCard(modifier = Modifier.padding(bottom = 5.dp), onClick = {}, content = {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_error),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(20.dp)
            )
            Column(modifier = Modifier.padding(start = 15.dp)) {
                Text(
                    text = stringResource(R.string.shizuku_access),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.95f)
                )

                Text(
                    text = stringResource(R.string.version),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.95f)
                )
            }
        }
    })
}

@Composable
fun RootAccessCard(modifier: Modifier) {
    BottomCornerRoundedCard(modifier = Modifier, onClick = {}, content = {
        Row(
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_error),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(20.dp)
            )
            Column(modifier = Modifier.padding(start = 15.dp)) {
                Text(
                    text = stringResource(R.string.root_access),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.95f),
                )

                Text(
                    text = stringResource(R.string.root_provider),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.95f),
                )

                Text(
                    text = stringResource(R.string.version),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.95f),
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
            UrlUtils.openUrl(
                url = URL_WIRELESS_DEBUGGING_INSTRUCTIONS,
                context
            )
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WirelessDebuggingStartButton(modifier: Modifier = Modifier) {
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }

    IconWithTextButton(
        icon = painterResource(R.drawable.ic_play),
        text = stringResource(R.string.start),
        contentDescription = null,
        modifier = modifier,
        onClick = {
            isSheetOpen = true
        })

    if (isSheetOpen) {
        WirelessDebuggingPairingMenu(
            modifier = Modifier,
            sheetState = sheetState,
            onDismissRequest = {
                isSheetOpen = false
                Log.w("Sheet", "Sheet is dismissed")
            })
    }
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
            UrlUtils.openUrl(
                url = URL_OTG_INSTRUCTIONS,
                context
            )
        })
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    AShellYouTheme {
        HomeScreen()
    }
}