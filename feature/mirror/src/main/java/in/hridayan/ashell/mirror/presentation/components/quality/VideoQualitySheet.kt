package `in`.hridayan.ashell.mirror.presentation.components.quality

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.card.CustomCardDefaults
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.switch.AppSwitch
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.mirror.domain.model.MirrorQualityPreset
import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.quality.BitrateChoice
import `in`.hridayan.ashell.mirror.domain.quality.CustomQuality
import `in`.hridayan.ashell.mirror.domain.quality.FrameRateChoice
import `in`.hridayan.ashell.mirror.domain.quality.QualityMode
import `in`.hridayan.ashell.mirror.domain.quality.QualitySelection
import `in`.hridayan.ashell.mirror.domain.quality.ResolutionChoice
import `in`.hridayan.ashell.mirror.domain.quality.ResolvedQuality
import `in`.hridayan.ashell.mirror.presentation.model.QualityUiState
import kotlinx.coroutines.launch

private val SheetPadding = 24.dp
private val SectionGap = 20.dp
private val SummaryPadding = 16.dp
private val SummaryGap = 4.dp
private val SwitchCardCorner = 24.dp
private val SwitchCardPadding = 17.dp
private val SwitchTextGap = 7.dp
private const val DESCRIPTION_ALPHA = 0.7f

private val RESOLUTIONS = listOf(ResolutionChoice.Auto, ResolutionChoice.Native) +
    listOf(2560, 1920, 1600, 1280, 1024, 800).map(ResolutionChoice::LongEdge)
private val FRAME_RATES =
    listOf(FrameRateChoice.Auto) + listOf(30, 60, 90, 120).map(FrameRateChoice::Fixed)
private val BITRATES =
    listOf(BitrateChoice.Auto) + listOf(2, 4, 8, 16, 24, 40).map(BitrateChoice::Fixed)

/**
 * Chooses the video quality for the transport in use. Choices are a draft until Apply, because
 * applying restarts the stream; the summary at the top shows what the draft would stream at on this
 * device. The statistics switch applies at once, since it needs no restart.
 *
 * @param preview what a selection would stream at, or null before the first session starts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoQualitySheet(
    state: QualityUiState,
    preview: (QualitySelection) -> ResolvedQuality?,
    onApply: (QualitySelection) -> Unit,
    onStatsOverlayChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val scope = rememberCoroutineScope()
    var draft by remember(state.selection) { mutableStateOf(state.selection) }

    val close: () -> Unit =
        { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SheetPadding)
                .padding(bottom = SheetPadding),
            verticalArrangement = Arrangement.spacedBy(SectionGap)
        ) {
            Text(
                text = stringResource(R.string.video_quality),
                style = MaterialTheme.typography.titleLarge
            )
            preview(draft)?.let { QualitySummary(it) }
            ModeChoice(draft.mode) { draft = draft.copy(mode = it) }
            if (draft.mode == QualityMode.CUSTOM) {
                CustomChoices(draft.custom, state) { draft = draft.copy(custom = it) }
            }
            StatsOverlaySwitch(state.isStatsOverlayOn, onStatsOverlayChange)
            SheetButtons(
                isChanged = draft != state.selection,
                onCancel = close,
                onApply = {
                    onApply(draft)
                    close()
                }
            )
        }
    }
}

@Composable
private fun QualitySummary(resolved: ResolvedQuality) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SummaryPadding),
            verticalArrangement = Arrangement.spacedBy(SummaryGap)
        ) {
            Text(text = qualitySummary(resolved), style = MaterialTheme.typography.titleSmall)
            resolved.limit.explanationRes()?.let {
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModeChoice(mode: QualityMode, onSelect: (QualityMode) -> Unit) {
    ChoiceChips(
        title = stringResource(R.string.quality),
        options = QualityMode.entries,
        selected = mode,
        label = { stringResource(it.labelRes()) },
        onSelect = onSelect,
        footnote = modeDescription(mode)
    )
}

@Composable
private fun modeDescription(mode: QualityMode): String = when (mode) {
    QualityMode.AUTO -> stringResource(R.string.quality_auto_description)
    QualityMode.SAVER -> presetDescription(MirrorQualityPreset.SAVER)
    QualityMode.BALANCED -> presetDescription(MirrorQualityPreset.BALANCED)
    QualityMode.SHARP -> stringResource(
        R.string.quality_sharp_description,
        MirrorQualityPreset.SHARP.maxFps
    )

    QualityMode.CUSTOM -> stringResource(R.string.quality_custom_description)
}

@Composable
private fun presetDescription(preset: MirrorQualityPreset): String =
    stringResource(R.string.quality_preset_description, preset.maxSize, preset.maxFps)

private fun QualityMode.labelRes(): Int = when (this) {
    QualityMode.AUTO -> R.string.auto
    QualityMode.SAVER -> R.string.saver
    QualityMode.BALANCED -> R.string.balanced
    QualityMode.SHARP -> R.string.sharp
    QualityMode.CUSTOM -> R.string.quality_custom
}

@Composable
private fun CustomChoices(
    custom: CustomQuality,
    state: QualityUiState,
    onChange: (CustomQuality) -> Unit
) {
    val auto = stringResource(R.string.auto)
    ChoiceChips(
        title = stringResource(R.string.resolution),
        options = RESOLUTIONS,
        selected = custom.resolution,
        label = { it.label(auto) },
        onSelect = { onChange(custom.copy(resolution = it)) }
    )
    ChoiceChips(
        title = stringResource(R.string.frame_rate),
        options = FRAME_RATES,
        selected = custom.frameRate,
        label = { it.label(auto) },
        onSelect = { onChange(custom.copy(frameRate = it)) },
        footnote = frameRateFootnote(custom.frameRate, state.viewerRefreshRate)
    )
    ChoiceChips(
        title = stringResource(R.string.bitrate),
        options = BITRATES,
        selected = custom.bitrate,
        label = { it.label(auto) },
        onSelect = { onChange(custom.copy(bitrate = it)) }
    )
    ChoiceChips(
        title = stringResource(R.string.codec),
        options = VideoCodec.entries.filter { it in state.supportedCodecs },
        selected = custom.codec,
        label = { it.displayName },
        onSelect = { onChange(custom.copy(codec = it)) }
    )
}

@Composable
private fun ResolutionChoice.label(auto: String): String = when (this) {
    ResolutionChoice.Auto -> auto
    ResolutionChoice.Native -> stringResource(R.string.native_resolution)
    is ResolutionChoice.LongEdge -> stringResource(R.string.long_edge_pixels, pixels)
}

@Composable
private fun FrameRateChoice.label(auto: String): String = when (this) {
    FrameRateChoice.Auto -> auto
    is FrameRateChoice.Fixed -> stringResource(R.string.frames_per_second, fps)
}

@Composable
private fun BitrateChoice.label(auto: String): String = when (this) {
    BitrateChoice.Auto -> auto
    is BitrateChoice.Fixed -> stringResource(R.string.megabits_per_second, megabits.toString())
}

/** A rate above this phone's refresh rate stays selectable, for displays that misreport theirs. */
@Composable
private fun frameRateFootnote(frameRate: FrameRateChoice, viewerRefreshRate: Int?): String? {
    val fixed = (frameRate as? FrameRateChoice.Fixed)?.fps ?: return null
    val viewer = viewerRefreshRate ?: return null
    return if (fixed > viewer) stringResource(R.string.viewer_max_frame_rate, viewer) else null
}

/** Laid out like a switch item on the settings screens, so the same setting looks the same here. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatsOverlaySwitch(isOn: Boolean, onChange: (Boolean) -> Unit) {
    val toggle = withHaptic(HapticFeedbackType.ToggleOn) { onChange(!isOn) }
    CustomCard(shape = CustomCardDefaults.shape(all = SwitchCardCorner), onClick = toggle) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SwitchCardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SwitchCardPadding)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SwitchTextGap)
            ) {
                Text(
                    text = stringResource(R.string.show_stream_statistics),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
                Text(
                    text = stringResource(R.string.des_show_stream_statistics),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(DESCRIPTION_ALPHA)
                )
            }
            AppSwitch(checked = isOn, onCheckedChange = { toggle() })
        }
    }
}

@Composable
private fun SheetButtons(isChanged: Boolean, onCancel: () -> Unit, onApply: () -> Unit) {
    OverflowButtonGroup(
        items = listOf(
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                text = stringResource(R.string.cancel),
                onClick = onCancel
            ),
            ButtonGroupItem(
                text = stringResource(R.string.apply),
                onClick = onApply,
                enabled = isChanged
            )
        )
    )
}
