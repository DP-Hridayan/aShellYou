package `in`.hridayan.ashell.settings.presentation.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.openUrl
import `in`.hridayan.ashell.settings.domain.model.LibraryItem

/**
 * Full-screen-ish dialog that renders the complete licenses text for [library].
 * Falls back to showing the licenses URL if no content is available.
 */
@Composable
fun LicenseDetailDialog(
    library: LibraryItem,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .heightIn(max = responsiveHeight()),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = library.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                library.licenseName?.let { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                val scrollState = rememberScrollState()

                if (!library.licenseContent.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                MaterialTheme.shapes.medium,
                            )
                            .padding(12.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = library.licenseContent,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.view_license_online_msg),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                val buttonGroupItems = listOf(
                    ButtonGroupItem(
                        buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                        text = stringResource(R.string.dismiss),
                        onClick = { onDismiss() }
                    ),
                    ButtonGroupItem(
                        buttonConfig = ButtonConfigDefaults.defaultConfig(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ),
                        text = stringResource(R.string.view_on_github),
                        enabled = library.licenseUrl != null,
                        onClick = withHaptic {
                            library.licenseUrl?.let { url ->
                                openUrl(url, context)
                            }
                        },
                    )
                )

                OverflowButtonGroup(items = buttonGroupItems)
            }
        }
    }
}

@Composable
private fun responsiveHeight(): Dp {
    return with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp() * 0.75f
    }
}
