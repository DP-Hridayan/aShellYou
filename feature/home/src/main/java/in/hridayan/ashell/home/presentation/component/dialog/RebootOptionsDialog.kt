package `in`.hridayan.ashell.home.presentation.component.dialog

import androidx.annotation.DrawableRes
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R

private val GRID_GAP = 12.dp
private val CELL_PADDING = 8.dp
private val ICON_SIZE = 36.dp
private val ICON_LABEL_SPACING = 8.dp
private val DIALOG_MIN_WIDTH = 280.dp

private const val SU_COMMAND = "su"
private const val COMMAND_FLAG = "-c"
private const val REBOOT_COMMAND = "reboot"
private const val REBOOT_RECOVERY_COMMAND = "reboot recovery"
private const val REBOOT_BOOTLOADER_COMMAND = "reboot bootloader"
private const val REBOOT_FASTBOOT_COMMAND = "reboot fastboot"

private sealed interface RebootOptionIcon {
    data class Vector(val imageVector: ImageVector) : RebootOptionIcon
    data class Drawable(@param:DrawableRes val id: Int) : RebootOptionIcon
}

private data class RebootOptionItem(
    val nameRes: Int,
    val icon: RebootOptionIcon,
    val containerColor: Color,
    val contentColor: Color,
    val command: Array<String>
)

@Composable
fun RebootOptionsContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onReboot: (Array<String>) -> Unit = {}
) {
    val options = listOf(
        RebootOptionItem(
            nameRes = R.string.system,
            icon = RebootOptionIcon.Vector(Icons.Rounded.RestartAlt),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            command = arrayOf(SU_COMMAND, COMMAND_FLAG, REBOOT_COMMAND)
        ),
        RebootOptionItem(
            nameRes = R.string.recovery,
            icon = RebootOptionIcon.Drawable(R.drawable.ic_restart_recovery),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            command = arrayOf(SU_COMMAND, COMMAND_FLAG, REBOOT_RECOVERY_COMMAND)
        ),
        RebootOptionItem(
            nameRes = R.string.bootloader,
            icon = RebootOptionIcon.Drawable(R.drawable.ic_restart_bootloader),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            command = arrayOf(SU_COMMAND, COMMAND_FLAG, REBOOT_BOOTLOADER_COMMAND)
        ),
        RebootOptionItem(
            nameRes = R.string.fastboot,
            icon = RebootOptionIcon.Drawable(R.drawable.ic_restart_fastboot),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            command = arrayOf(SU_COMMAND, COMMAND_FLAG, REBOOT_FASTBOOT_COMMAND)
        )
    )

    Grid(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(min = DIALOG_MIN_WIDTH),
        config = {
            column(1.fr)
            column(1.fr)
            row(GridTrackSize.Auto)
            row(GridTrackSize.Auto)
            gap(GRID_GAP)
        }
    ) {
        options.forEach { option ->
            RebootOptionSquare(
                option = option,
                onClick = withHaptic {
                    onReboot(option.command)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun RebootOptionsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onReboot: (Array<String>) -> Unit
) {
    DialogContainer(onDismiss = onDismiss) {
        RebootOptionsContent(
            modifier = modifier,
            onDismiss = onDismiss,
            onReboot = onReboot
        )
    }
}

@Composable
private fun RebootOptionSquare(
    option: RebootOptionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = MaterialTheme.shapes.large,
        color = option.containerColor,
        contentColor = option.contentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CELL_PADDING),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val icon = option.icon) {
                is RebootOptionIcon.Vector -> Icon(
                    imageVector = icon.imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(ICON_SIZE),
                    tint = option.contentColor
                )

                is RebootOptionIcon.Drawable -> Icon(
                    painter = painterResource(icon.id),
                    contentDescription = null,
                    modifier = Modifier.size(ICON_SIZE),
                    tint = option.contentColor
                )
            }

            Spacer(modifier = Modifier.height(ICON_LABEL_SPACING))

            Text(
                text = stringResource(option.nameRes),
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = option.contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RebootOptionsDialogPreview() {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            RebootOptionsContent()
        }
    }
}


