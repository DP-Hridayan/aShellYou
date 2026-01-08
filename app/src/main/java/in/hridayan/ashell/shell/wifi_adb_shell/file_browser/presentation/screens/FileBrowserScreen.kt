@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.presentation.screens

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.model.RemoteFile
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.presentation.viewmodel.FileBrowserEvent
import `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.presentation.viewmodel.FileBrowserViewModel
import java.io.File

@Composable
fun FileBrowserScreen(
    viewModel: FileBrowserViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navController = LocalNavController.current

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<RemoteFile?>(null) }
    
    // Rename dialog state
    var showRenameDialog by remember { mutableStateOf(false) }
    var fileToRename by remember { mutableStateOf<RemoteFile?>(null) }
    
    // Info dialog state
    var showInfoDialog by remember { mutableStateOf(false) }
    var fileForInfo by remember { mutableStateOf<RemoteFile?>(null) }
    
    // Clipboard for copy/move
    var clipboardFile by remember { mutableStateOf<RemoteFile?>(null) }
    var clipboardOperation by remember { mutableStateOf<String?>(null) } // "copy" or "move"

    // File picker for upload
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Get file name from URI
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val fileName = cursor?.use { c ->
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                c.moveToFirst()
                if (nameIndex >= 0) c.getString(nameIndex) else "uploaded_file"
            } ?: "uploaded_file"
            
            // Copy to cache and upload
            val cacheFile = File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            viewModel.uploadFile(cacheFile.absolutePath, fileName)
        }
    }

    // Handle events
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

    // Determine if we're at the root/home path
    val isAtHome = state.currentPath == "/storage/emulated/0"

    // Back handler - navigate to parent directory, or exit if at home
    BackHandler {
        if (isAtHome) {
            navController.popBackStack()
        } else {
            viewModel.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Breadcrumb navigation
                    PathBreadcrumbs(
                        currentPath = state.currentPath,
                        onNavigateToPath = { path -> viewModel.loadFiles(path) }
                    )
                },
                navigationIcon = {
                    // Back/Up navigation
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
                    // Home button - go directly to WiFi ADB screen
                    IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = "Home"
                        )
                    }
                    // Upload button
                    IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                        filePickerLauncher.launch("*/*")
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Upload,
                            contentDescription = "Upload"
                        )
                    }
                    IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                        viewModel.refresh()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                        showCreateFolderDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.CreateNewFolder,
                            contentDescription = "New Folder"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    // Connection error - show retry (breadcrumbs handle navigation)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.error ?: "Connection error",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    // Filter out ".." entries since breadcrumbs handle navigation
                    val displayFiles = state.files.filterNot { it.isParentDirectory }
                    val isEmpty = state.isVirtualEmptyFolder || displayFiles.isEmpty()
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(displayFiles, key = { it.path }) { file ->
                            FileListItem(
                                file = file,
                                onClick = { viewModel.onFileClick(file) },
                                onDownload = {
                                    viewModel.downloadFile(file.path, file.name)
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
                                    Toast.makeText(context, "Copied: ${file.name}", Toast.LENGTH_SHORT).show()
                                },
                                onMove = {
                                    clipboardFile = file
                                    clipboardOperation = "move"
                                    Toast.makeText(context, "Cut: ${file.name}", Toast.LENGTH_SHORT).show()
                                },
                                onInfo = {
                                    fileForInfo = file
                                    showInfoDialog = true
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                        
                        // Show "empty folder" message if no files besides parent
                        if (isEmpty) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "This folder is empty",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            // Progress overlay
            if (state.isOperationInProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.operationMessage ?: "Processing...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { state.operationProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    // Create folder dialog
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                viewModel.createDirectory(name)
                showCreateFolderDialog = false
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog && fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${if (fileToDelete!!.isDirectory) "folder" else "file"}?") },
            text = { Text("Are you sure you want to delete \"${fileToDelete!!.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        fileToDelete?.let { viewModel.deleteFile(it.path) }
                        showDeleteDialog = false
                        fileToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Rename dialog
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
    
    // Info dialog
    if (showInfoDialog && fileForInfo != null) {
        FileInfoDialog(
            file = fileForInfo!!,
            onDismiss = {
                showInfoDialog = false
                fileForInfo = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListItem(
    file: RemoteFile,
    onClick: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File icon
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

        // File info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (file.isDirectory) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!file.isDirectory && file.displaySize.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

        // More options menu
        if (!file.isParentDirectory) {
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
                    // Download (files only)
                    if (!file.isDirectory) {
                        DropdownMenuItem(
                            text = { Text("Download") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Download, contentDescription = null)
                            },
                            onClick = {
                                onDownload()
                                showMenu = false
                            }
                        )
                    }
                    
                    // Rename
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Edit, contentDescription = null)
                        },
                        onClick = {
                            onRename()
                            showMenu = false
                        }
                    )
                    
                    // Copy
                    DropdownMenuItem(
                        text = { Text("Copy") },
                        leadingIcon = {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                        },
                        onClick = {
                            onCopy()
                            showMenu = false
                        }
                    )
                    
                    // Move
                    DropdownMenuItem(
                        text = { Text("Move") },
                        leadingIcon = {
                            Icon(Icons.Rounded.DriveFileMove, contentDescription = null)
                        },
                        onClick = {
                            onMove()
                            showMenu = false
                        }
                    )
                    
                    // Info
                    DropdownMenuItem(
                        text = { Text("Info") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Info, contentDescription = null)
                        },
                        onClick = {
                            onInfo()
                            showMenu = false
                        }
                    )
                    
                    // Delete
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

@Composable
private fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create folder") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(folderName) },
                enabled = folderName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getFileIcon(file: RemoteFile): ImageVector {
    return when {
        file.isParentDirectory -> Icons.Rounded.FolderOpen
        file.isDirectory -> Icons.Rounded.Folder
        else -> Icons.AutoMirrored.Rounded.InsertDriveFile
    }
}

/**
 * Breadcrumb navigation for file browser path.
 * Displays path as: Internal Storage > folder1 > folder2
 * Each segment is clickable for direct navigation.
 */
@Composable
private fun PathBreadcrumbs(
    currentPath: String,
    onNavigateToPath: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    // Parse path into segments
    val basePath = "/storage/emulated/0"
    val relativePath = if (currentPath.startsWith(basePath)) {
        currentPath.removePrefix(basePath)
    } else {
        currentPath
    }
    
    val segments = mutableListOf<Pair<String, String>>() // (displayName, fullPath)
    
    // Add "Internal Storage" as root
    segments.add("Internal Storage" to basePath)
    
    // Add path segments
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
    
    // Auto-scroll to end when path changes
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
            
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                color = if (isLast) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(enabled = !isLast) { onNavigateToPath(fullPath) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
            
            if (!isLast) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Dialog for renaming a file or folder.
 */
@Composable
private fun RenameDialog(
    currentName: String,
    isDirectory: Boolean,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename ${if (isDirectory) "folder" else "file"}") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onRename(newName) },
                enabled = newName.isNotBlank() && newName != currentName
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog showing file/folder information.
 */
@Composable
private fun FileInfoDialog(
    file: RemoteFile,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (file.isDirectory) "Folder Info" else "File Info") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow("Name", file.name)
                InfoRow("Path", file.path)
                InfoRow("Type", if (file.isDirectory) "Folder" else "File")
                if (!file.isDirectory && file.displaySize.isNotEmpty()) {
                    InfoRow("Size", file.displaySize)
                }
                if (file.lastModified.isNotEmpty()) {
                    InfoRow("Modified", file.lastModified)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
