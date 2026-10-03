@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.common.presentation.components.bottomsheet

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.radio.RadioOptionCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.presentation.provider.RadioGroupOptionsProvider
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.launch

private const val DEVICE_CARD_CONTAINER_ALPHA = 0.75f

/**
 * Shows the device the shell is attached to and, when [showLocalAdbModes] is set, the Local ADB
 * working modes. A sheet rather than a dialog so the mode list scrolls instead of being cropped on
 * short screens.
 */
@Composable
fun ConnectedDeviceBottomSheet(
    connectedDevice: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showLocalAdbModes: Boolean = true
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val scope = rememberCoroutineScope()
    val hideThenDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            AutoResizeableText(
                text = stringResource(R.string.connected_device),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            ConnectedDeviceCard(connectedDevice = connectedDevice)

            if (showLocalAdbModes) {
                LocalAdbModeSection(onFinished = hideThenDismiss)
            }
        }
    }
}

@Composable
private fun ConnectedDeviceCard(connectedDevice: String?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.largeIncreased)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.largeIncreased
            ),
        shape = MaterialTheme.shapes.largeIncreased,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = DEVICE_CARD_CONTAINER_ALPHA
            ),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(
            text = connectedDevice ?: stringResource(R.string.none),
            style = MaterialTheme.typography.titleSmallEmphasized,
            maxLines = 1,
            modifier = Modifier
                .padding(15.dp)
                .basicMarquee()
        )
    }
}

@Composable
private fun LocalAdbModeSection(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val settings = LocalSettings.current
    val savedMode = settings[SettingsKeys.LocalAdbWorkingMode]
    var selectedMode by rememberSaveable { mutableIntStateOf(savedMode) }

    LaunchedEffect(savedMode) {
        selectedMode = savedMode
    }

    val options = RadioGroupOptionsProvider.localAdbShellModeOptions

    Column(modifier = modifier.fillMaxWidth()) {
        AutoResizeableText(
            modifier = Modifier.padding(start = 5.dp, top = 10.dp, bottom = 10.dp),
            text = stringResource(R.string.local_adb_mode),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        options.forEachIndexed { index, option ->
            option.labelResId?.let { labelResId ->
                RadioOptionCard(
                    label = stringResource(labelResId),
                    selected = option.value == selectedMode,
                    index = index,
                    count = options.size,
                    onSelect = { selectedMode = option.value }
                )
            }
        }

        OverflowButtonGroup(
            modifier = Modifier.padding(top = 24.dp),
            items = listOf(
                ButtonGroupItem(
                    buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                    text = stringResource(R.string.cancel),
                    onClick = onFinished
                ),
                ButtonGroupItem(
                    text = stringResource(R.string.apply),
                    onClick = {
                        settings.set(key = SettingsKeys.LocalAdbWorkingMode, value = selectedMode)
                        onFinished()
                    }
                )
            )
        )
    }
}
