@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.commandexamples.presentation.component.bottomsheet

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.presentation.component.row.Labels
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandExamplesViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.card.RoundedCornerCard
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.components.shape.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText

@Composable
fun CommandsFilterBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    commandExamplesViewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val weakHaptic = LocalWeakHaptic.current
    val lazyListState = rememberLazyListState()
    val states by commandExamplesViewModel.states.collectAsState()
    val searchedLabels by commandExamplesViewModel.searchedLabels.collectAsState()
    val filteredLabels by commandExamplesViewModel.filteredLabels.collectAsState()
    var cardHeight by remember { mutableStateOf(0.dp) }
    val screenDensity = LocalDensity.current

    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AutoResizeableText(
                text = stringResource(R.string.filter_by_labels),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (filteredLabels.isNotEmpty()) {
                Labels(
                    modifier = Modifier.fillMaxWidth(),
                    labels = filteredLabels,
                    showCrossIcon = true,
                    crossIconOnClick = { label ->
                        commandExamplesViewModel.toggleFilteredLabels(label = label)
                    }
                )
            }

            CustomSearchBar(
                modifier = Modifier.fillMaxWidth(),
                value = states.labelField.fieldValue,
                onValueChange = { commandExamplesViewModel.onLabelFieldTextChange(it) },
                trailingIcon = {
                    if (states.labelField.fieldValue.text.isNotEmpty()) {
                        Icon(
                            painter = painterResource(R.drawable.ic_clear),
                            contentDescription = null,
                            modifier = Modifier.clickable(
                                enabled = true,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    weakHaptic()
                                    commandExamplesViewModel.onLabelFieldTextChange(TextFieldValue(""))
                                }
                            ))
                    }
                }
            )

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                itemsIndexed(searchedLabels) { index, label ->

                    val shape = getRoundedShape(index, searchedLabels.size)

                    val selected = label in filteredLabels

                    val animatedCorner by animateDpAsState(
                        targetValue = if (selected) cardHeight / 2 else 4.dp,
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

                    val containerColor =
                        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest

                    val contentColor =
                        if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

                    RoundedCornerCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                cardHeight = with(screenDensity) { coordinates.size.height.toDp() }
                            },
                        paddingValues = PaddingValues(horizontal = 0.dp, vertical = 1.dp),
                        roundedCornerShape = finalShape,
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor,
                            contentColor = contentColor
                        ),
                        onClick = {
                            weakHaptic()
                            commandExamplesViewModel.toggleFilteredLabels(label)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                text = label,
                                style = MaterialTheme.typography.bodyMediumEmphasized,
                            )

                            if (selected) Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}