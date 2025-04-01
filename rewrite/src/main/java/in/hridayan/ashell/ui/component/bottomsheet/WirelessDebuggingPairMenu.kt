package `in`.hridayan.ashell.ui.component.bottomsheet

import android.inputmethodservice.Keyboard
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.ui.component.card.ErrorCard
import `in`.hridayan.ashell.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WirelessDebuggingPairingMenu(
    modifier: Modifier = Modifier, sheetState: SheetState, onDismissRequest: () -> Unit = {}
) {
    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = null,
        onDismissRequest = onDismissRequest
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(Dimens.paddingExtraLarge),
            text = stringResource(R.string.wireless_debugging),
            maxLines = 1,
            style = MaterialTheme.typography.headlineSmall
        )
        ErrorCard(
            modifier = Modifier.padding(horizontal = Dimens.paddingLarge),
            text = stringResource(R.string.turn_off_mobile_data),
            icon = painterResource(R.drawable.ic_warning)
        )
        Text(
            modifier = Modifier
                .align(Alignment.Start)
                .padding(Dimens.paddingLarge),
            text = stringResource(R.string.pair),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        IpAddressInputField()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
        )
        {
            PairingPortInputField()
            PairingCodeInputField()
        }

    }
}

@Composable
fun IpAddressInputField(modifier: Modifier = Modifier) {
    var ipAddress by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingLarge),
        value = ipAddress,
        onValueChange = { ipAddress = it },
        label = { Text(stringResource(R.string.ip_address)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun PairingPortInputField(modifier: Modifier = Modifier) {
    var pairingPort by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        modifier = Modifier,
        value = pairingPort,
        onValueChange = { pairingPort = it },
        label = { Text(stringResource(R.string.ip_address)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

    @Composable
    fun PairingCodeInputField(modifier: Modifier = Modifier) {
        var pairingCode by rememberSaveable { mutableStateOf("") }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = pairingCode,
            onValueChange = { pairingCode= it },
            label = { Text(stringResource(R.string.ip_address)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
}

