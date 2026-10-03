package `in`.hridayan.ashell.logcat.presentation.components.filter

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.domain.model.FilterMode

@Composable
fun FilterModeSelector(
    mode: FilterMode,
    onModeChange: (FilterMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = FilterMode.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, option ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                selected = option == mode,
                onClick = withHaptic(HapticFeedbackType.ToggleOn) { onModeChange(option) },
                label = { Text(stringResource(option.labelRes())) },
            )
        }
    }
}

@StringRes
private fun FilterMode.labelRes(): Int = when (this) {
    FilterMode.INCLUDE -> R.string.include
    FilterMode.EXCLUDE -> R.string.exclude
}
