@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.common.domain.model.LogcatWorkingMode
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.launch

private data class ModeOption(val value: Int, val labelResId: Int, val descResId: Int? = null)

/**
 * Selects the logcat execution source for the "This Device" tab.
 *
 * - Basic → needs READ_LOGS permission
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
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val settings = LocalSettings.current

    var selected by rememberSaveable { mutableIntStateOf(currentMode) }

    val modeOptions = remember {
        listOf(
            ModeOption(LogcatWorkingMode.BASIC, R.string.basic_shell),
            ModeOption(LogcatWorkingMode.SHIZUKU, R.string.shizuku),
            ModeOption(LogcatWorkingMode.ROOT, R.string.root),
            ModeOption(LogcatWorkingMode.WIRELESS, R.string.wireless_debugging),
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.logcat_source),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            modeOptions.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            selected = option.value
                        }
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(option.labelResId),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    RadioButton(
                        selected = option.value == selected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            selected = option.value
                        },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                Spacer(Modifier.padding(4.dp))
                TextButton(
                    onClick = {
                        scope.launch { settings.set(SettingsKeys.LogcatMode, selected) }
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onDismiss()
                            if (selected != currentMode) onModeChanged(selected)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        }
    }
}
