package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexDirection
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.dialog.AiAccessPromptHost
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.theme.AshellYouAnimationSpecs
import `in`.hridayan.ashell.core.presentation.theme.domain.model.UserGeneratedColorScheme
import `in`.hridayan.ashell.core.presentation.theme.domain.model.toDomain
import `in`.hridayan.ashell.core.presentation.theme.domain.model.toEntity
import `in`.hridayan.ashell.core.presentation.theme.domain.model.toPayload
import `in`.hridayan.ashell.core.presentation.theme.util.ColorSchemeSerializer
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.components.animatedcomposable.AiGenerationAnimationBox
import `in`.hridayan.ashell.settings.presentation.components.bottomsheet.ThemePreviewBottomSheet
import `in`.hridayan.ashell.settings.presentation.components.dialog.DeleteColorSchemeDialog
import `in`.hridayan.ashell.settings.presentation.components.svg.vectors.themePicker
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel.GenerateColorSchemeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin


@Composable
fun GenerateColorSchemeScreen(
    modifier: Modifier = Modifier,
    viewModel: GenerateColorSchemeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val savedColorSchemes by viewModel.savedColorSchemes.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationMessage by viewModel.generationProgressMessage.collectAsState()
    val previewPayload by viewModel.previewPayload.collectAsState()
    val appliedThemeId by viewModel.appliedThemeId.collectAsState()
    val generationError by viewModel.generationError.collectAsState()

    val topAppBarState = rememberTopAppBarState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var schemeToExport by remember { mutableStateOf<UserGeneratedColorScheme?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { destinationUri ->
            schemeToExport?.let { theme ->
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val serialized = ColorSchemeSerializer.serialize(theme.toEntity())
                        val out = context.contentResolver.openOutputStream(destinationUri)
                        out?.use { stream ->
                            stream.write(serialized.toByteArray(Charsets.UTF_8))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        schemeToExport = null
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { destinationUri ->
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(destinationUri)?.use { stream ->
                        val bytes = stream.readBytes()
                        val content = String(bytes, Charsets.UTF_8)
                        val entity = ColorSchemeSerializer.deserialize(content)
                        viewModel.setPreviewPayload(entity.toDomain().toPayload())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    AppScaffold(
        modifier = modifier.fillMaxSize(),
        onNavigateBack = { navController.navigateBack() },
        topAppBarState = topAppBarState,
        listState = listState,
        topBarTitle = stringResource(id = R.string.generate_color_scheme),
        content = { innerPadding: PaddingValues, topBarScrollBehavior: TopAppBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                    .imePadding(),
                state = listState,
                contentPadding = innerPadding
            ) {
                item {
                    if (savedColorSchemes.isNotEmpty()) {
                        ThemeMaterialCarousel(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            themes = savedColorSchemes,
                            appliedThemeId = appliedThemeId,
                            onApplyTheme = { theme ->
                                viewModel.applyColorScheme(theme)
                            },
                            onDelete = { theme -> viewModel.deleteTheme(theme) },
                            onEdit = { theme ->
                                navController.navigate(
                                    NavRoutes.EditColorSchemeScreen(
                                        theme.id
                                    )
                                )
                            },
                            onShare = { theme ->
                                schemeToExport = theme
                                exportLauncher.launch(
                                    "${
                                        theme.name.replace(
                                            " ",
                                            "_"
                                        )
                                    }.ashellyoucolorscheme"
                                )
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                modifier = Modifier.padding(
                                    horizontal = 40.dp,
                                    vertical = 20.dp
                                ),
                                imageVector = DynamicColorImageVectors.themePicker(),
                                contentDescription = null
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    CreateWithAiSection(
                        isGenerating = isGenerating,
                        generationMessage = generationMessage,
                        onGenerate = { prompt -> viewModel.generateColorScheme(prompt) },
                        onImport = {
                            importLauncher.launch(
                                arrayOf("application/octet-stream", "*/*")
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    )

    if (previewPayload != null) {
        ThemePreviewBottomSheet(
            payload = previewPayload!!,
            onDismiss = { viewModel.clearPreview() },
            onSave = { entity -> viewModel.saveColorScheme(entity.toDomain()) }
        )
    }

    val aiAccessPrompt by viewModel.aiAccessPrompt.collectAsState()
    AiAccessPromptHost(
        status = aiAccessPrompt,
        onDismiss = viewModel::dismissAiAccessPrompt,
        onAddApiKey = {
            viewModel.dismissAiAccessPrompt()
            navController.navigate(NavRoutes.CloudModelsScreen)
        }
    )

    generationError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissGenerationError() },
            icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null) },
            title = { Text(stringResource(id = R.string.error)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = withHaptic { viewModel.dismissGenerationError() }) {
                    Text(stringResource(id = android.R.string.ok))
                }
            }
        )
    }
}

private const val CARD_DEFAULT_HEIGHT = 300
private const val HEADER_HEIGHT = 64
private const val HEADER_CURVE_DEPTH = 18
private const val WAVE_PHASE_MODULO = 7
private const val WAVE_PHASE_MULTIPLIER = 0.89f
private const val WAVE_ANIMATION_DURATION_MILLIS = 14000
private const val FULL_CYCLE_RADIANS = 6.2831855f
private const val TANGENT_BOUNDARY_DIVISOR = 3f
private const val TANGENT_SMOOTH_DIVISOR = 6f
private const val WAVE_SEPARATOR_WIDTH_DP = 1.5f
private const val WAVE_SEPARATOR_ALPHA = 0.55f
private const val WAVE1_ALPHA = 0.92f
private const val WAVE2_ALPHA = 0.90f
private const val WAVE3_ALPHA = 0.90f
private const val WAVE1_Y_BASE = 0.44f
private const val WAVE1_AMPLITUDE = 0.052f
private const val WAVE2_Y_BASE = 0.60f
private const val WAVE2_AMPLITUDE = 0.044f
private const val WAVE3_Y_BASE = 0.75f
private const val WAVE3_AMPLITUDE = 0.038f

private val WAVE_X_RATIOS = floatArrayOf(0f, 0.26f, 0.53f, 0.79f, 1f)
private val WAVE1_POINT_AMPLITUDES = floatArrayOf(0.75f, 1.15f, 0.85f, 1.10f, 0.80f)
private val WAVE1_POINT_PHASES = floatArrayOf(0.0f, 1.3f, 2.8f, 4.2f, 5.6f)
private val WAVE2_POINT_AMPLITUDES = floatArrayOf(0.90f, 0.80f, 1.20f, 0.85f, 0.95f)
private val WAVE2_POINT_PHASES = floatArrayOf(2.1f, 3.4f, 4.9f, 0.3f, 1.7f)
private val WAVE3_POINT_AMPLITUDES = floatArrayOf(0.80f, 1.10f, 0.75f, 1.15f, 0.85f)
private val WAVE3_POINT_PHASES = floatArrayOf(4.2f, 5.5f, 0.9f, 2.3f, 3.7f)

private fun parseHex(hex: String): Color = try {
    val normalized = if (hex.startsWith("#")) hex else "#$hex"
    Color(normalized.toColorInt())
} catch (e: Exception) {
    Color.Gray
}

@Composable
fun ThemeMaterialCarousel(
    modifier: Modifier = Modifier,
    cardHeight: Dp = CARD_DEFAULT_HEIGHT.dp,
    themes: List<UserGeneratedColorScheme>,
    appliedThemeId: Int,
    onApplyTheme: (UserGeneratedColorScheme) -> Unit,
    onDelete: (UserGeneratedColorScheme) -> Unit,
    onEdit: (UserGeneratedColorScheme) -> Unit,
    onShare: (UserGeneratedColorScheme) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveAnimation")
    val waveAnimationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_CYCLE_RADIANS,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = WAVE_ANIMATION_DURATION_MILLIS,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhaseAnimation"
    )

    var searchQuery by remember { mutableStateOf("") }
    val filteredThemes = remember(themes, searchQuery) {
        if (searchQuery.isBlank()) themes
        else themes.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val carouselState = rememberCarouselState { filteredThemes.size }
    val animationScope = rememberCoroutineScope()
    var initialScrollDone by rememberSaveable { mutableStateOf(false) }
    val isDynamicColor = LocalSettings.current[SettingsKeys.DynamicColors]
    val isUserGeneratedColorSchemeApplied =
        LocalSettings.current[SettingsKeys.UserGeneratedColorSchemeApplied]

    LaunchedEffect(filteredThemes) {
        if (!initialScrollDone && filteredThemes.isNotEmpty()) {
            val idx = filteredThemes.indexOfFirst { it.id == appliedThemeId }
            if (idx >= 0) carouselState.scrollToItem(idx)
            initialScrollDone = true
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ThemeSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        if (filteredThemes.isEmpty()) {
            Text(
                text = stringResource(R.string.no_results_found),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 32.dp)
            )
        } else {
            HorizontalCenteredHeroCarousel(
                state = carouselState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                itemSpacing = 8.dp,
                contentPadding = PaddingValues(8.dp),
            ) { index ->
                val theme = filteredThemes[index]
                ThemeCarouselItem(
                    theme = theme,
                    isCurrentItem = carouselState.currentItem == index,
                    isApplied = theme.id == appliedThemeId && !isDynamicColor && isUserGeneratedColorSchemeApplied,
                    cardHeight = cardHeight,
                    animatedPhase = waveAnimationPhase,
                    onClick = withHaptic {
                        onApplyTheme(theme)
                        if (carouselState.currentItem != index) {
                            animationScope.launch { carouselState.animateScrollToItem(index) }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            filteredThemes.getOrNull(carouselState.currentItem)?.let { currentTheme ->
                ThemeActionPill(
                    theme = currentTheme,
                    onShare = onShare,
                    onEdit = onEdit,
                    onDeleteRequested = { showDeleteDialog = true }
                )

                if (showDeleteDialog) {
                    DeleteColorSchemeDialog(
                        themeName = currentTheme.name,
                        onConfirm = {
                            onDelete(currentTheme)
                            showDeleteDialog = false
                        },
                        onDismiss = { showDeleteDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    CustomSearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
        value = query,
        onValueChange = onQueryChange,
        hint = stringResource(R.string.search),
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = withHaptic { onQueryChange("") }) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = null)
                }
            }
        }
    )
}

@Composable
private fun CarouselItemScope.ThemeCarouselItem(
    theme: UserGeneratedColorScheme,
    isCurrentItem: Boolean,
    isApplied: Boolean,
    cardHeight: Dp,
    animatedPhase: Float,
    onClick: () -> Unit
) {
    val staticPhase = (theme.id % WAVE_PHASE_MODULO) * WAVE_PHASE_MULTIPLIER
    val totalPhase = staticPhase + animatedPhase
    val cardShape = RoundedCornerShape(24.dp)

    ThemeCard(
        theme = theme,
        modifier = Modifier.maskClip(cardShape),
        isCurrentItem = isCurrentItem,
        isApplied = isApplied,
        cardHeight = cardHeight,
        phase = totalPhase,
        onClick = onClick
    )
}

@Composable
fun ThemeCard(
    theme: UserGeneratedColorScheme,
    modifier: Modifier = Modifier,
    isCurrentItem: Boolean = true,
    isApplied: Boolean = false,
    cardHeight: Dp = CARD_DEFAULT_HEIGHT.dp,
    phase: Float = 0f,
    onClick: (() -> Unit)? = null
) {
    val primary = parseHex(theme.primary)
    val onPrimary = parseHex(theme.onPrimary)
    val secondary = parseHex(theme.secondary)
    val onSecondary = parseHex(theme.onSecondary)
    val tertiary = parseHex(theme.tertiary)
    val surface = parseHex(theme.surface)
    val secondaryContainer = parseHex(theme.secondaryContainer)
    val onSecondaryContainer = parseHex(theme.onSecondaryContainer)

    val cardShape = RoundedCornerShape(24.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()

    val clickModifier = if (onClick != null) {
        Modifier
            .indication(interactionSource, ripple())
            .pointerInput(onClick) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val press = PressInteraction.Press(down.position)
                    coroutineScope.launch { interactionSource.emit(press) }
                    val up = waitForUpOrCancellation()
                    if (up != null) {
                        up.consume()
                        coroutineScope.launch {
                            interactionSource.emit(PressInteraction.Release(press))
                        }
                        onClick()
                    } else {
                        coroutineScope.launch { interactionSource.emit(PressInteraction.Cancel(press)) }
                    }
                }
            }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .height(cardHeight)
            .clip(cardShape)
            .then(modifier)
            .background(surface)
            .then(clickModifier)
    ) {
        WavePattern(
            modifier = Modifier.fillMaxSize(),
            phase = phase,
            primary = primary,
            onPrimary = onPrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            tertiary = tertiary
        )
        ThemeCardHeader(
            name = theme.name,
            isCurrentItem = isCurrentItem,
            isApplied = isApplied,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer
        )
    }
}

@Composable
private fun ThemeCardHeader(
    name: String,
    isCurrentItem: Boolean,
    isApplied: Boolean,
    secondaryContainer: Color,
    onSecondaryContainer: Color
) {
    val checkedIconScale by animateFloatAsState(
        targetValue = if (isApplied) 1f else 0f,
        animationSpec = if (isApplied) {
            AshellYouAnimationSpecs.springFloat
        } else {
            tween(
                durationMillis = 300,
                easing = LinearEasing
            )
        },
        label = "Check Scale Animation"
    )

    val headerTextScale by animateFloatAsState(
        targetValue = if (isCurrentItem) 1f else 0f,
        animationSpec = if (isCurrentItem) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        } else {
            tween(
                durationMillis = 300,
                easing = LinearEasing
            )
        },
        label = "Header Text Scale Animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((HEADER_HEIGHT + HEADER_CURVE_DEPTH).dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val baseH = HEADER_HEIGHT.dp.toPx()
            val dip = HEADER_CURVE_DEPTH.dp.toPx()

            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, baseH - dip)
                cubicTo(
                    w * 0.65f, baseH - dip,
                    w * 0.35f, baseH + dip,
                    0f, baseH + dip
                )
                close()
            }
            drawPath(path, color = secondaryContainer)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 14.dp, top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            if (isCurrentItem) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = headerTextScale
                            scaleY = headerTextScale
                        },
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSecondaryContainer,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }

            if (isApplied) {
                Icon(
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            scaleX = checkedIconScale
                            scaleY = checkedIconScale
                        },
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = null,
                    tint = onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun ThemeActionPill(
    theme: UserGeneratedColorScheme,
    onShare: (UserGeneratedColorScheme) -> Unit,
    onEdit: (UserGeneratedColorScheme) -> Unit,
    onDeleteRequested: () -> Unit
) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = withHaptic { onShare(theme) }) {
            Icon(
                imageVector = Icons.Rounded.Share,
                contentDescription = stringResource(id = R.string.share),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        VerticalDivider(
            modifier = Modifier.height(24.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        IconButton(onClick = withHaptic { onEdit(theme) }) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = stringResource(id = R.string.edit),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        VerticalDivider(
            modifier = Modifier.height(24.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        IconButton(onClick = withHaptic { onDeleteRequested() }) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = stringResource(id = R.string.delete),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun WavePattern(
    modifier: Modifier = Modifier,
    phase: Float,
    primary: Color,
    onPrimary: Color,
    secondary: Color,
    onSecondary: Color,
    tertiary: Color
) {
    val wavePath = remember { Path() }
    val separatorPath = remember { Path() }
    val yRatios = remember { FloatArray(WAVE_X_RATIOS.size) }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        computeWaveYRatios(
            targetYRatios = yRatios,
            baseY = WAVE1_Y_BASE,
            amplitude = WAVE1_AMPLITUDE,
            phase = phase,
            pointAmps = WAVE1_POINT_AMPLITUDES,
            pointPhases = WAVE1_POINT_PHASES
        )
        buildSmoothWavePath(
            path = wavePath,
            xRatios = WAVE_X_RATIOS,
            yRatios = yRatios,
            width = w,
            height = h,
            isClosed = true
        )
        drawPath(wavePath, color = primary.copy(alpha = WAVE1_ALPHA))

        computeWaveYRatios(
            targetYRatios = yRatios,
            baseY = WAVE2_Y_BASE,
            amplitude = WAVE2_AMPLITUDE,
            phase = phase,
            pointAmps = WAVE2_POINT_AMPLITUDES,
            pointPhases = WAVE2_POINT_PHASES
        )
        buildSmoothWavePath(
            path = wavePath,
            xRatios = WAVE_X_RATIOS,
            yRatios = yRatios,
            width = w,
            height = h,
            isClosed = true
        )
        drawPath(wavePath, color = secondary.copy(alpha = WAVE2_ALPHA))

        buildSmoothWavePath(
            path = separatorPath,
            xRatios = WAVE_X_RATIOS,
            yRatios = yRatios,
            width = w,
            height = h,
            isClosed = false
        )
        drawPath(
            separatorPath,
            color = onPrimary.copy(alpha = WAVE_SEPARATOR_ALPHA),
            style = Stroke(width = WAVE_SEPARATOR_WIDTH_DP.dp.toPx())
        )

        computeWaveYRatios(
            targetYRatios = yRatios,
            baseY = WAVE3_Y_BASE,
            amplitude = WAVE3_AMPLITUDE,
            phase = phase,
            pointAmps = WAVE3_POINT_AMPLITUDES,
            pointPhases = WAVE3_POINT_PHASES
        )
        buildSmoothWavePath(
            path = wavePath,
            xRatios = WAVE_X_RATIOS,
            yRatios = yRatios,
            width = w,
            height = h,
            isClosed = true
        )
        drawPath(wavePath, color = tertiary.copy(alpha = WAVE3_ALPHA))

        buildSmoothWavePath(
            path = separatorPath,
            xRatios = WAVE_X_RATIOS,
            yRatios = yRatios,
            width = w,
            height = h,
            isClosed = false
        )
        drawPath(
            separatorPath,
            color = onSecondary.copy(alpha = WAVE_SEPARATOR_ALPHA),
            style = Stroke(width = WAVE_SEPARATOR_WIDTH_DP.dp.toPx())
        )
    }
}

private fun computeWaveYRatios(
    targetYRatios: FloatArray,
    baseY: Float,
    amplitude: Float,
    phase: Float,
    pointAmps: FloatArray,
    pointPhases: FloatArray
) {
    for (i in targetYRatios.indices) {
        val angle = phase + pointPhases[i]
        targetYRatios[i] = baseY + amplitude * pointAmps[i] * sin(angle)
    }
}

private fun buildSmoothWavePath(
    path: Path,
    xRatios: FloatArray,
    yRatios: FloatArray,
    width: Float,
    height: Float,
    isClosed: Boolean
) {
    val pointCount = xRatios.size
    val firstX = xRatios[0] * width
    val firstY = yRatios[0] * height
    path.reset()
    path.moveTo(firstX, firstY)

    for (i in 0 until pointCount - 1) {
        val p0X = xRatios[i] * width
        val p0Y = yRatios[i] * height
        val p1X = xRatios[i + 1] * width
        val p1Y = yRatios[i + 1] * height

        val cp1X: Float
        val cp1Y: Float
        if (i == 0) {
            val deltaX = (p1X - p0X) / TANGENT_BOUNDARY_DIVISOR
            cp1X = p0X + deltaX
            cp1Y = p0Y
        } else {
            val prevX = xRatios[i - 1] * width
            val prevY = yRatios[i - 1] * height
            cp1X = p0X + (p1X - prevX) / TANGENT_SMOOTH_DIVISOR
            cp1Y = p0Y + (p1Y - prevY) / TANGENT_SMOOTH_DIVISOR
        }

        val cp2X: Float
        val cp2Y: Float
        if (i == pointCount - 2) {
            val deltaX = (p1X - p0X) / TANGENT_BOUNDARY_DIVISOR
            cp2X = p1X - deltaX
            cp2Y = p1Y
        } else {
            val nextX = xRatios[i + 2] * width
            val nextY = yRatios[i + 2] * height
            cp2X = p1X - (nextX - p0X) / TANGENT_SMOOTH_DIVISOR
            cp2Y = p1Y - (nextY - p0Y) / TANGENT_SMOOTH_DIVISOR
        }

        path.cubicTo(cp1X, cp1Y, cp2X, cp2Y, p1X, p1Y)
    }

    if (isClosed) {
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()
    }
}

@Composable
fun CreateWithAiSection(
    isGenerating: Boolean,
    generationMessage: String,
    onGenerate: (String) -> Unit,
    onImport: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.create_with_ai),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AnimatedContent(
            targetState = isGenerating,
            label = "AI Generation State",
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            }
        ) { generating ->
            if (generating) {
                AiGenerationAnimationBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    message = generationMessage
                )
            } else {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { if (it.length <= 1096) prompt = it },
                    placeholder = { Text(stringResource(id = R.string.ai_theme_prompt_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isGenerating,
                    supportingText = {
                        Text(
                            text = "${prompt.length}/1096",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            color = if (prompt.length == 1096) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isGenerating) {
            FlexBox(
                modifier = Modifier.fillMaxWidth(),
                config = {
                    direction(FlexDirection.Row)
                    gap(15.dp)
                    alignItems(FlexAlignItems.Stretch)
                }
            ) {
                Button(
                    modifier = Modifier.flex { grow(1f) },
                    onClick = withHaptic {
                        onGenerate(prompt)
                        prompt = ""
                    },
                    enabled = prompt.isNotBlank(),
                    shapes = ButtonDefaults.shapes()
                ) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.generate_scheme))
                }

                OutlinedButton(
                    modifier = Modifier.flex { grow(1f) },
                    onClick = withHaptic { onImport() },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_download),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string._import))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeCardPreview() {
    val sampleTheme = remember {
        UserGeneratedColorScheme(
            id = 1,
            name = "Ocean Wave",
            primary = "#2B5C8F",
            onPrimary = "#FFFFFF",
            primaryContainer = "#D4E3FF",
            onPrimaryContainer = "#001C38",
            inversePrimary = "#A5C8FF",
            secondary = "#535F70",
            onSecondary = "#FFFFFF",
            secondaryContainer = "#D7E3F7",
            onSecondaryContainer = "#101C2B",
            tertiary = "#6B5778",
            onTertiary = "#FFFFFF",
            tertiaryContainer = "#F2DAFF",
            onTertiaryContainer = "#251432",
            error = "#BA1A1A",
            onError = "#FFFFFF",
            errorContainer = "#FFDAD6",
            onErrorContainer = "#410002",
            background = "#FDFCFF",
            onBackground = "#1A1C1E",
            surface = "#FDFCFF",
            onSurface = "#1A1C1E",
            surfaceVariant = "#DFE2EB",
            onSurfaceVariant = "#43474E",
            surfaceTint = "#2B5C8F",
            inverseSurface = "#2F3033",
            inverseOnSurface = "#F1F0F4",
            surfaceBright = "#FDFCFF",
            surfaceDim = "#DAD9DD",
            surfaceContainer = "#EEEDF1",
            surfaceContainerHigh = "#E8E7EB",
            surfaceContainerHighest = "#E2E2E6",
            surfaceContainerLow = "#F4F3F7",
            surfaceContainerLowest = "#FFFFFF",
            outline = "#73777F",
            outlineVariant = "#C3C7D0",
            scrim = "#000000"
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "PreviewWaveAnimation")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_CYCLE_RADIANS,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = WAVE_ANIMATION_DURATION_MILLIS,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "PreviewWavePhase"
    )

    Box(
        modifier = Modifier
            .padding(16.dp)
            .width(260.dp)
    ) {
        ThemeCard(
            theme = sampleTheme,
            isCurrentItem = true,
            isApplied = true,
            phase = phase
        )
    }
}
