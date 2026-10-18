package `in`.hridayan.ashell.shell.common.presentation.components.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.radio.RadioOptionCard
import `in`.hridayan.ashell.core.presentation.provider.RadioGroupOptionsProvider
import `in`.hridayan.ashell.core.presentation.theme.AshellYouAnimationSpecs
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.resources.R

private const val ROTATION_COLLAPSED = 0f
private const val ROTATION_EXPANDED = 180f
private val CardHorizontalPadding = 16.dp
private val CardVerticalPadding = 14.dp
private val OptionsMarginHorizontal = 10.dp
private val OptionsMarginTop = 2.dp
private val OptionsMarginBottom = 10.dp
private val OptionContentPadding = PaddingValues(vertical = 4.dp, horizontal = 20.dp)

@Composable
fun LocalAdbModeCollapsibleCard(
    selectedMode: Int,
    onModeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    CustomCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = AshellYouAnimationSpecs.springIntSize),
        shape = CardCornerShape.SINGLE_CARD,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LocalAdbModeHeader(
                isExpanded = isExpanded,
                onToggleExpand = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LocalAdbModeOptionsList(
                    selectedMode = selectedMode,
                    onModeSelected = onModeSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = OptionsMarginHorizontal,
                            end = OptionsMarginHorizontal,
                            top = OptionsMarginTop,
                            bottom = OptionsMarginBottom
                        )
                )
            }
        }
    }
}

@Composable
private fun LocalAdbModeHeader(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) ROTATION_EXPANDED else ROTATION_COLLAPSED,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
    )
    val click = withHaptic(HapticFeedbackType.VirtualKey, onToggleExpand)

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = click)
            .padding(horizontal = CardHorizontalPadding, vertical = CardVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.local_adb_mode),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.rotate(rotation)
        )
    }
}

@Composable
private fun LocalAdbModeOptionsList(
    selectedMode: Int,
    onModeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = RadioGroupOptionsProvider.localAdbShellModeOptions

    Column(modifier = modifier) {
        options.forEachIndexed { index, option ->
            option.labelResId?.let { labelResId ->
                val isSelected = option.value == selectedMode
                RadioOptionCard(
                    label = stringResource(labelResId),
                    selected = isSelected,
                    index = index,
                    count = options.size,
                    cardColors = if (isSelected) {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    } else {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    border = if (isSelected) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onTertiaryContainer)
                    } else {
                        null
                    },
                    contentPadding = OptionContentPadding,
                    radioButtonColors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        unselectedColor = MaterialTheme.colorScheme.onSurface
                    ),
                    onSelect = { onModeSelected(option.value) }
                )
            }
        }
    }
}
