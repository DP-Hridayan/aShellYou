package `in`.hridayan.ashell.portforward.presentation.components.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.domain.model.RecentHost
import `in`.hridayan.ashell.portforward.domain.usecase.ValidateTunnelFormUseCase.FieldError
import `in`.hridayan.ashell.portforward.presentation.model.LocalPortStatus

private const val HOST_FIELD_WEIGHT = 2f
private const val PORT_FIELD_WEIGHT = 1f
private const val MAX_PORT_DIGITS = 5
private val ChipGap = 8.dp
private val StatusEndPadding = 12.dp
private val StatusGap = 4.dp
private val StatusIconSize = 18.dp
private val StatusProgressStroke = 2.dp

/** The field that takes a pasted URL or a typed address and fills the fields below it. */
@Composable
fun AddressField(value: String, onValueChange: (String) -> Unit, onPaste: () -> Unit) {
    EditorTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.port_forward_paste_address),
        position = GroupPosition.Single,
        trailingIcon = {
            IconButton(onClick = withHaptic { onPaste() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_paste),
                    contentDescription = stringResource(R.string.paste)
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next)
    )
}

/**
 * Hosts used before, as a row that scrolls rather than wraps, so a long history never pushes the
 * form down.
 */
@Composable
fun RecentHostChips(hosts: List<RecentHost>, onSelect: (RecentHost) -> Unit) {
    if (hosts.isEmpty()) return
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = EditorSpacing.BlockGap),
        contentPadding = PaddingValues(horizontal = EditorSpacing.SectionLabelInset),
        horizontalArrangement = Arrangement.spacedBy(ChipGap)
    ) {
        items(hosts, key = { it.host }) { host ->
            SuggestionChip(onClick = withHaptic { onSelect(host) }, label = { Text(host.host) })
        }
    }
}

/** Host and port side by side, joined into one surface. */
@Composable
fun HostAndPortFields(
    host: String,
    port: String,
    hostError: FieldError?,
    portError: FieldError?,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(EditorSpacing.GroupGap)) {
        EditorTextField(
            modifier = Modifier.weight(HOST_FIELD_WEIGHT),
            value = host,
            onValueChange = onHostChange,
            label = stringResource(R.string.host),
            position = GroupPosition.Start,
            isError = hostError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next)
        )
        PortField(
            modifier = Modifier.weight(PORT_FIELD_WEIGHT),
            value = port,
            label = stringResource(R.string.port),
            position = GroupPosition.End,
            isError = portError != null,
            onValueChange = onPortChange
        )
    }
    EditorCaption(text = (hostError ?: portError)?.text(), isError = true)
}

/** The port on this phone, with whether another app holds it shown at its end. */
@Composable
fun LocalPortField(
    value: String,
    status: LocalPortStatus,
    isError: Boolean,
    position: GroupPosition,
    onValueChange: (String) -> Unit
) {
    PortField(
        value = value,
        label = stringResource(R.string.port_forward_local_port),
        position = position,
        isError = isError,
        onValueChange = onValueChange,
        trailingIcon = { PortStatusIndicator(status) }
    )
}

/** A numeric field that accepts at most a five-digit port. */
@Composable
fun PortField(
    value: String,
    label: String,
    position: GroupPosition,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    EditorTextField(
        modifier = modifier,
        value = value,
        onValueChange = { text -> if (text.isPortInput()) onValueChange(text) },
        label = label,
        position = position,
        isError = isError,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
    )
}

@Composable
private fun PortStatusIndicator(status: LocalPortStatus) {
    Row(
        modifier = Modifier.padding(end = StatusEndPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StatusGap)
    ) {
        when (status) {
            LocalPortStatus.Unknown -> Unit
            LocalPortStatus.Checking -> CircularProgressIndicator(
                modifier = Modifier.size(StatusIconSize),
                strokeWidth = StatusProgressStroke
            )

            LocalPortStatus.Free -> StatusLabel(
                R.drawable.ic_check_circle,
                R.string.port_forward_port_free,
                isError = false
            )
            is LocalPortStatus.InUse -> StatusLabel(
                R.drawable.ic_error,
                R.string.port_forward_port_taken,
                isError = true
            )
        }
    }
}

@Composable
private fun StatusLabel(icon: Int, label: Int, isError: Boolean) {
    val color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Icon(
        modifier = Modifier.size(StatusIconSize),
        painter = painterResource(icon),
        contentDescription = null,
        tint = color
    )
    Text(text = stringResource(label), style = MaterialTheme.typography.labelMedium, color = color)
}

/** Offers the next free port when another app holds the chosen one. */
@Composable
fun SuggestedPortChip(port: Int, onUse: (Int) -> Unit) {
    AssistChip(
        modifier = Modifier.padding(start = EditorSpacing.SectionLabelInset, top = EditorSpacing.CaptionTop),
        onClick = withHaptic { onUse(port) },
        label = { Text(stringResource(R.string.port_forward_use_port, port)) }
    )
}

/** The message for a field error, shared by every field of the editor. */
@Composable
fun FieldError.text(): String = when (this) {
    FieldError.Required -> stringResource(R.string.port_forward_field_required)
    FieldError.Invalid -> stringResource(R.string.port_forward_field_invalid)
    FieldError.Privileged -> stringResource(R.string.port_forward_error_port_privileged)
    is FieldError.UsedByTunnel -> stringResource(R.string.port_forward_used_by_tunnel, tunnelName)
}

private fun String.isPortInput(): Boolean = all { it.isDigit() } && length <= MAX_PORT_DIGITS
