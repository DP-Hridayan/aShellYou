package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.presentation.components.card.RoundedCornerCard
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.shape.CardCornerShape
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel.WifiAdbViewModel

@Composable
fun PairedDevicesDialog(
    onDismiss: () -> Unit = {},
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val savedDevices by viewModel.savedDevices.collectAsState()

    LaunchedEffect(Unit, savedDevices) {
        viewModel.loadSavedDevices()
    }

    DialogContainer(onDismiss = onDismiss) {
        savedDevices.forEachIndexed { i, device ->
            val shape = CardCornerShape.getRoundedShape(i, savedDevices.size)

            RoundedCornerCard(
                roundedCornerShape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${device.ip}:${device.port}",
                    modifier = Modifier.padding(
                        horizontal = 15.dp,
                        vertical = 10.dp
                    )
                )
            }
        }
    }
}