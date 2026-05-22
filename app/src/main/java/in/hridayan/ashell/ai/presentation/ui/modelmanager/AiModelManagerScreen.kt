@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.ai.presentation.ui.modelmanager

import android.os.StatFs
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.ai.presentation.viewmodel.AiModelManagerViewModel

/**
 * Settings screen for managing AI models.
 * Lists available models with download/select/delete controls
 * and shows storage usage.
 */
@Composable
fun AiModelManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: AiModelManagerViewModel = hiltViewModel()
) {
    val models by viewModel.models.collectAsState()
    val storageUsage by viewModel.storageUsage.collectAsState()
    val cacheSizeBytes by viewModel.cacheSizeBytes.collectAsState()

    val context = LocalContext.current
    val totalAvailableBytes = remember {
        try {
            val stat = StatFs(context.filesDir.absolutePath)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (_: Exception) {
            0L
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "AI Models",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Storage usage bar
            item {
                StorageUsageBar(
                    usedBytes = storageUsage,
                    totalAvailableBytes = totalAvailableBytes
                )
                Spacer(Modifier.height(8.dp))
            }

            // Model cards
            items(
                items = models,
                key = { it.model.id }
            ) { modelState ->
                ModelCard(
                    state = modelState,
                    onDownload = { viewModel.downloadModel(modelState.model.id) },
                    onCancelDownload = { viewModel.cancelDownload(modelState.model.id) },
                    onDelete = { viewModel.deleteModel(modelState.model.id) },
                    onSelect = { viewModel.selectModel(modelState.model.id) },
                    onDismissError = { viewModel.dismissError(modelState.model.id) }
                )
            }

            // Cache management
            item {
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = { viewModel.clearCache() },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        Icons.Rounded.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    val cacheMb = cacheSizeBytes / 1_048_576.0
                    Text(
                        text = if (cacheMb > 0.01) "Clear analysis cache (%.1f MB)".format(cacheMb)
                        else "Clear analysis cache"
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
