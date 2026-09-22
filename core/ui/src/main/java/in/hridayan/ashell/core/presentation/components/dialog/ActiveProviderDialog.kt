package `in`.hridayan.ashell.core.presentation.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.resources.R

private const val SELECTED_CARD_SHAPE_PERCENT = 50

/**
 * Lets the user pick which provider AI features should use.
 *
 * Stateless by design: confirming is refused when the chosen provider has no API key, and the
 * caller reports that back through [errorMessage] so the dialog can stay open with the selection
 * intact rather than silently saving something unusable.
 *
 * @param selected The provider currently chosen, or `null` for "None".
 */
@Composable
fun ActiveProviderDialog(
    providers: List<LlmProvider>,
    selected: LlmProvider?,
    errorMessage: String?,
    onSelect: (LlmProvider?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DialogContainer(onDismiss = onDismiss) {
        AutoResizeableText(
            text = stringResource(R.string.active_provider),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        val optionCount = providers.size + 1

        ProviderOptionCard(
            label = stringResource(R.string.none),
            isSelected = selected == null,
            shape = getRoundedShape(0, optionCount),
            onSelect = { onSelect(null) },
        )

        providers.forEachIndexed { index, provider ->
            ProviderOptionCard(
                label = provider.displayName,
                isSelected = selected == provider,
                shape = getRoundedShape(index + 1, optionCount),
                onSelect = { onSelect(provider) },
            )
        }

        errorMessage?.let { ProviderSelectionError(message = it) }

        OverflowButtonGroup(
            items = listOf(
                ButtonGroupItem(
                    buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss
                ),
                ButtonGroupItem(
                    text = stringResource(R.string.ok),
                    onClick = onConfirm
                )
            ),
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Composable
private fun ProviderOptionCard(
    label: String,
    isSelected: Boolean,
    shape: CustomCardShape,
    onSelect: () -> Unit,
) {
    val cardColors = if (isSelected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    CustomCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = if (isSelected) CustomCardShape(SELECTED_CARD_SHAPE_PERCENT) else shape,
        colors = cardColors,
        onClick = withHaptic(HapticFeedbackType.ToggleOn) { onSelect() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 20.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.weight(1f))

            RadioButton(
                selected = isSelected,
                onClick = withHaptic(HapticFeedbackType.ToggleOn) { onSelect() },
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun ProviderSelectionError(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(top = 2.dp),
            painter = painterResource(R.drawable.ic_error),
            tint = MaterialTheme.colorScheme.error,
            contentDescription = null
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}
