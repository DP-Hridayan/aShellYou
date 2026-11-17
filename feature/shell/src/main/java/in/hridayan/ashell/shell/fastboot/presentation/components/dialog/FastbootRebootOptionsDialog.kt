package `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog

import androidx.annotation.DrawableRes
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
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
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.fastboot.domain.model.RebootMode

private val GRID_GAP = 12.dp
private val CELL_PADDING = 8.dp
private val ICON_SIZE = 36.dp
private val ICON_LABEL_SPACING = 8.dp
private val DIALOG_MIN_WIDTH = 280.dp

private sealed interface FastbootRebootOptionIcon {
    data class Vector(val imageVector: ImageVector) : FastbootRebootOptionIcon
    data class Drawable(@param:DrawableRes val id: Int) : FastbootRebootOptionIcon
}

private data class FastbootRebootOptionItem(
    val nameRes: Int,
    val icon: FastbootRebootOptionIcon,
    val containerColor: Color,
    val contentColor: Color,
    val mode: RebootMode
)

@Composable
fun FastbootRebootOptionsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onReboot: (RebootMode) -> Unit
) {
    DialogContainer(onDismiss = onDismiss) {
        FastbootRebootOptionsContent(
            modifier = modifier,
            onDismiss = onDismiss,
            onReboot = onReboot
        )
    }
}

@OptIn(ExperimentalGridApi::class)
@Composable
internal fun FastbootRebootOptionsContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onReboot: (RebootMode) -> Unit = {}
) {
    val options = listOf(
        FastbootRebootOptionItem(
            nameRes = R.string.system,
            icon = FastbootRebootOptionIcon.Vector(Icons.Rounded.RestartAlt),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            mode = RebootMode.NORMAL
        ),
        FastbootRebootOptionItem(
            nameRes = R.string.recovery,
            icon = FastbootRebootOptionIcon.Drawable(R.drawable.ic_restart_recovery),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            mode = RebootMode.RECOVERY
        ),
        FastbootRebootOptionItem(
            nameRes = R.string.bootloader,
            icon = FastbootRebootOptionIcon.Drawable(R.drawable.ic_restart_bootloader),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            mode = RebootMode.BOOTLOADER
        ),
        FastbootRebootOptionItem(
            nameRes = R.string.fastboot,
            icon = FastbootRebootOptionIcon.Drawable(R.drawable.ic_restart_fastboot),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            mode = RebootMode.FASTBOOTD
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
            FastbootRebootOptionSquare(
                option = option,
                onClick = withHaptic {
                    onReboot(option.mode)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun FastbootRebootOptionSquare(
    option: FastbootRebootOptionItem,
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
                is FastbootRebootOptionIcon.Vector -> Icon(
                    imageVector = icon.imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(ICON_SIZE),
                    tint = option.contentColor
                )

                is FastbootRebootOptionIcon.Drawable -> Icon(
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
