package `in`.hridayan.ashell.logcat.presentation.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import `in`.hridayan.ashell.logcat.presentation.components.LogLevelColors
import `in`.hridayan.ashell.logcat.presentation.model.SelectableLogLevels

/**
 * One chip per selectable log level. Selected chips are tinted with the level's colour and carry
 * a check mark, so the selection does not rely on colour alone.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogLevelChips(
    selected: Set<LogLevel>,
    onToggle: (LogLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SelectableLogLevels.forEach { level ->
            LogLevelChip(
                level = level,
                isSelected = level in selected,
                onClick = { onToggle(level) },
            )
        }
    }
}

@Composable
private fun LogLevelChip(
    level: LogLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val isDark = LocalDarkMode.current
    val labelColor = LogLevelColors.textColor(level, isDark)
    FilterChip(
        selected = isSelected,
        onClick = withHaptic { onClick() },
        label = { Text(logLevelName(level)) },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = LogLevelColors.chipContainer(level, isDark),
            selectedLabelColor = labelColor,
            selectedLeadingIconColor = labelColor,
        ),
    )
}
