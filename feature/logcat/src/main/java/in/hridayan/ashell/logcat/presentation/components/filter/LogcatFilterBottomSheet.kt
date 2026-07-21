@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.components.filter

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.logcat.domain.model.DefaultIncludeLevels
import `in`.hridayan.ashell.logcat.domain.model.FilterMode
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import `in`.hridayan.ashell.logcat.presentation.components.LogLevelColors

/**
 * Rich filter bottom sheet.
 *
 * Sections:
 * 1. Saved profiles (horizontal chip row)
 * 2. Include / Exclude mode toggle
 * 3. Log-level multi-select chips (replaces the old min-level dropdown)
 * 4. Field text fields: Tag, Package, PID, TID, UID
 * 5. Action buttons: Clear all | Save profile | Apply
 *
 * Level chip semantics per mode:
 * - INCLUDE: only selected levels are shown. Default = all except Verbose.
 * - EXCLUDE: selected levels are hidden. Default = none selected (nothing hidden).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogcatFilterBottomSheet(
    activeFilter: LogFilter,
    savedFilters: List<LogFilter>,
    onApply: (LogFilter) -> Unit,
    onSaveProfile: (name: String) -> Unit,
    onDeleteProfile: (id: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    var draft by remember { mutableStateOf(activeFilter) }
    var showSaveDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.filter),
                style = MaterialTheme.typography.titleMedium,
            )

            if (savedFilters.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    savedFilters.forEach { profile ->
                        InputChip(
                            selected = false,
                            onClick = { draft = profile },
                            label = { Text(profile.name.ifBlank { "Filter" }) },
                            trailingIcon = {
                                TextButton(onClick = { onDeleteProfile(profile.id) }) {
                                    Text("✕", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                    }
                }
            }

            val modes = FilterMode.entries
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                modes.forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = modes.size
                        ),
                        selected = draft.mode == mode,
                        onClick = {
                            // When switching modes, reset levels to the sensible default for that mode
                            val newLevels = when (mode) {
                                FilterMode.INCLUDE -> DefaultIncludeLevels
                                FilterMode.EXCLUDE -> emptySet()
                            }
                            draft = draft.copy(mode = mode, levels = newLevels)
                        },
                        label = {
                            Text(
                                text = when (mode) {
                                    FilterMode.INCLUDE -> stringResource(R.string.include)
                                    FilterMode.EXCLUDE -> stringResource(R.string.exclude)
                                }
                            )
                        }
                    )
                }
            }

            Text(
                text = stringResource(R.string.logcat_filter_level),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // All levels except SILENT/UNKNOWN which are rarely useful
            val displayLevels = listOf(
                LogLevel.VERBOSE,
                LogLevel.DEBUG,
                LogLevel.INFO,
                LogLevel.WARNING,
                LogLevel.ERROR,
                LogLevel.FATAL,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                displayLevels.forEach { level ->
                    val selected = level in draft.levels
                    val barColor = LogLevelColors.baseColor(level)
                    // Chip color: full-alpha bar color when selected, faded when not
                    val chipSelectedColor = barColor.copy(alpha = 0.25f)

                    FilterChip(
                        selected = selected,
                        onClick = {
                            draft = draft.copy(
                                levels = if (selected) {
                                    draft.levels - level
                                } else {
                                    draft.levels + level
                                }
                            )
                        },
                        label = { Text(level.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipSelectedColor,
                            selectedLabelColor = lerp(barColor, Color.Black, 0.3f),
                        ),
                    )
                }
            }

            FilterTextField(
                label = stringResource(R.string.tag),
                value = draft.tags.joinToString(","),
                onValueChange = { raw ->
                    draft = draft.copy(tags = raw.split(",").map { it.trim() }
                        .filter { it.isNotBlank() }.toSet())
                }
            )
            FilterTextField(
                label = stringResource(R.string.package_name),
                value = draft.packages.joinToString(","),
                onValueChange = { raw ->
                    draft = draft.copy(packages = raw.split(",").map { it.trim() }
                        .filter { it.isNotBlank() }.toSet())
                }
            )
            FilterTextField(
                label = stringResource(R.string.logcat_filter_pid),
                value = draft.pids.joinToString(","),
                onValueChange = { raw ->
                    draft = draft.copy(pids = raw.split(",").map { it.trim() }
                        .filter { it.isNotBlank() }.toSet())
                }
            )
            FilterTextField(
                label = stringResource(R.string.logcat_filter_tid),
                value = draft.tids.joinToString(","),
                onValueChange = { raw ->
                    draft = draft.copy(tids = raw.split(",").map { it.trim() }
                        .filter { it.isNotBlank() }.toSet())
                }
            )
            FilterTextField(
                label = stringResource(R.string.logcat_filter_uid),
                value = draft.uids.joinToString(","),
                onValueChange = { raw ->
                    draft = draft.copy(uids = raw.split(",").map { it.trim() }
                        .filter { it.isNotBlank() }.toSet())
                }
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        draft = LogFilter()
                        onApply(LogFilter())
                    }
                ) {
                    Text(stringResource(R.string.clear_all))
                }
                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = { showSaveDialog = true }
                ) {
                    Text(stringResource(R.string.save_as_profile))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onApply(draft); onDismiss() }
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveProfileDialog(
            onConfirm = { name ->
                onSaveProfile(name)
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false }
        )
    }
}

@Composable
private fun FilterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        placeholder = { Text("Optional") },
    )
}

@Composable
private fun SaveProfileDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.save_as_profile)) },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Profile name") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
