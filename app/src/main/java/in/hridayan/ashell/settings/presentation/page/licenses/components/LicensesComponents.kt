@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.page.licenses.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.card.CustomCardDefaults
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.utils.openUrl
import `in`.hridayan.ashell.settings.domain.model.LibraryItem

/**
 * Prominent card shown at the top of the screen displaying the app's own licenses (GPLv3).
 *
 * Includes the licenses name, a short description, a clickable URL to the full text,
 * and a button to open a full-text dialog.
 */
@Composable
fun AppLicenseCard(
    modifier: Modifier = Modifier,
    onReadFullText: () -> Unit,
) {
    val context = LocalContext.current

    CustomCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardCornerShape.SINGLE_CARD,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        pressedScale = 1f,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.this_application),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // License badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.clip(MaterialTheme.shapes.small),
            ) {
                Text(
                    text = stringResource(R.string.gpl_3_0),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.gpl_3_0_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                        openUrl(
                            UrlConst.URL_GITHUB_REPO_LICENSE,
                            context
                        )
                    },
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 2.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    Text(
                        text = stringResource(R.string.view_on_github),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                TextButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) { onReadFullText() }) {
                    Text(
                        text = stringResource(R.string.full_license_text),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Single expandable row for a third-party dependency.
 * Tapping the row expands metadata; the chip/URL buttons are always visible.
 */
@Composable
fun LibraryListItem(
    library: LibraryItem,
    modifier: Modifier = Modifier,
    shape: CustomCardShape = CustomCardDefaults.shape(),
    onShowDetail: (LibraryItem) -> Unit,
) {
    val context = LocalContext.current
    var expanded by rememberSaveable { mutableStateOf(false) }

    CustomCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        onClick = withHaptic { expanded = !expanded },
        pressedScale = 0.98f,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = library.name.take(1).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                ) {
                    Text(
                        text = library.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    library.version?.let { ver ->
                        Text(
                            text = ver,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded)
                        stringResource(R.string.collapse)
                    else
                        stringResource(R.string.expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Expanded metadata
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Artifact ID
                    MetadataRow(
                        icon = Icons.Rounded.Code,
                        label = stringResource(R.string.artifact_id),
                        value = library.artifactId,
                    )

                    // Developers
                    if (library.developers.isNotEmpty()) {
                        MetadataRow(
                            icon = Icons.Rounded.Person,
                            label = stringResource(R.string.developer),
                            value = library.developers.joinToString(", "),
                        )
                    }

                    // License
                    library.licenseName?.let { lic ->
                        MetadataRow(
                            icon = Icons.Rounded.AccountBalance,
                            label = stringResource(R.string.license_label),
                            value = lic,
                        )
                    }

                    // Website / SCM
                    val link = library.website ?: library.scmUrl
                    if (link != null) {
                        Row(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .clickable { openUrl(link, context) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = link,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    // View licenses text button (only if content is available)
                    if (!library.licenseContent.isNullOrBlank() || library.licenseUrl != null) {
                        TextButton(
                            onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                onShowDetail(
                                    library
                                )
                            },
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text(
                                text = stringResource(R.string.full_license_text),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(14.dp)
                .padding(top = 2.dp),
        )
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}