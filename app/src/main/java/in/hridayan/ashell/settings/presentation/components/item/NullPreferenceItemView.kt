@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.presentation.components.card.RoundedCornerCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.settings.presentation.model.PreferenceItem
import `in`.hridayan.ashell.settings.presentation.util.getResolvedDescription
import `in`.hridayan.ashell.settings.presentation.util.getResolvedIcon
import `in`.hridayan.ashell.settings.presentation.util.getResolvedTitle
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun NullPreferenceItemView(
    modifier: Modifier = Modifier,
    item: PreferenceItem,
    roundedShape: RoundedCornerShape,
    contentDescription: String = "",
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    if (!item.isLayoutVisible) return

    val icon = item.getResolvedIcon()
    val titleText = item.getResolvedTitle()
    val descriptionText = item.getResolvedDescription()

    RoundedCornerCard(
        modifier = Modifier.fillMaxWidth(),
        roundedCornerShape = roundedShape
    )
    {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    onClick = withHaptic {
                        settingsViewModel.onItemClicked(item.key)
                    })
                .padding(horizontal = 20.dp, vertical = 17.dp),
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
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
        }
    }
}