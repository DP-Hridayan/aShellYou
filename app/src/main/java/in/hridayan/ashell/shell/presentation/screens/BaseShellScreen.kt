@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.presentation.screens

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.tooltip.TooltipContent
import `in`.hridayan.ashell.navigation.CommandExamplesScreen
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.SettingsScreen
import `in`.hridayan.ashell.shell.domain.model.CommandResult
import `in`.hridayan.ashell.shell.domain.model.ShellState
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel
import kotlinx.coroutines.launch

@Composable
fun BaseShellScreen(
    modifier: Modifier = Modifier,
    shellViewModel: ShellViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val interactionSources = remember { List(5) { MutableInteractionSource() } }
    val navController = LocalNavController.current
    val commandResults by shellViewModel.commandResults.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val command by shellViewModel.command.collectAsState()
    val commandError by shellViewModel.commandError.collectAsState()
    val shellState by shellViewModel.shellState.collectAsState()

    val actionFabIcon: @Composable () -> Unit = {
        when (shellState) {
            is ShellState.Busy -> AnimatedStopIcon()
            is ShellState.Free -> Icon(
                painter = painterResource(R.drawable.ic_help),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            else -> Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }


    val actionFabOnClick: () -> Unit = {
        when (shellState) {
            is ShellState.InputQuery -> {
                coroutineScope.launch {
                    shellViewModel.runCommand()
                }
            }

            is ShellState.Busy -> {
                shellViewModel.stopCommand()
            }

            is ShellState.Free -> {
                navController.navigate(CommandExamplesScreen)
            }
        }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            @Suppress("DEPRECATION")
            ButtonGroup(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, bottom = 25.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        weakHaptic()
                    },
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .animateWidth(interactionSources[0]),
                    interactionSource = interactionSources[0],
                ) {
                    TooltipContent(text = stringResource(R.string.search)) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        weakHaptic()
                    },
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .animateWidth(interactionSources[1]),
                    interactionSource = interactionSources[1],
                ) {
                    TooltipContent(text = stringResource(R.string.bookmarks)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bookmarks),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        weakHaptic()
                    },
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .animateWidth(interactionSources[2]),
                    interactionSource = interactionSources[2],
                ) {
                    TooltipContent(text = stringResource(R.string.history)) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        weakHaptic()
                        shellViewModel.clearOutput()
                    },
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .animateWidth(interactionSources[3]),
                    interactionSource = interactionSources[3],
                ) {
                    TooltipContent(text = stringResource(R.string.clear)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_clear),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        weakHaptic()
                        navController.navigate(SettingsScreen)
                    },
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .animateWidth(interactionSources[4]),
                    interactionSource = interactionSources[4],
                ) {
                    TooltipContent(text = stringResource(R.string.settings)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {},
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.animateContentSize(),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    AutoResizeableText(
                        text = stringResource(R.string.basic_shell),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                val label =
                    if (commandError) stringResource(R.string.field_cannot_be_blank) else stringResource(
                        R.string.command_title
                    )

                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    label = { Text(label) },
                    value = command,
                    onValueChange = { shellViewModel.onCommandChange(it) })

                FloatingActionButton(
                    onClick = actionFabOnClick,
                    modifier = Modifier.padding(top = 6.dp),
                    content = actionFabIcon
                )
            }

            CommandCard(commandResults = commandResults)
        }
    }
}

@Composable
fun CommandCard(commandResults: List<CommandResult>) {
    val isDarkMode = LocalDarkMode.current
    val scrollState = rememberScrollState()

    LaunchedEffect(commandResults.map { it.outputFlow }.map { it.collectAsState().value }) {
        snapshotFlow { scrollState.maxValue }.collect { max ->
            scrollState.animateScrollTo(max)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            commandResults.forEach { result ->
                val outputLines by result.outputFlow.collectAsState()

                SelectionContainer(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "$ ${result.command}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                        )

                        outputLines.forEach { line ->
                            Text(
                                text = line.text,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = if (line.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("UseCompatLoadingForDrawables")
@Composable
fun AnimatedStopIcon(modifier: Modifier = Modifier) {
    val tintColor = MaterialTheme.colorScheme.onPrimaryContainer

    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                val avd =
                    context.getDrawable(R.drawable.ic_stop_animated) as? AnimatedVectorDrawable
                setImageDrawable(avd)
                setColorFilter(tintColor.toArgb(), PorterDuff.Mode.SRC_IN)
                avd?.start()
            }
        },
        modifier = modifier.size(24.dp)
    )
}
