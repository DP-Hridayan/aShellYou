@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)

package `in`.hridayan.ashell.settings.presentation.components.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.utils.isKeyboardVisible
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.AppFont
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel.LookAndFeelViewModel
import `in`.hridayan.ashell.settings.presentation.provider.RadioGroupOptionsProvider
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun FontStyleBottomSheet(
    onDismiss: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    viewModel: LookAndFeelViewModel = hiltViewModel()
) {
    val res = LocalResources.current
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val scrollState = rememberScrollState()
    val interactionSources = remember { List(2) { MutableInteractionSource() } }
    val fontStyles = RadioGroupOptionsProvider.fontStyleOptions
    val selected = LocalSettings.current.fontFamily
    var tempSelected by remember { mutableIntStateOf(selected) }

    val isCheckedMatchCase by viewModel.isCheckedMatchCase.collectAsState()
    val isCheckedBold by viewModel.isCheckedBold.collectAsState()
    val isCheckedItalic by viewModel.isCheckedItalic.collectAsState()
    val isCheckedUnderline by viewModel.isCheckedUnderline.collectAsState()

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    val filteredFontStyles = remember(searchQuery.text, fontStyles) {
        if (searchQuery.text.isEmpty()) {
            fontStyles
        } else {
            fontStyles.filter { option ->
                val displayName = res.getString(option.labelResId)
                displayName.contains(searchQuery.text, ignoreCase = true)
            }
        }
    }

    val displayFont = AppFont.fromId(tempSelected).fontFamily
    val previewText = stringResource(R.string.font_display_text)
    val displayText = when {
        isCheckedMatchCase -> previewText.uppercase()
        else -> previewText.lowercase()
    }

    val isKeyboardVisible by isKeyboardVisible()

    LaunchedEffect(selected) {
        tempSelected = selected
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            AutoResizeableText(
                stringResource(R.string.font_family),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            AutoResizeableText(
                stringResource(R.string.preview),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(MaterialTheme.shapes.largeIncreased)
                    .border(
                        width = 1.dp,
                        shape = MaterialTheme.shapes.largeIncreased,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    .animateContentSize()
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                    text = displayText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = displayFont,
                        fontWeight = if (isCheckedBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (isCheckedItalic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = if (isCheckedUnderline) TextDecoration.Underline else TextDecoration.None
                    )
                )
            }

            TextFormatUtilityRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            CustomSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                hint = stringResource(R.string.search_fonts)
            )

            AnimatedVisibility(
                visible = filteredFontStyles.isEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_error),
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = null
                    )

                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        text = stringResource(R.string.no_search_results_found),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .verticalScroll(scrollState)
                    .animateContentSize()
            ) {
                filteredFontStyles.forEachIndexed { index, option ->
                    val shape = getRoundedShape(index, filteredFontStyles.size)

                    val isSelected = option.value == tempSelected

                    val cardColors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.run {
                            if (isSelected) primaryContainer else surfaceContainerLowest
                        },
                        contentColor = MaterialTheme.colorScheme.run {
                            if (isSelected) onPrimaryContainer else onSurface
                        }
                    )

                    val finalShape = if (isSelected) {
                        CustomCardShape(50)
                    } else {
                        shape
                    }

                    val labelNameFontFamily = AppFont.fromId(option.value).fontFamily

                    CustomCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        shape = finalShape,
                        colors = cardColors
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    onClick = withHaptic { tempSelected = option.value }
                                )
                                .padding(vertical = 8.dp, horizontal = 20.dp)
                        ) {
                            Text(
                                text = stringResource(option.labelResId),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = labelNameFontFamily
                            )

                            Spacer(Modifier.weight(1f))

                            RadioButton(
                                selected = (option.value == tempSelected),
                                onClick = withHaptic(if (isSelected) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff) {
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
            }

            @Suppress("DEPRECATION")
            AnimatedVisibility(
                visible = !isKeyboardVisible,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                ButtonGroup(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(interactionSources[0]),
                        shapes = ButtonDefaults.shapes(),
                        interactionSource = interactionSources[0],
                        onClick = withHaptic(HapticFeedbackType.Reject) {
                            onDismiss()
                        },
                        content = { Text(text = stringResource(R.string.cancel)) }
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
                        onClick = withHaptic(HapticFeedbackType.Confirm) {
                            settingsViewModel.setInt(
                                key = SettingsKeys.FONT_FAMILY,
                                value = tempSelected
                            )
                            onDismiss()
                        },
                        content = { Text(text = stringResource(R.string.apply)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TextFormatUtilityRow(
    modifier: Modifier = Modifier,
    viewModel: LookAndFeelViewModel = hiltViewModel()
) {
    val isCheckedMatchCase by viewModel.isCheckedMatchCase.collectAsState()
    val isCheckedBold by viewModel.isCheckedBold.collectAsState()
    val isCheckedItalic by viewModel.isCheckedItalic.collectAsState()
    val isCheckedUnderline by viewModel.isCheckedUnderline.collectAsState()

    val checkedContainerColor = MaterialTheme.colorScheme.primaryContainer
    val checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    val unCheckedContainerColor = BottomSheetDefaults.ContainerColor
    val unCheckedContentColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(
            containerColor = BottomSheetDefaults.ContainerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(if (isCheckedMatchCase) checkedContainerColor else unCheckedContainerColor)
                    .clickable(
                        enabled = true,
                        onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                            viewModel.toggleMatchCase()
                        }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_matchcase),
                    contentDescription = null,
                    tint = if (isCheckedMatchCase) checkedContentColor else unCheckedContentColor,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(if (isCheckedBold) checkedContainerColor else unCheckedContainerColor)
                    .clickable(
                        enabled = true,
                        onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                            viewModel.toggleBold()
                        }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_format_bold),
                    contentDescription = null,
                    tint = if (isCheckedBold) checkedContentColor else unCheckedContentColor,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(if (isCheckedItalic) checkedContainerColor else unCheckedContainerColor)
                    .clickable(
                        enabled = true,
                        onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                            viewModel.toggleItalic()
                        }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_format_italic),
                    contentDescription = null,
                    tint = if (isCheckedItalic) checkedContentColor else unCheckedContentColor,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(if (isCheckedUnderline) checkedContainerColor else unCheckedContainerColor)
                    .clickable(
                        enabled = true,
                        onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                            viewModel.toggleUnderline()
                        }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_format_underline),
                    contentDescription = null,
                    tint = if (isCheckedUnderline) checkedContentColor else unCheckedContentColor,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        enabled = true,
                        onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                            viewModel.formatClear()
                        }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_format_clear),
                    contentDescription = null,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }
        }
    }
}