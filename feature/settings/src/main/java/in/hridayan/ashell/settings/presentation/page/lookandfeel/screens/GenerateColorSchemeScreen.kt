@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFlexBoxApi::class)

package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.dialog.ApiKeyRequiredDialog
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.theme.domain.model.UserGeneratedColorScheme
import `in`.hridayan.ashell.core.presentation.theme.domain.model.toDomain
import `in`.hridayan.ashell.core.presentation.theme.domain.model.toEntity
import `in`.hridayan.ashell.core.presentation.theme.domain.model.toPayload
import `in`.hridayan.ashell.core.presentation.theme.util.ColorSchemeSerializer
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.components.bottomsheet.ThemePreviewBottomSheet
import `in`.hridayan.ashell.settings.presentation.components.svg.vectors.themePicker
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel.GenerateColorSchemeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

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
                        ThemePokerCardCarousel(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            themes = savedColorSchemes,
                            appliedThemeId = appliedThemeId,
                            onApplyTheme = { theme ->
                                viewModel.applyColorScheme(theme)
                                Log.d("GenerateTheme", "Is theme Dark?: ${theme.isDarkTheme}")
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

    val showApiKeyRequiredDialog by viewModel.showApiKeyRequiredDialog.collectAsState()
    if (showApiKeyRequiredDialog) {
        ApiKeyRequiredDialog(
            onDismiss = { viewModel.dismissApiKeyRequiredDialog() },
            onConfirm = {
                viewModel.dismissApiKeyRequiredDialog()
                navController.navigate(NavRoutes.CloudModelsScreen)
            }
        )
    }
}

@Composable
fun ThemePokerCardCarousel(
    modifier: Modifier = Modifier,
    cardHeight: Dp = 320.dp,
    themes: List<UserGeneratedColorScheme>,
    appliedThemeId: Int,
    onApplyTheme: (UserGeneratedColorScheme) -> Unit,
    onDelete: (UserGeneratedColorScheme) -> Unit,
    onEdit: (UserGeneratedColorScheme) -> Unit,
    onShare: (UserGeneratedColorScheme) -> Unit
) {
    val initialPage = remember(themes, appliedThemeId) {
        val index = themes.indexOfFirst { it.id == appliedThemeId }
        if (index >= 0) index else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { themes.size }
    )

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 64.dp), // Fan out effect
        pageSpacing = (-40).dp
    ) { page ->
        val theme = themes[page]

        fun parseHex(hex: String): Color {
            return try {
                val hexStr = if (hex.startsWith("#")) hex else "#$hex"
                Color(hexStr.toColorInt())
            } catch (e: Exception) {
                Color.Gray
            }
        }

        val primary = parseHex(theme.primary)
        val secondary = parseHex(theme.secondary)
        val tertiary = parseHex(theme.tertiary)
        val surface = parseHex(theme.surface)
        val onSurface = parseHex(theme.onSurface)

        CustomCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .graphicsLayer {
                    val pageOffset =
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val absOffset = pageOffset.absoluteValue

                    // Poker card fan math
                    val scale = 1f - (absOffset * 0.15f)
                    scaleX = scale
                    scaleY = scale
                    rotationZ = -pageOffset * 15f
                    translationY = absOffset * 40.dp.toPx()
                },
            onClick = withHaptic { onApplyTheme(theme) },
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            border = BorderStroke(
                width = 2.dp,
                color = primary
            ),
            colors = CardDefaults.cardColors(containerColor = surface)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Canvas for AI-generated SVG Art
                val rawPathData = theme.svgPathData
                val pathData = if (rawPathData.contains("<")) {
                    val dMatch = Regex("""d\s*=\s*['"]([^'"]*)['"]""").find(rawPathData)
                    dMatch?.groupValues?.get(1) ?: rawPathData
                } else {
                    rawPathData
                }.trim()

                if (pathData.isNotBlank()) {
                    val path = remember(pathData) {
                        try {
                            Log.d("GenerateTheme", "Parsing SVG Path: ${pathData.take(50)}...")
                            PathParser().parsePathString(pathData).toPath()
                        } catch (e: Exception) {
                            Log.e("GenerateTheme", "Failed to parse SVG: ${e.message}")
                            null
                        }
                    }

                    if (path != null) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        ) {
                            val bounds = path.getBounds()
                            if (bounds.width > 0f && bounds.height > 0f) {
                                val scaleFactor = minOf(
                                    size.width / bounds.width,
                                    size.height / bounds.height
                                ) * 0.8f
                                val dx =
                                    (size.width - bounds.width * scaleFactor) / 2f - bounds.left * scaleFactor
                                val dy =
                                    (size.height - bounds.height * scaleFactor) / 2f - bounds.top * scaleFactor

                                Log.d(
                                    "GenerateTheme",
                                    "Drawing SVG: size=$size, bounds=$bounds, scale=$scaleFactor, dx=$dx, dy=$dy"
                                )

                                withTransform({
                                    translate(left = dx, top = dy)
                                    scale(
                                        scaleX = scaleFactor,
                                        scaleY = scaleFactor,
                                        pivot = Offset.Zero
                                    )
                                }) {
                                    drawPath(
                                        path = path,
                                        color = onSurface.copy(alpha = 0.1f), // high contrast for visibility testing
                                        style = Fill
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Log.d("GenerateTheme", "pathdata is blank")
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = theme.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primary)
                        )
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(secondary)
                        )
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tertiary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = withHaptic { onShare(theme) }) {
                            Icon(
                                Icons.Rounded.Share,
                                contentDescription = stringResource(id = R.string.share),
                                tint = primary
                            )
                        }
                        IconButton(onClick = withHaptic { onEdit(theme) }) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = stringResource(id = R.string.edit),
                                tint = secondary
                            )
                        }
                        IconButton(onClick = withHaptic { onDelete(theme) }) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = stringResource(id = R.string.delete),
                                tint = tertiary
                            )
                        }
                    }
                }
            }
        }
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

        OutlinedTextField(
            value = prompt,
            onValueChange = { if (it.length <= 1096) prompt = it },
            placeholder = { Text(stringResource(id = R.string.ai_theme_prompt_hint)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
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

        Spacer(modifier = Modifier.height(16.dp))

        if (isGenerating) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                Text(
                    text = generationMessage,
                    fontStyle = FontStyle.Italic
                )
            }
        } else {
            FlexBox(
                modifier = Modifier.fillMaxWidth(),
                config = {
                    direction(FlexDirection.Row)
                    gap(15.dp)
                    alignItems(FlexAlignItems.Stretch)
                }) {
                Button(
                    modifier = Modifier.flex { grow(1f) },
                    onClick = withHaptic { onGenerate(prompt) },
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