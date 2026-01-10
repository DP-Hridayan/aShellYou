@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.file_browser.presentation.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.undrawDreamer
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.utils.isConnectedToWifi
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileOperation
import `in`.hridayan.ashell.shell.file_browser.domain.model.OperationType
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile
import `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog.CreateFolderDialog
import `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog.FileConflictDialog
import `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog.FileInfoDialog
import `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog.RenameDialog
import `in`.hridayan.ashell.shell.file_browser.presentation.util.FileIconMapper
import `in`.hridayan.ashell.shell.file_browser.presentation.viewmodel.FileBrowserEvent
import `in`.hridayan.ashell.shell.file_browser.presentation.viewmodel.FileBrowserViewModel
import java.io.File

@Composable
fun FileBrowserScreen(
    deviceAddress: String = "",
    isOwnDevice: Boolean = false,
    viewModel: FileBrowserViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navController = LocalNavController.current

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<RemoteFile?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var fileToRename by remember { mutableStateOf<RemoteFile?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var fileForInfo by remember { mutableStateOf<RemoteFile?>(null) }
    var clipboardFile by remember { mutableStateOf<RemoteFile?>(null) }
    var clipboardOperation by remember { mutableStateOf<String?>(null) }
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
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
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
                            IconButton(onClick = { viewModel.exitSelectionMode() }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Cancel")
                            }
                        },
                        actions = {
                            // Toggle select all / deselect all based on current selection
                            val allSelected = viewModel.areAllFilesSelected()
                            IconButton(
                                onClick = {
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

                            IconButton(onClick = { viewModel.downloadSelectedFiles() }) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = "Download"
                                )
                            }

                            IconButton(onClick = {
                                val selectedPaths = viewModel.getSelectedFilePaths()
                                if (selectedPaths.isNotEmpty()) {
                                    clipboardPaths = selectedPaths
                                    clipboardOperation = "copy_batch"
                                    clipboardFile =
                                        state.files.find { it.path == selectedPaths.first() }
                                    Toast.makeText(
                                        context,
                                        "Copied ${selectedPaths.size} items",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    viewModel.exitSelectionMode()
                                }
                            }) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy")
                            }

                            IconButton(onClick = {
                                val selectedPaths = viewModel.getSelectedFilePaths()
                                if (selectedPaths.isNotEmpty()) {
                                    clipboardPaths = selectedPaths
                                    clipboardOperation = "move_batch"
                                    clipboardFile =
                                        state.files.find { it.path == selectedPaths.first() }
                                    Toast.makeText(
                                        context,
                                        "Cut ${selectedPaths.size} items",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    viewModel.exitSelectionMode()
                                }
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.DriveFileMove,
                                    contentDescription = "Move"
                                )
                            }

                            IconButton(onClick = {
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
                            AutoResizeableText(
                                text = deviceAddress.ifEmpty { "File Browser" },
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace
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
                    when {
                        state.isLoading -> {
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
                                            ?: stringResource(R.string.fb_connection_error),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = {
                                        // If we're in error state and WiFi is connected, always try 
                                        // silent reconnect since the error indicates connection issue
                                        if (isWifiConnected) {
                                            viewModel.silentReconnectAndRefresh()
                                        } else {
                                            // WiFi not connected, just show error persists
                                            viewModel.refresh()
                                        }
                                    }) {
                                        Text(stringResource(R.string.fb_retry))
                                    }
                                }
                            }
                        }

                        else -> {
                            val displayFiles = state.files.filterNot { it.isParentDirectory }
                            val isEmpty = state.isVirtualEmptyFolder || displayFiles.isEmpty()

                            val listState = rememberLazyListState()

                            Box(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    items(displayFiles, key = { it.path }) { file ->
                                        FileListItem(
                                            file = file,
                                            isSelectionMode = state.isSelectionMode,
                                            isSelected = state.selectedFiles.contains(file.path),
                                            onClick = {
                                                if (state.isSelectionMode) {
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
                                                clipboardOperation = "copy"
                                                Toast.makeText(
                                                    context,
                                                    "Copied: ${file.name}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onMove = {
                                                clipboardFile = file
                                                clipboardOperation = "move"
                                                Toast.makeText(
                                                    context,
                                                    "Cut: ${file.name}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onInfo = {
                                                fileForInfo = file
                                                showInfoDialog = true
                                            },
                                            hideDownload = isOwnDevice,
                                            modifier = Modifier.animateItem()
                                        )
                                    }

                                    if (isEmpty) {
                                        item {

                                        }
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
                                        text = stringResource(R.string.fb_empty_folder),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.9f
                                        ),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                        }
                    }

                    // Progress dialog overlay
                    if (state.operations.isNotEmpty() && !isProgressMinimized) {
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
                                        text = "Operations",
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TextButton(onClick = { isProgressMinimized = true }) {
                                            Text("Minimize")
                                        }
                                        TextButton(onClick = { viewModel.cancelAllOperations() }) {
                                            Text(
                                                "Cancel all",
                                                color = MaterialTheme.colorScheme.error
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
                    val operationText = when (clipboardOperation) {
                        "copy", "copy_batch" -> "Copying ${clipboardPaths.size.takeIf { it > 0 } ?: 1} item(s)"
                        "move", "move_batch" -> "Moving ${clipboardPaths.size.takeIf { it > 0 } ?: 1} item(s)"
                        else -> "Ready to paste"
                    }
                    Text(
                        text = operationText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Paste button
                    IconButton(
                        onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                            when (clipboardOperation) {
                                "copy" -> {
                                    clipboardFile?.let { file ->
                                        val destPath = "${state.currentPath}/${file.name}"
                                        viewModel.copyFile(file.path, destPath)
                                    }
                                }

                                "move" -> {
                                    clipboardFile?.let { file ->
                                        val destPath = "${state.currentPath}/${file.name}"
                                        viewModel.moveFile(file.path, destPath)
                                    }
                                }

                                "copy_batch" -> {
                                    viewModel.copyFileBatch(clipboardPaths, state.currentPath)
                                }

                                "move_batch" -> {
                                    viewModel.moveFileBatch(clipboardPaths, state.currentPath)
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
            horizontalAlignment = Alignment.End,
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
                        onCheckedChange = { fabMenuExpanded = !fabMenuExpanded }
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
                // Hide upload when browsing own device (file transfer makes no sense)
                if (!isOwnDevice) {
                    FloatingActionButtonMenuItem(
                        onClick = {
                            fabMenuExpanded = false
                            filePickerLauncher.launch("*/*")
                        },
                        text = { Text("Upload file") },
                        icon = { Icon(Icons.Rounded.Upload, contentDescription = null) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        showCreateFolderDialog = true
                    },
                    text = { Text("New folder") },
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
            }
        )
    }

    if (showDeleteDialog) {
        val isSelectionDelete = state.isSelectionMode && state.selectedFiles.isNotEmpty()
        val deleteCount = if (isSelectionDelete) state.selectedFiles.size else 1
        val deleteTitle = if (isSelectionDelete) {
            "Delete $deleteCount items?"
        } else {
            "Delete ${if (fileToDelete?.isDirectory == true) "folder" else "file"}?"
        }
        val deleteText = if (isSelectionDelete) {
            "Are you sure you want to delete $deleteCount selected items?"
        } else {
            "Are you sure you want to delete \"${fileToDelete?.name}\"?"
        }

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                fileToDelete = null
            },
            title = { Text(deleteTitle) },
            text = { Text(deleteText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isSelectionDelete) {
                            viewModel.deleteSelectedFiles()
                        } else {
                            fileToDelete?.let { viewModel.deleteFile(it.path) }
                        }
                        showDeleteDialog = false
                        fileToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    fileToDelete = null
                }) {
                    Text("Cancel")
                }
            }
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
            onResolution = { resolution ->
                viewModel.resolveConflict(resolution)
            },
            onDismiss = { viewModel.dismissConflict() }
        )
    }
}

@Composable
private fun FileListItem(
    file: RemoteFile,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onInfo: () -> Unit,
    hideDownload: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else Color.Transparent
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (isSelectionMode) {
                        // In selection mode, long click toggles selection (same as click)
                        onClick()
                    } else {
                        onLongClick()
                    }
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = getFileIcon(file),
            contentDescription = null,
            tint = if (file.isDirectory) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (file.isDirectory) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!file.isDirectory && file.displaySize.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = file.displaySize,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (file.lastModified.isNotEmpty()) {
                        Text(
                            text = file.lastModified,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // In selection mode: show checkbox, otherwise show menu
        if (!file.isParentDirectory) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() }
                )
            } else {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Hide download when browsing own device
                        if (!file.isDirectory && !hideDownload) {
                            DropdownMenuItem(
                                text = { Text("Download") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Download,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    onDownload()
                                    showMenu = false
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Rename") },
                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                            onClick = {
                                onRename()
                                showMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Copy") },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.ContentCopy,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onCopy()
                                showMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Move") },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.DriveFileMove,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onMove()
                                showMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Info") },
                            leadingIcon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                            onClick = {
                                onInfo()
                                showMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                onDelete()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// Dialog functions moved to presentation/component/dialog/ for better code organization

private fun getFileIcon(file: RemoteFile): ImageVector {
    return FileIconMapper.getIcon(file)
}

@Composable
private fun PathBreadcrumbs(
    currentPath: String,
    onNavigateToPath: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val basePath = "/storage/emulated/0"
    val relativePath = if (currentPath.startsWith(basePath)) {
        currentPath.removePrefix(basePath)
    } else {
        currentPath
    }

    val segments = mutableListOf<Pair<String, String>>()
    segments.add("Internal Storage" to basePath)

    if (relativePath.isNotBlank() && relativePath != "/") {
        val parts = relativePath.trim('/').split("/")
        var accumulatedPath = basePath
        for (part in parts) {
            if (part.isNotBlank()) {
                accumulatedPath = "$accumulatedPath/$part"
                segments.add(part to accumulatedPath)
            }
        }
    }

    LaunchedEffect(currentPath) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        segments.forEachIndexed { index, (displayName, fullPath) ->
            val isLast = index == segments.lastIndex

            TextButton(
                onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                    if (isLast) return@withHaptic
                    onNavigateToPath(fullPath)
                },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.animateContentSize()
            ) {
                AutoResizeableText(
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isLast) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            if (!isLast) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun OperationItem(
    operation: FileOperation,
    onCancel: () -> Unit
) {
    val progress = if (operation.totalBytes > 0) {
        operation.bytesTransferred.toFloat() / operation.totalBytes.toFloat()
    } else 0f

    val icon = when (operation.type) {
        OperationType.DOWNLOAD -> Icons.Rounded.Download
        OperationType.UPLOAD -> Icons.Rounded.Upload
        OperationType.COPY -> Icons.Rounded.ContentCopy
        OperationType.MOVE -> Icons.AutoMirrored.Rounded.DriveFileMove
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = operation.fileName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (operation.totalBytes > 0) {
                Text(
                    text = "${formatSize(operation.bytesTransferred)} / ${formatSize(operation.totalBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }
        IconButton(onClick = onCancel) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Cancel",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
