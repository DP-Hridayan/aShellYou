@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.dialog

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalPaletteStyle
import `in`.hridayan.ashell.core.common.LocalSeedColor
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.domain.model.PaletteStyle
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.theme.color.createDynamicScheme
import `in`.hridayan.ashell.core.presentation.theme.color.getPaletteKeyColors
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PaletteStylePickerDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: (PaletteStyle) -> Unit,
) {
    val interactionSources = remember {
        List(2) { MutableInteractionSource() }
    }

    val isHapticAllowed = LocalSettings.current.isHapticEnabled
    val isDarkMode = LocalDarkMode.current
    val haptic = LocalHapticFeedback.current
    val currentStyle = LocalPaletteStyle.current
    val primarySeed = LocalSeedColor.current.primary
    val styles = PaletteStyle.entries.toList()

    var tempSelected by remember {
        mutableStateOf(currentStyle)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(listState) {

        var previousTop = true
        var previousBottom = false
        var firstEmission = true

        snapshotFlow {
            Pair(
                !listState.canScrollBackward,
                !listState.canScrollForward
            )
        }
            .distinctUntilChanged()
            .collect { (atTop, atBottom) ->

                if (firstEmission) {
                    previousTop = atTop
                    previousBottom = atBottom
                    firstEmission = false
                    return@collect
                }

                val reachedTop = atTop && !previousTop
                val reachedBottom = atBottom && !previousBottom

                if (reachedTop || reachedBottom) {
                    if (isHapticAllowed) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                }

                previousTop = atTop
                previousBottom = atBottom
            }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true
        )
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
                    text = stringResource(R.string.palette_style),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {

                        itemsIndexed(styles) { index, style ->

                            val shape = getRoundedShape(index, styles.size)
                            val selected = style == tempSelected

                            // Compute per-style primary container color from this style's own scheme
                            val styleScheme = remember(primarySeed, style) {
                                createDynamicScheme(primarySeed, style, isDark = false)
                            }
                            val stylePrimaryContainer = remember(styleScheme) {
                                @SuppressLint("RestrictedApi")
                                Color(styleScheme.primaryPalette.tone(if (isDarkMode) 30 else 90))
                            }
                            val styleOnPrimaryContainer = remember(styleScheme) {
                                @SuppressLint("RestrictedApi")
                                Color(styleScheme.primaryPalette.tone(if (isDarkMode) 90 else 10))
                            }

                            val stylePrimary = remember(styleScheme) {
                                @SuppressLint("RestrictedApi")
                                Color(styleScheme.primaryPalette.tone(if (isDarkMode) 80 else 40))
                            }

                            val cardColors = if (selected) CardDefaults.cardColors(
                                containerColor = stylePrimaryContainer,
                                contentColor = styleOnPrimaryContainer
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
                                    tempSelected = style
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = 8.dp,
                                            horizontal = 20.dp
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    MiniPalettePreview(
                                        primarySeedArgb = primarySeed,
                                        paletteStyle = style,
                                    )

                                    Text(
                                        text = stringResource(style.displayNameResId),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    RadioButton(
                                        selected = selected,
                                        onClick = withHaptic(HapticFeedbackType.ToggleOn) {
                                            tempSelected = style
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = stylePrimary,
                                            unselectedColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = listState.canScrollBackward,
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut(),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surfaceContainer,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = listState.canScrollForward,
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.surfaceContainer
                                        )
                                    )
                                )
                        )
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
                        onClick = withHaptic(HapticFeedbackType.Reject) {
                            onDismiss()
                        },
                        content = {
                            AutoResizeableText(
                                text = stringResource(R.string.cancel)
                            )
                        }
                    )

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(interactionSources[1]),
                        shapes = ButtonDefaults.shapes(),
                        interactionSource = interactionSources[1],
                        onClick = withHaptic(HapticFeedbackType.Confirm) {
                            onConfirm(tempSelected)
                            onDismiss()
                        },
                        content = {
                            AutoResizeableText(
                                text = stringResource(R.string.apply)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniPalettePreview(
    primarySeedArgb: Int,
    paletteStyle: PaletteStyle,
) {

    val keyColors = remember(primarySeedArgb, paletteStyle) {
        getPaletteKeyColors(
            primarySeedArgb,
            paletteStyle
        )
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(color = keyColors.primary)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(color = keyColors.secondary)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(color = keyColors.tertiary)
                )
            }
        }
    }
}