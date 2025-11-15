@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.card.PillShapedCard
import `in`.hridayan.ashell.settings.presentation.model.PreferenceItem
import `in`.hridayan.ashell.settings.presentation.model.SettingsType
import `in`.hridayan.ashell.settings.presentation.util.getResolvedDescription
import `in`.hridayan.ashell.settings.presentation.util.getResolvedIcon
import `in`.hridayan.ashell.settings.presentation.util.getResolvedTitle
import `in`.hridayan.ashell.core.presentation.components.card.RoundedCornerCard
import `in`.hridayan.ashell.settings.presentation.components.switch.SettingsSwitch
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun BooleanPreferenceItemView(
    modifier: Modifier = Modifier,
    item: PreferenceItem,
    roundedShape: RoundedCornerShape,
    contentDescription: String = "",
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    if (!item.isLayoutVisible) return

    val weakHaptic = LocalWeakHaptic.current
    val icon = item.getResolvedIcon()
    val titleText = item.getResolvedTitle()
    val descriptionText = item.getResolvedDescription()

    val checked by settingsViewModel.isItemChecked(item.key)
        .collectAsState(initial = item.key.default as Boolean)

    val enabled by settingsViewModel.isItemEnabled(item.key).collectAsState(initial = true)

    val onClick: () -> Unit = {
        if (enabled) {
            settingsViewModel.onBooleanItemClicked(item.key)
            weakHaptic()
        }
    }

    if (item.type == SettingsType.SwitchBanner) {
        PillShapedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 0.dp),
            clickable = enabled,
            onClick = onClick,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = titleText,
                    style = MaterialTheme.typography.headlineSmall,
                )
                SettingsSwitch(
                    checked = checked,
                    onCheckedChange = {
                        onClick()
                    })
            }
        }
    }

    if (item.type == SettingsType.Switch) {
        RoundedCornerCard(
            modifier = Modifier.fillMaxWidth(),
            roundedCornerShape = roundedShape
        )
        {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = enabled,
                        onClick = onClick
                    )
                    .padding(horizontal = 20.dp, vertical = 17.dp)
                    .alpha(if (enabled) 1f else 0.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = contentDescription,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    if (titleText.isNotEmpty()) {
                        Text(
                            text = titleText,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                        )
                    }

                    if (descriptionText.isNotEmpty()) {
                        Text(
                            text = descriptionText,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.alpha(0.9f)
                        )
                    }
                }

                SettingsSwitch(
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = {
                        onClick()
                    })
            }
        }
    }
}