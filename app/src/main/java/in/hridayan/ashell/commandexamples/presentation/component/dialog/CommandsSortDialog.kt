@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.commandexamples.presentation.component.dialog

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.card.RoundedCornerCard
import `in`.hridayan.ashell.core.presentation.components.shape.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.settings.presentation.provider.RadioGroupOptionsProvider
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun CommandsSortDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val weakHaptic = LocalWeakHaptic.current
    val interactionSources = remember { List(2) { MutableInteractionSource() } }
    val sortOptions = RadioGroupOptionsProvider.commandSortOptions
    val selected =
        settingsViewModel.getInt(key = SettingsKeys.COMMAND_SORT_TYPE)
            .collectAsState(initial = SettingsKeys.COMMAND_SORT_TYPE.default as Int)
    var tempSelected by remember { mutableIntStateOf(selected.value) }

    LaunchedEffect(selected.value) {
        tempSelected = selected.value
    }

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
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                sortOptions.forEachIndexed { index, option ->
                    val shape = getRoundedShape(index, sortOptions.size)

                    val selected = option.value == tempSelected

                    val animatedCorner by animateDpAsState(
                        targetValue = if (selected) 32.dp else 4.dp,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        ),
                        label = "cornerAnimation"
                    )

                    val finalShape = if (selected) {
                        RoundedCornerShape(animatedCorner)
                    } else {
                        shape
                    }

                    RoundedCornerCard(
                        modifier = Modifier.fillMaxWidth(),
                        roundedCornerShape = finalShape,
                        paddingValues = PaddingValues(vertical = 1.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                    {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempSelected = option.value
                                    weakHaptic()
                                }
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
                                onClick = {
                                    tempSelected = option.value
                                    weakHaptic()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    unselectedColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            )
                        }
                    }
                }

                @Suppress("DEPRECATION")
                ButtonGroup(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(interactionSources[0]),
                        shapes = ButtonDefaults.shapes(),
                        interactionSource = interactionSources[0],
                        onClick = {
                            weakHaptic()
                            onDismiss()
                        },
                        content = { AutoResizeableText(text = stringResource(R.string.cancel)) }
                    )

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(interactionSources[1]),
                        shapes = ButtonDefaults.shapes(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        interactionSource = interactionSources[1],
                        onClick = {
                            weakHaptic()
                            settingsViewModel.setInt(
                                key = SettingsKeys.COMMAND_SORT_TYPE,
                                value = tempSelected
                            )
                            onDismiss()
                        },
                        content = { AutoResizeableText(text = stringResource(R.string.sort)) }
                    )
                }
            }
        }
    }
}