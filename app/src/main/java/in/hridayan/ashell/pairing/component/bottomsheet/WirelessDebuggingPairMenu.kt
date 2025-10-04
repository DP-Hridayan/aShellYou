@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.pairing.component.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.button.IconWithTextButton
import `in`.hridayan.ashell.core.presentation.components.card.ErrorCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.LabelText
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens
import `in`.hridayan.ashell.pairing.component.image.QRImage
import `in`.hridayan.ashell.pairing.helper.PairUsingQR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WirelessDebuggingPairingMenu(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showManualPairingMenu by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = null,
        onDismissRequest = onDismissRequest
    ) {
        AutoResizeableText(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(Dimens.paddingExtraLarge),
            text = stringResource(R.string.wireless_debugging),
            style = MaterialTheme.typography.headlineSmallEmphasized,
            fontWeight = FontWeight.Bold
        )

        ErrorCard(
            modifier = Modifier.padding(horizontal = Dimens.paddingLarge),
            text = stringResource(R.string.turn_off_mobile_data),
            icon = painterResource(R.drawable.ic_warning)
        )

        if (showManualPairingMenu) {
            PairManually(onClickPairUsingQR = { showManualPairingMenu = false })
        } else {
            QRPair(onClickPairManually = { showManualPairingMenu = true })
        }
    }
}

@Composable
fun QRPair(
    modifier: Modifier = Modifier,
    onClickPairManually: () -> Unit = {}
) {
    val weakHaptic = LocalWeakHaptic.current
    val qrHelper = PairUsingQR()
    val sessionId = "ashell_you"
    val pairingCode = 123456
    val qrBitmap = qrHelper.generateQrBitmap(sessionId, pairingCode)

    Column(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 5.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_qr_scanner),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )

                Text(
                    text = stringResource(R.string.qr_pair_hint),
                    style = MaterialTheme.typography.bodySmallEmphasized,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )

                Text(
                    text = stringResource(R.string.qr_scanner_location_hint),
                    style = MaterialTheme.typography.bodySmallEmphasized,
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            QRImage(
                qrBitmap = qrBitmap,
                modifier = Modifier.padding(25.dp)
            )
        }

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(R.string.or),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        TextButton(
            modifier = Modifier
                .padding(25.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shapes = ButtonDefaults.shapes(),
            onClick = {
                weakHaptic()
                onClickPairManually()
            },
        ) {
            Text(
                text = stringResource(R.string.pair_manually),
                modifier = Modifier.padding(horizontal = 5.dp)
            )
        }
    }
}

@Composable
fun PairManually(
    modifier: Modifier = Modifier,
    onClickPairUsingQR: () -> Unit = {}
) {
    val context = LocalContext.current
    val weakHaptic = LocalWeakHaptic.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp, start = 25.dp, end = 25.dp)
    ) {
        LabelText(
            stringResource(R.string.pair),
            modifier = Modifier
                .padding(top = 25.dp, bottom = 10.dp)
                .align(Alignment.Start)
        )

        IpAddressInputField()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            PairingPortInputField(modifier = Modifier.weight(1f))
            PairingCodeInputField(modifier = Modifier.weight(1f))
        }

        var pairButtonText by rememberSaveable { mutableStateOf(context.getString(R.string.pair)) }

        IconWithTextButton(
            icon = painterResource(R.drawable.ic_pair),
            text = pairButtonText,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp),
            onClick = { })

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        )

        LabelText(
            stringResource(R.string.connect), modifier = Modifier
                .padding(bottom = 10.dp)
                .align(Alignment.Start)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
        ) {
            ConnectPortInputField(modifier = Modifier.weight(1f))
            IconWithTextButton(
                icon = painterResource(R.drawable.ic_wireless),
                text = stringResource(R.string.connect),
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )
        }

        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 25.dp),
            text = stringResource(R.string.or),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        TextButton(
            modifier = Modifier
                .padding(25.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shapes = ButtonDefaults.shapes(),
            onClick = {
                weakHaptic()
                onClickPairUsingQR()
            },
        ) {
            Text(
                text = stringResource(R.string.pair_using_qr),
                modifier = Modifier.padding(horizontal = 5.dp)
            )
        }
    }
}

@Composable
fun IpAddressInputField(modifier: Modifier = Modifier) {
    var ipAddress by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = ipAddress,
        onValueChange = { ipAddress = it },
        label = { Text(stringResource(R.string.ip_address)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number)
    )
}

@Composable
fun PairingPortInputField(modifier: Modifier = Modifier) {
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
fun PairingCodeInputField(modifier: Modifier = Modifier) {
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
fun ConnectPortInputField(modifier: Modifier = Modifier) {
    var connectPort by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        modifier = modifier,
        value = connectPort,
        onValueChange = { connectPort = it },
        label = { Text(stringResource(R.string.port)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number)
    )
}