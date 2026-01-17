@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.file_browser.presentation.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.undrawDreamer
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.utils.isConnectedToWifi
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.shell.file_browser.domain.model.ClipboardOperation
import `in`.hridayan.ashell.shell.file_browser.domain.model.ConnectionMode
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile
import `in`.hridayan.ashell.shell.file_browser.presentation.component.FileListItem
import `in`.hridayan.ashell.shell.file_browser.presentation.component.OperationItem
import `in`.hridayan.ashell.shell.file_browser.presentation.component.PathBreadcrumbs
import `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog.CreateFolderDialog
import `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog.DeleteFileDialog
import `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog.FileConflictDialog
import `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog.FileInfoDialog
import `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog.RenameDialog
import `in`.hridayan.ashell.shell.file_browser.presentation.model.FileBrowserEvent
import `in`.hridayan.ashell.shell.file_browser.presentation.viewmodel.FileBrowserViewModel
import java.io.File

@Composable
fun FileBrowserScreen(
    deviceAddress: String = "",
    connectionMode: ConnectionMode = ConnectionMode.WIFI_ADB,
    isOwnDevice: Boolean = false,
    viewModel: FileBrowserViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val res = LocalResources.current
    val navController = LocalNavController.current
    val weakHaptic = LocalWeakHaptic.current

    val pullToRefreshState = rememberPullToRefreshState()

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<RemoteFile?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var fileToRename by remember { mutableStateOf<RemoteFile?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var fileForInfo by remember { mutableStateOf<RemoteFile?>(null) }
    var clipboardFile by remember { mutableStateOf<RemoteFile?>(null) }
    var clipboardOperation by remember { mutableStateOf<ClipboardOperation?>(null) }
    var clipboardPaths by remember { mutableStateOf<List<String>>(emptyList()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val fileName = cursor?.use { c ->
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                c.moveToFirst()
                if (nameIndex >= 0) c.getString(nameIndex) else "uploaded_file"
            } ?: "uploaded_file"

            val cacheFile = File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            viewModel.uploadFile(cacheFile.absolutePath, fileName)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FileBrowserEvent.ShowToast -> {
                    val message = if (event.formatArgs.isEmpty()) {
                        res.getString(event.messageResId)
                    } else {
                        res.getString(event.messageResId, *event.formatArgs.toTypedArray())
                    }
                    showToast(context, message)
                }

                else -> {}
            }
        }
    }

    // Set connection mode based on navigation parameter (WiFi ADB or OTG)
    LaunchedEffect(connectionMode) {
        viewModel.setConnectionMode(connectionMode)
    }

    val isAtHome = state.currentPath == "/storage/emulated/0"
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isProgressMinimized by rememberSaveable { mutableStateOf(false) }

    val dimAlpha by animateFloatAsState(
        targetValue = if (fabMenuExpanded) 0.5f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "DimAlphaAnimation"
    )

    BackHandler(enabled = state.isSelectionMode || fabMenuExpanded || !isAtHome) {
        when {
            state.isSelectionMode -> viewModel.exitSelectionMode()
            fabMenuExpanded -> fabMenuExpanded = false
            isAtHome -> navController.popBackStack()
            else -> viewModel.navigateUp()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                if (state.isSelectionMode) {
                    TopAppBar(
                        title = {
                            Text("${state.selectedFiles.size}")
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        navigationIcon = {
                            IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                viewModel.exitSelectionMode()
                            }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Cancel")
                            }
                        },
                        actions = {
                            // Toggle select all / deselect all based on current selection
                            val allSelected = viewModel.areAllFilesSelected()
                            IconButton(
                                onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                    if (allSelected) {
                                        viewModel.deselectAllFiles()
                                    } else {
                                        viewModel.selectAllFiles()
                                    }
                                }
                            ) {
                                Icon(
                                    painter = if (allSelected) painterResource(R.drawable.ic_deselect_all)
                                    else painterResource(R.drawable.ic_select_all),
                                    contentDescription = if (allSelected) "Deselect all" else "Select all"
                                )
                            }

                            // Hide download in OTG mode (file transfers not supported)
                            if (connectionMode != ConnectionMode.OTG_ADB) {
                                IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                    viewModel.downloadSelectedFiles()
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = "Download"
                                    )
                                }
                            }

                            IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                val selectedPaths = viewModel.getSelectedFilePaths()
                                if (selectedPaths.isNotEmpty()) {
                                    clipboardPaths = selectedPaths
                                    clipboardOperation = ClipboardOperation.COPY_BATCH
                                    clipboardFile =
                                        state.files.find { it.path == selectedPaths.first() }
                                    showToast(
                                        context,
                                        res.getString(R.string.copied_items, selectedPaths.size)
                                    )
                                    viewModel.exitSelectionMode()
                                }
                            }) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy")
                            }

                            IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                val selectedPaths = viewModel.getSelectedFilePaths()
                                if (selectedPaths.isNotEmpty()) {
                                    clipboardPaths = selectedPaths
                                    clipboardOperation = ClipboardOperation.MOVE_BATCH
                                    clipboardFile =
                                        state.files.find { it.path == selectedPaths.first() }
                                    showToast(
                                        context,
                                        res.getString(R.string.cut_items, selectedPaths.size)
                                    )
                                    viewModel.exitSelectionMode()
                                }
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.DriveFileMove,
                                    contentDescription = "Move"
                                )
                            }

                            IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                showDeleteDialog = true
                            }) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(
                                text = deviceAddress.ifEmpty { stringResource(R.string.file_browser) },
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.basicMarquee()
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        navigationIcon = {
                            IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                if (isAtHome) {
                                    navController.popBackStack()
                                } else {
                                    viewModel.navigateUp()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                navController.popBackStack()
                            }) {
                                Icon(Icons.Rounded.Home, contentDescription = "Home")
                            }
                            IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                viewModel.refresh()
                            }) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PathBreadcrumbs(
                    currentPath = state.currentPath,
                    onNavigateToPath = { path -> viewModel.loadFiles(path) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 15.dp, vertical = 4.dp)
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    // Preserve scroll position across state changes
                    val listState = rememberLazyListState()

                    when {
                        state.isLoading || state.isPasting -> {
                            LoadingIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        state.error != null -> {
                            val isWifiConnected = context.isConnectedToWifi()

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = state.error
                                            ?: stringResource(R.string.connection_error),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = withHaptic {
                                        if (isWifiConnected) {
                                            viewModel.silentReconnectAndRefresh()
                                        } else {
                                            viewModel.refresh()
                                        }
                                    }) {
                                        Text(stringResource(R.string.retry))
                                    }
                                }
                            }
                        }

                        else -> {
                            val displayFiles = state.files.filterNot { it.isParentDirectory }
                            val isEmpty = state.isVirtualEmptyFolder || displayFiles.isEmpty()

                            PullToRefreshBox(
                                isRefreshing = state.isLoading,
                                onRefresh = { viewModel.refresh() },
                                modifier = Modifier.fillMaxSize(),
                                state = pullToRefreshState,
                                indicator = {
                                    PullToRefreshDefaults.LoadingIndicator(
                                        state = pullToRefreshState,
                                        isRefreshing = state.isLoading,
                                        modifier = Modifier.align(Alignment.TopCenter)
                                    )
                                }
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        items(displayFiles, key = { it.path }) { file ->
                                            // Compute if this folder is disabled for paste (moving into itself)
                                            val isDisabledForPaste = remember(
                                                clipboardPaths,
                                                clipboardOperation,
                                                file.path
                                            ) {
                                                file.isDirectory &&
                                                        clipboardOperation?.isMove == true &&
                                                        (clipboardPaths.contains(file.path) || (clipboardFile?.path == file.path))
                                            }

                                            FileListItem(
                                                file = file,
                                                isSelectionMode = state.isSelectionMode,
                                                isSelected = state.selectedFiles.contains(file.path),
                                                isDisabledForPaste = isDisabledForPaste,
                                                onClick = {
                                                    if (isDisabledForPaste) {
                                                        showToast(
                                                            context,
                                                            res.getString(R.string.fb_cannot_paste_into_selected)
                                                        )
                                                    } else if (state.isSelectionMode) {
                                                        viewModel.toggleFileSelection(file)
                                                    } else {
                                                        viewModel.onFileClick(file)
                                                    }
                                                },
                                                onLongClick = {
                                                    if (!state.isSelectionMode) {
                                                        viewModel.enterSelectionMode(file)
                                                    }
                                                },
                                                onDownload = {
                                                    viewModel.downloadFile(
                                                        file.path,
                                                        file.name
                                                    )
                                                },
                                                onDelete = {
                                                    fileToDelete = file
                                                    showDeleteDialog = true
                                                },
                                                onRename = {
                                                    fileToRename = file
                                                    showRenameDialog = true
                                                },
                                                onCopy = {
                                                    clipboardFile = file
                                                    clipboardOperation = ClipboardOperation.COPY
                                                    showToast(
                                                        context,
                                                        res.getString(R.string.copy) + ": " + file.name
                                                    )
                                                },
                                                onMove = {
                                                    clipboardFile = file
                                                    clipboardOperation = ClipboardOperation.MOVE
                                                    showToast(
                                                        context,
                                                        res.getString(R.string.cut_to_clipboard) + ": " + file.name
                                                    )
                                                },
                                                onInfo = {
                                                    fileForInfo = file
                                                    showInfoDialog = true
                                                },
                                                hideDownload = isOwnDevice || connectionMode == ConnectionMode.OTG_ADB,
                                                modifier = Modifier.animateItem()
                                            )
                                        }

                                        item {
                                            Spacer(modifier = Modifier.height(80.dp))
                                        }
                                    }


                                    if (isEmpty) Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Center)
                                            .padding(bottom = 45.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(30.dp)
                                    ) {
                                        Image(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 40.dp),
                                            imageVector = DynamicColorImageVectors.undrawDreamer(),
                                            contentDescription = null
                                        )

                                        Text(
                                            text = stringResource(R.string.empty_folder),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.9f
                                            ),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                } // End Box
                            } // End PullToRefreshBox
                        }
                    }

                    // Progress dialog overlay
                    if (state.operations.isNotEmpty() && !isProgressMinimized) {
                        val interactionSources = remember { List(2) { MutableInteractionSource() } }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .padding(32.dp)
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(
                                        text = stringResource(R.string.operations),
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    state.operations.forEach { operation ->
                                        OperationItem(
                                            operation = operation,
                                            onCancel = { viewModel.cancelOperation(operation.id) }
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    @Suppress("DEPRECATION")
                                    ButtonGroup(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedButton(
                                            onClick = withHaptic(HapticFeedbackType.Reject) {
                                                isProgressMinimized = true
                                            },
                                            shapes = ButtonDefaults.shapes(),
                                            modifier = Modifier
                                                .weight(1f)
                                                .animateWidth(interactionSources[0]),
                                            interactionSource = interactionSources[0],
                                        ) {
                                            AutoResizeableText(
                                                text = stringResource(R.string.minimize),
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }

                                        Button(
                                            onClick = withHaptic(HapticFeedbackType.Confirm) {
                                                viewModel.cancelAllOperations()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .animateWidth(interactionSources[1]),
                                            interactionSource = interactionSources[1],
                                            shapes = ButtonDefaults.shapes(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = MaterialTheme.colorScheme.onError
                                            )
                                        ) {
                                            AutoResizeableText(
                                                text = stringResource(R.string.cancel_all),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dim overlay for FAB menu
        if (dimAlpha > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = dimAlpha))
                    .clickable(
                        enabled = fabMenuExpanded,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { fabMenuExpanded = false }
                    )
            )
        }

        // Clipboard dock visibility state
        val hasClipboard = clipboardFile != null || clipboardPaths.isNotEmpty()
        val dockHeight by animateDpAsState(
            targetValue = if (hasClipboard) 64.dp else 0.dp,
            animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
            label = "DockHeightAnimation"
        )

        // Clipboard dock at bottom of screen
        AnimatedVisibility(
            visible = hasClipboard,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300, easing = LinearOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = LinearOutSlowInEasing)
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cancel button
                    IconButton(
                        onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                            clipboardFile = null
                            clipboardOperation = null
                            clipboardPaths = emptyList()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    // Status text
                    val operationText = when {
                        clipboardOperation?.isCopy == true -> stringResource(
                            R.string.copying_items,
                            clipboardPaths.size.takeIf { it > 0 } ?: 1)

                        clipboardOperation?.isMove == true -> stringResource(
                            R.string.moving_items,
                            clipboardPaths.size.takeIf { it > 0 } ?: 1)

                        else -> stringResource(R.string.ready_to_paste)
                    }
                    Text(
                        text = operationText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Paste button
                    IconButton(
                        onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                            val paths = if (clipboardOperation?.isBatch == true) {
                                clipboardPaths
                            } else {
                                clipboardFile?.let { listOf(it.path) } ?: emptyList()
                            }

                            if (paths.isNotEmpty()) {
                                when {
                                    clipboardOperation?.isCopy == true -> {
                                        viewModel.copyFileBatch(paths, state.currentPath)
                                    }

                                    clipboardOperation?.isMove == true -> {
                                        viewModel.moveFileBatch(paths, state.currentPath)
                                    }
                                }
                            }

                            clipboardFile = null
                            clipboardOperation = null
                            clipboardPaths = emptyList()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentPaste,
                            contentDescription = "Paste",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // FAB column - pushed up when dock is visible
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp + dockHeight, end = 16.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Minimized progress indicator
            if (state.operations.isNotEmpty() && isProgressMinimized) {
                SmallFloatingActionButton(
                    onClick = { isProgressMinimized = false },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = state.operations.size.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Main FAB menu
            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        modifier = Modifier
                            .semantics {
                                traversalIndex = -1f
                                stateDescription =
                                    if (fabMenuExpanded) "Expanded" else "Collapsed"
                                contentDescription = "Toggle menu"
                            },
                        checked = fabMenuExpanded,
                        containerColor = ToggleFloatingActionButtonDefaults.containerColor(
                            initialColor = MaterialTheme.colorScheme.primaryContainer,
                            finalColor = MaterialTheme.colorScheme.primary
                        ),
                        onCheckedChange = {
                            fabMenuExpanded = !fabMenuExpanded
                            weakHaptic()
                        }
                    ) {
                        val imageVector by remember {
                            derivedStateOf {
                                if (checkedProgress > 0.5f) Icons.Rounded.Close else Icons.Rounded.Add
                            }
                        }

                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = null,
                            modifier = Modifier.animateIcon(
                                checkedProgress = { checkedProgress },
                                color = ToggleFloatingActionButtonDefaults.iconColor(
                                    initialColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    finalColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        )
                    }
                }
            ) {
                // Hide upload when browsing own device or in OTG mode (file transfer not supported)
                if (!isOwnDevice && connectionMode != ConnectionMode.OTG_ADB) {
                    FloatingActionButtonMenuItem(
                        onClick = withHaptic(HapticFeedbackType.Confirm) {
                            fabMenuExpanded = false
                            filePickerLauncher.launch("*/*")
                        },
                        text = { AutoResizeableText(stringResource(R.string.upload_file)) },
                        icon = { Icon(Icons.Rounded.Upload, contentDescription = null) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                FloatingActionButtonMenuItem(
                    onClick = withHaptic(HapticFeedbackType.Confirm) {
                        fabMenuExpanded = false
                        showCreateFolderDialog = true
                    },
                    text = { AutoResizeableText(stringResource(R.string.new_folder)) },
                    icon = {
                        Icon(
                            Icons.Rounded.CreateNewFolder,
                            contentDescription = null
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

// Dialogs
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                viewModel.createDirectory(name)
                showCreateFolderDialog = false
            },
            existingNames = state.files.map { it.name }.toSet()
        )
    }

    if (showDeleteDialog) {
        val isSelectionDelete = state.isSelectionMode && state.selectedFiles.isNotEmpty()
        val deleteCount = if (isSelectionDelete) state.selectedFiles.size else 1
        val deleteTitle = if (isSelectionDelete) {
            stringResource(R.string.delete_items_title, deleteCount)
        } else {
            if (fileToDelete?.isDirectory == true) stringResource(R.string.delete_folder)
            else stringResource(R.string.delete_file)
        }
        val deleteMessage = if (isSelectionDelete) {
            stringResource(R.string.delete_items_message, deleteCount)
        } else {
            stringResource(R.string.delete_message, fileToDelete?.name ?: "")
        }

        DeleteFileDialog(
            onDismiss = {
                showDeleteDialog = false
                fileToDelete = null
            },
            onDelete = {
                if (isSelectionDelete) {
                    viewModel.deleteSelectedFiles()
                } else {
                    fileToDelete?.let { viewModel.deleteFile(it.path) }
                }
                showDeleteDialog = false
                fileToDelete = null
            },
            title = deleteTitle,
            message = deleteMessage
        )
    }

    if (showRenameDialog && fileToRename != null) {
        RenameDialog(
            currentName = fileToRename!!.name,
            isDirectory = fileToRename!!.isDirectory,
            onDismiss = {
                showRenameDialog = false
                fileToRename = null
            },
            onRename = { newName ->
                val oldPath = fileToRename!!.path
                val parentPath = File(oldPath).parent ?: ""
                val newPath = "$parentPath/$newName"
                viewModel.renameFile(oldPath, newPath)
                showRenameDialog = false
                fileToRename = null
            }
        )
    }

    if (showInfoDialog && fileForInfo != null) {
        FileInfoDialog(
            file = fileForInfo!!,
            onDismiss = {
                showInfoDialog = false
                fileForInfo = null
            }
        )
    }

// Conflict Resolution Dialog
    state.pendingConflict?.let { conflict ->
        FileConflictDialog(
            conflict = conflict,
            showApplyToAll = conflict.remainingCount > 0,
            onResolution = { resolution, applyToAll ->
                viewModel.resolveConflict(resolution, applyToAll)
            },
            onDismiss = { viewModel.dismissConflict() }
        )
    }
}
