package `in`.hridayan.ashell.shell.common.presentation.components.dialog

import `in`.hridayan.ashell.core.resources.R


import `in`.hridayan.ashell.core.common.SettingsKeys

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.ui.provider.RadioGroupOptionsProvider

@Composable
fun BookmarksSortDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    initialSort: Int,
    onSortChange: (Int) -> Unit,
) {
    val sortOptions = RadioGroupOptionsProvider.bookmarkSortOptions
    var tempSelected by remember { mutableIntStateOf(initialSort) }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.sort),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                sortOptions.forEachIndexed { index, option ->
                    val shape = getRoundedShape(index, sortOptions.size)

                    val selected = option.value == tempSelected

                    val cardColors = if (selected) CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) else CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )

                    val finalShape = if (selected) {
                        CustomCardShape(50)
                    } else {
                        shape
                    }

                    CustomCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(1.dp),
                        shape = finalShape,
                        colors = cardColors,
                        onClick = withHaptic(HapticFeedbackType.ToggleOn) {
                            tempSelected = option.value
                        }
                    )
                    {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 20.dp)
                        ) {
                            Text(
                                text = stringResource(option.labelResId),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.weight(1f))

                            RadioButton(
                                selected = (option.value == tempSelected),
                                onClick = withHaptic(HapticFeedbackType.ToggleOn) {
                                    tempSelected = option.value
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    unselectedColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                val buttonGroupItems = listOf(
                    ButtonGroupItem(
                        buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                        text = stringResource(R.string.cancel),
                        onClick = { onDismiss() }
                    ),
                    ButtonGroupItem(
                        text = stringResource(R.string.sort),
                        onClick = {
                            onSortChange(tempSelected)
                            onDismiss()
                        }
                    )
                )

                OverflowButtonGroup(
                    items = buttonGroupItems,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}
