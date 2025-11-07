@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.commandexamples.presentation.component.card

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.presentation.component.dialog.EditCommandDialog
import `in`.hridayan.ashell.commandexamples.presentation.component.row.Labels
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandExamplesViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.card.CollapsibleCard
import `in`.hridayan.ashell.core.presentation.utils.SnackBarUtils
import `in`.hridayan.ashell.core.utils.ClipboardUtils
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CommandExampleCard(
    modifier: Modifier,
    id: Int,
    command: String,
    description: String,
    isFavourite: Boolean,
    labels: List<String>,
    commandExamplesViewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val weakHaptic = LocalWeakHaptic.current
    val screenDensity = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val prevScreen = navController.previousBackStackEntry
    val shellViewModel: ShellViewModel =
        if (prevScreen != null) hiltViewModel(prevScreen) else hiltViewModel()
    val interactionSources = remember { List(3) { MutableInteractionSource() } }
    var isDeleted by rememberSaveable { mutableStateOf(false) }
    var isEditDialogOpen by rememberSaveable { mutableStateOf(false) }
    val animatedHeight = remember { Animatable(1f) }
    var topPadding by remember(id) { mutableStateOf(15.dp) }
    val swipeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val cardWidth = remember { mutableFloatStateOf(0f) }
    val cardHeight = remember { mutableFloatStateOf(0f) }
    var lastHapticZone by remember { mutableStateOf(0) }

    val compositionDeleteLottie by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.icons8_trash))

    val deleteLottieProgress by animateLottieCompositionAsState(
        composition = compositionDeleteLottie,
        isPlaying = swipeOffset.value < -0.25f * cardWidth.floatValue
    )

    val compositionEditLottie by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.icons8_edit))

    val editLottieProgress by animateLottieCompositionAsState(
        composition = compositionEditLottie,
        isPlaying = swipeOffset.value > 0.25f * cardWidth.floatValue
    )

    val onDelete: () -> Unit = {
        isDeleted = true
        SnackBarUtils.showSnackBarWithAction(
            message = context.getString(R.string.item_deleted),
            actionText = context.getString(R.string.undo),
            durationMillis = 2500,
            onActionClicked = {
                coroutineScope.launch {
                    swipeOffset.snapTo(0f)
                    topPadding = 15.dp
                }
                isDeleted = false
            },
            onDismiss = {
                if (isDeleted) {
                    commandExamplesViewModel.deleteCommand(
                        id = id,
                        onSuccess = { isDeleted = true })
                }
            }
        )
    }

    val onEdit: () -> Unit = {
        coroutineScope.launch {
            commandExamplesViewModel.setFieldsForEdit(id = id)
            isEditDialogOpen = true
        }
    }

    val borderAlpha by animateFloatAsState(
        targetValue = if (swipeOffset.value == 0f) 0.5f else 0f,
        animationSpec = tween(durationMillis = 250, easing = LinearEasing)
    )

    val borderStroke = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = borderAlpha)
    )

    if (!isDeleted || swipeOffset.value != 0f)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp, top = topPadding)
                .onGloballyPositioned {
                    if (cardHeight.floatValue == 0f || !isDeleted) {
                        cardHeight.floatValue = it.size.height.toFloat()
                    }
                    cardWidth.floatValue = it.size.width.toFloat()
                }
                .then(
                    if (isDeleted && cardHeight.floatValue > 0f)
                        Modifier.height(
                            with(screenDensity) {
                                (cardHeight.floatValue * animatedHeight.value).toDp()
                            }
                        )
                    else Modifier
                ),
            contentAlignment = if (swipeOffset.value > 0) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            if (swipeOffset.value != 0f) {
                Box(
                    modifier = Modifier
                        .width(with(screenDensity) { abs(swipeOffset.value).toDp() })
                        .height(with(screenDensity) { cardHeight.floatValue.toDp() })
                        .clip(with(screenDensity) { RoundedCornerShape(cardHeight.floatValue.toDp() / 2) })
                        .background(
                            when {
                                swipeOffset.value > 0 -> MaterialTheme.colorScheme.primary
                                swipeOffset.value < 0 -> MaterialTheme.colorScheme.error
                                else -> Color.Transparent
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (swipeOffset.value > 0)
                        EditLottie(
                            composition = compositionEditLottie,
                            progress = editLottieProgress,
                        )
                    else DeleteLottie(
                        composition = compositionDeleteLottie,
                        progress = deleteLottieProgress,
                    )
                }
            }

            CollapsibleCard(
                modifier = Modifier
                    .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                val width = cardWidth.floatValue
                                if (width == 0f) return@detectHorizontalDragGestures

                                val elasticLimit = width * 0.25f
                                val newOffset = swipeOffset.value + dragAmount
                                val absOffset = abs(newOffset)

                                val adjustedOffset = when {
                                    absOffset < elasticLimit -> {
                                        val resistance = 1f - (absOffset / elasticLimit)
                                        val resistanceFactor = 0.25f + (resistance * 0.75f)
                                        swipeOffset.value + (dragAmount * resistanceFactor)
                                    }

                                    else -> newOffset
                                }

                                scope.launch {
                                    swipeOffset.snapTo(adjustedOffset.coerceIn(-width, width))
                                }

                                val currentZone = when {
                                    swipeOffset.value > elasticLimit -> 1
                                    swipeOffset.value < -elasticLimit -> -1
                                    else -> 0
                                }

                                if (currentZone != lastHapticZone) {
                                    weakHaptic()
                                    lastHapticZone = currentZone
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    lastHapticZone = 0
                                    val width = cardWidth.floatValue
                                    val elasticLimit = width * 0.25f

                                    when {
                                        swipeOffset.value > elasticLimit -> {
                                            swipeOffset.animateTo(
                                                width,
                                                spring(stiffness = Spring.StiffnessMediumLow)
                                            )
                                            onEdit()
                                            swipeOffset.snapTo(0f)
                                        }

                                        swipeOffset.value < -elasticLimit -> {
                                            swipeOffset.animateTo(
                                                -width,
                                                spring(stiffness = Spring.StiffnessMediumLow)
                                            )

                                            scope.launch {
                                                animatedHeight.animateTo(
                                                    0f,
                                                    animationSpec = tween(
                                                        durationMillis = 130,
                                                        easing = FastOutLinearInEasing
                                                    )
                                                )
                                                topPadding = 0.dp
                                            }

                                            onDelete()
                                        }

                                        else -> {
                                            swipeOffset.animateTo(
                                                0f,
                                                spring(stiffness = Spring.StiffnessLow)
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                    .fillMaxWidth(),
                border = borderStroke,
                collapsedContent = {
                    if (labels.isNotEmpty()) Labels(
                        modifier = Modifier.fillMaxWidth(),
                        labels = labels
                    )

                    Text(
                        text = command,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                },

                expandedContent = {
                    if (description.isNotEmpty()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 5.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        @Suppress("DEPRECATION")
                        ButtonGroup(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            EditButton(
                                onEdit = onEdit,
                                interactionSource = interactionSources[0],
                                modifier = Modifier
                                    .size(40.dp)
                                    .animateWidth(interactionSources[0])
                            )
                            DeleteButton(
                                interactionSource = interactionSources[1],
                                modifier = Modifier
                                    .size(40.dp)
                                    .animateWidth(interactionSources[1]),
                                onClick = onDelete
                            )
                            CopyButton(
                                id = id,
                                interactionSource = interactionSources[2],
                                modifier = Modifier
                                    .size(40.dp)
                                    .animateWidth(interactionSources[2])
                            )
                        }

                        UseCommandButton(onClick = {
                            shellViewModel.onCommandTextFieldChange(
                                TextFieldValue(text = command)
                            )
                            shellViewModel.updateTextFieldSelection()
                            navController.popBackStack()
                            commandExamplesViewModel.incrementUseCount(id)
                        })
                    }
                })
        }

    if (isEditDialogOpen) EditCommandDialog(id = id, onDismiss = { isEditDialogOpen = false })
}

@Composable
private fun DeleteButton(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit = {},
) {
    val weakHaptic = LocalWeakHaptic.current

    IconButton(
        onClick = {
            weakHaptic()
            onClick()
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shapes = IconButtonDefaults.shapes(),
        interactionSource = interactionSource,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_delete),
            contentDescription = null,
        )
    }
}

@Composable
private fun EditButton(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    onEdit: () -> Unit = {}
) {
    val weakHaptic = LocalWeakHaptic.current

    IconButton(
        onClick = {
            weakHaptic()
            onEdit()
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        shapes = IconButtonDefaults.shapes(),
        interactionSource = interactionSource,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_edit),
            contentDescription = null,
        )
    }
}

@Composable
private fun CopyButton(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    id: Int,
    viewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    IconButton(
        onClick = {
            weakHaptic()
            coroutineScope.launch {
                val command = viewModel.getCommandById(id) ?: ""
                if (command.isNotEmpty()) {
                    ClipboardUtils.copyToClipboard(text = command, context = context)
                    showToast(context, context.getString(R.string.copied_to_clipboard))
                } else {
                    showToast(context, context.getString(R.string.command_not_found))
                }
            }
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shapes = IconButtonDefaults.shapes(),
        interactionSource = interactionSource,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_copy),
            contentDescription = null,
        )
    }
}

@Composable
private fun UseCommandButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val weakHaptic = LocalWeakHaptic.current
    val size = ButtonDefaults.ExtraSmallContainerHeight

    Button(
        onClick = {
            weakHaptic()
            onClick()
        },
        modifier = modifier.heightIn(size),
        shapes = ButtonDefaults.shapes(),
        contentPadding = ButtonDefaults.contentPaddingFor(size)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_open_in_new),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.iconSizeFor(size))
        )

        Spacer(Modifier.widthIn(ButtonDefaults.iconSpacingFor(size)))

        Text(text = stringResource(R.string.use))
    }
}

/**
 * @Composable
 * private fun FavouriteButton(
 *     id: Int,
 *     isFavourite: Boolean,
 *     viewModel: CommandExamplesViewModel
 * ) {
 *     FavouriteIconButton(
 *         isFavorite = isFavourite,
 *         onToggle = {
 *             viewModel.toggleFavourite(
 *                 id = id,
 *                 isFavourite = !isFavourite,
 *                 onSuccess = {}
 *             )
 *         })
 * }
 */


@Composable
private fun DeleteLottie(
    modifier: Modifier = Modifier,
    composition: LottieComposition?,
    progress: Float
) {
    val color = MaterialTheme.colorScheme.onError

    val colorProperty = rememberLottieDynamicProperty(
        property = LottieProperty.COLOR,
        keyPath = arrayOf("**"),
        value = color.toArgb()
    )

    val dynamicProperties = rememberLottieDynamicProperties(colorProperty)

    LottieAnimation(
        composition = composition,
        progress = { progress },
        dynamicProperties = dynamicProperties,
        modifier = modifier.size(36.dp)
    )
}

@Composable
private fun EditLottie(
    modifier: Modifier = Modifier,
    composition: LottieComposition?,
    progress: Float
) {
    val color = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier
            .graphicsLayer {
                alpha = 1f
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                drawContent()
                drawRect(color = color, blendMode = BlendMode.SrcAtop)
            }
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(36.dp)
        )
    }
}