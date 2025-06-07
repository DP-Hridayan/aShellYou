package `in`.hridayan.ashell.home.presentation.component.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.components.card.ErrorCard
import `in`.hridayan.ashell.core.presentation.components.text.LabelText
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WirelessDebuggingPairingMenu(
    modifier: Modifier = Modifier.Companion,
    sheetState: SheetState,
    onDismissRequest: () -> Unit = {}
) {
    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = null,
        onDismissRequest = onDismissRequest
    ) {
        Text(
            modifier = Modifier.Companion
                .align(Alignment.Companion.CenterHorizontally)
                .padding(Dimens.paddingExtraLarge),
            text = stringResource(R.string.wireless_debugging),
            maxLines = 1,
            style = MaterialTheme.typography.headlineSmall
        )

        ErrorCard(
            modifier = Modifier.Companion.padding(horizontal = Dimens.paddingLarge),
            text = stringResource(R.string.turn_off_mobile_data),
            icon = painterResource(R.drawable.ic_warning)
        )

        LabelText(
            stringResource(R.string.pair),
            modifier = Modifier.Companion
                .padding(Dimens.paddingLarge)
                .align(Alignment.Companion.Start)
        )

        IpAddressInputField()

        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(Dimens.paddingLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
        ) {
            PairingPortInputField(modifier = Modifier.Companion.weight(1f))
            PairingCodeInputField(modifier = Modifier.Companion.weight(1f))
        }

        var context = LocalContext.current
        var text by rememberSaveable { mutableStateOf(context.getString(R.string.pair)) }
        IconWithTextButton(
            icon = painterResource(R.drawable.ic_pair),
            text = text,
            contentDescription = null,
            modifier = Modifier.Companion.align(Alignment.Companion.CenterHorizontally),
            onClick = { })

        LabelText(
            stringResource(R.string.connect), modifier = Modifier.Companion
                .padding(
                    start = Dimens.paddingLarge,
                    end = Dimens.paddingLarge,
                    top = Dimens.paddingLarge
                )
                .align(Alignment.Companion.Start)
        )

        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(Dimens.paddingLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
        ) {
            ConnectPortInputField(modifier = Modifier.Companion.weight(1f))
            IconWithTextButton(
                icon = painterResource(R.drawable.ic_wireless),
                text = stringResource(R.string.connect),
                modifier = Modifier.Companion
                    .weight(1f)
                    .align(Alignment.Companion.CenterVertically)

            )
        }
    }
}

@Composable
fun IpAddressInputField(modifier: Modifier = Modifier.Companion) {
    var ipAddress by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingLarge),
        value = ipAddress,
        onValueChange = { ipAddress = it },
        label = { Text(stringResource(R.string.ip_address)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number)
    )
}

@Composable
fun PairingPortInputField(modifier: Modifier = Modifier.Companion) {
    var pairingPort by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        modifier = modifier,
        value = pairingPort,
        onValueChange = { pairingPort = it },
        label = { Text(stringResource(R.string.port)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number)
    )
}

@Composable
fun PairingCodeInputField(modifier: Modifier = Modifier.Companion) {
    var pairingCode by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        modifier = modifier,
        value = pairingCode,
        onValueChange = { pairingCode = it },
        label = { Text(stringResource(R.string.pairing_code)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number)
    )
}

@Composable
fun ConnectPortInputField(modifier: Modifier = Modifier.Companion) {
    var connectPort by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        modifier = modifier,
        value = connectPort,
        onValueChange = { connectPort = it },
        label = { Text(stringResource(R.string.port)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number)
    )
}