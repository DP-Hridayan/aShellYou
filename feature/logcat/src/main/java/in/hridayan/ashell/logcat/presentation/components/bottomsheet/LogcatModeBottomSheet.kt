@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.model.LogcatWorkingMode
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.radio.RadioOptionCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.launch

private data class ModeOption(val value: Int, val labelResId: Int, val descResId: Int? = null)

private val modeOptions = listOf(
    ModeOption(
        value = LogcatWorkingMode.READ_LOGS,
        labelResId = R.string.logcat_permission_title,
        descResId = R.string.logcat_read_logs_mode_description,
    ),
    ModeOption(LogcatWorkingMode.SHIZUKU, R.string.shizuku),
    ModeOption(LogcatWorkingMode.ROOT, R.string.root),
    ModeOption(LogcatWorkingMode.WIRELESS, R.string.wireless_debugging),
)

/**
 * Selects the logcat execution source for the "This Device" tab, styled like the shell's
 * Connected Device sheet.
 *
 * - Log access → READ_LOGS granted once via ADB; app restart needed after grant
 * - Shizuku / Root → full system log, no permission needed
 * - Wireless Debugging → own phone connected via WiFi ADB, no permission needed
 *
 * WIRELESS mode does NOT navigate anywhere — it just sets the preference.
 * The user must have already paired their device via wireless debugging.
 * If they haven't, the empty state on the logcat screen prompts them.
 */
@Composable
fun LogcatModeBottomSheet(
    currentMode: Int,
    onModeChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scope = rememberCoroutineScope()
    val settings = LocalSettings.current
    var selected by rememberSaveable { mutableIntStateOf(currentMode) }

    LaunchedEffect(currentMode) {
        selected = currentMode
    }

    val hideThenDismiss: (onHidden: () -> Unit) -> Unit = { onHidden ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            onHidden()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            AutoResizeableText(
                text = stringResource(R.string.logcat_source),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            ModeOptions(selected = selected, onSelect = { selected = it })

            OverflowButtonGroup(
                modifier = Modifier.padding(top = 9.dp),
                items = listOf(
                    ButtonGroupItem(
                        buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                        text = stringResource(R.string.cancel),
                        onClick = { hideThenDismiss {} },
                    ),
                    ButtonGroupItem(
                        text = stringResource(R.string.apply),
                        onClick = {
                            val chosen = selected
                            settings.set(SettingsKeys.LogcatMode, chosen)
                            hideThenDismiss { if (chosen != currentMode) onModeChanged(chosen) }
                        },
                    ),
                ),
            )
        }
    }
}

@Composable
private fun ModeOptions(selected: Int, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        modeOptions.forEachIndexed { index, option ->
            RadioOptionCard(
                label = stringResource(option.labelResId),
                description = option.descResId?.let { stringResource(it) },
                selected = option.value == selected,
                index = index,
                count = modeOptions.size,
                onSelect = { onSelect(option.value) },
            )
        }
    }
}
